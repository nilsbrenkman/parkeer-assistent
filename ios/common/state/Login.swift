//
//  UserAuth.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 13/06/2021.
//

import Foundation
import SwiftUI
import LocalAuthentication

@MainActor
class Login: ObservableObject, ErrorHandler {
    
    @Published var isLoading: Bool = true
    @Published var isBackground: Bool = false
    @Published var isLoggedIn: Bool = false
    
    public var autoLogin: Bool = true
    
    private var credentials: [Credentials] = []
    private var authenticated: Date? = nil

    let loginClient: LoginClient

    init() throws {
        loginClient = try ClientManager.instance.get(LoginClient.self)
        ApiClient.client.registerErrorHandler(self)
    }
    
    nonisolated func handleError(_ error: ClientError) {
        Task {
            await MainActor.run() {
                if error == .Unauthorized {
                    if self.isLoggedIn {
                        MessageManager.instance.addMessage(Lang.Error.unauthorized.localized(), type: Type.WARN)
                    }
                        
                    self.isLoggedIn = false
                    self.isLoading = false
                    self.isBackground = false
                    self.autoLogin = false
                    return
                }
                if error == .NoHttpResponse {
                    MessageManager.instance.addMessage(Lang.Error.serverUnknown.localized(), type: Type.ERROR)
                }
                MessageManager.instance.addMessage(Lang.Error.serverUnknown.localized(), type: Type.ERROR)
            }
        }

    }
    
    func loggedIn() {
        self.isLoading = true
        
        Task {
            let response = try await self.loginClient.loggedId()
            
            self.isLoggedIn = response.success
            self.isLoading = false
        }
    }
    
    func login(username: String, password: String, storeCredentials: Bool, onComplete: @escaping () -> Void) {
        Task {
            let response = try await self.loginClient.login(username: username, password: password)
            
            if response.success {
                Stats.user.loginCount += 1
                if storeCredentials {
                    do {
                        try Keychain.storeCredentials(username: username, password: password)
                        Keychain.setRecent(username)
                    } catch {
                        print("Store credentials failed: \(error)")
                    }
                }
                self.isLoggedIn = true
                
            } else {
                MessageManager.instance.addMessage(response.message, type: Type.ERROR)
            }
            onComplete()
        }
    }
    
    func logout(_ onComplete: @escaping () -> Void) {
        self.isLoading = true
        
        Task {
            let response = try await self.loginClient.logout()
            
            if response.success {
                self.isLoggedIn = false
            } else {
                MessageManager.instance.addMessage(response.message, type: Type.ERROR)
            }
            onComplete()
            self.autoLogin = false
            self.isLoading = false
        }
    }
    
    func accounts() throws -> [Credentials] {
        let context = LAContext()
        var error: NSError?

        if authenticated != nil && authenticated!.addingTimeInterval(5 * 60) > Date.now() {
            return self.credentials
        }
        
        if !context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) {
            throw AuthenticationError.Unavailable
        }
        
        let stored = Keychain.retrieveCredentials()
        if stored.isEmpty {
            return []
        }
            
        let reason = Lang.Login.reason.localized()
        let semaphore = DispatchSemaphore(value: 0)
        
        var success: Bool = false
        
        context.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, localizedReason: reason) { authentication, error in
            success = authentication
            semaphore.signal()
        }
        
        semaphore.wait()
        
        if success {
            self.credentials = stored
            self.authenticated = Date.now()
        } else {
            throw AuthenticationError.Failed
        }
        return self.credentials
    }
    
    func addAccount() {
        try? Keychain.storeCredentials(username: "", password: "")
        self.credentials = Keychain.retrieveCredentials()
    }
    
    func updateAccount(_ account: Credentials, username: String, password: String, alias: String?) {
        let isRecent = Keychain.getRecent(self.credentials)?.username == account.username
        try? Keychain.updateCredentials(account, username: username, password: password, alias: alias)
        self.credentials = Keychain.retrieveCredentials()
        if isRecent {
            Keychain.setRecent(username)
        }
    }

    func deleteAccount(_ account: Credentials) {
        let isRecent = Keychain.getRecent(self.credentials)?.username == account.username
        try? Keychain.deleteCredentials(account: account)
        self.credentials = Keychain.retrieveCredentials()
        if isRecent {
            Keychain.setRecent(self.credentials.first?.username)
        }
    }

}
