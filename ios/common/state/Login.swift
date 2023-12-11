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
    @Published var accounts: [Credentials] = []
    
    public var autoLogin: Bool = true
    
    private var authenticated: Date? = nil

    let loginClient: LoginClient
    
    weak var user: User?
    weak var payment: Payment?

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
                        
                    self.clearUser()
                    return
                }
                if error == .NoHttpResponse {
                    MessageManager.instance.addMessage(Lang.Error.serverUnknown.localized(), type: Type.ERROR)
                }
                MessageManager.instance.addMessage(Lang.Error.serverUnknown.localized(), type: Type.ERROR)
            }
        }
    }
    
    private func clearUser() {
        isLoggedIn = false
        isLoading = false
        isBackground = false
        autoLogin = false
        
        user?.page = nil
        user?.selectedVisitor = nil
        user?.isLoaded = false
    }
    
    func loggedIn() async {
        isLoading = true
        
        guard let response = try? await loginClient.loggedId() else {
            isLoggedIn = false
            isLoading = false
            return
        }
        
        isLoggedIn = response.success
        isLoading = false
    }
    
    func login(username: String, password: String, storeCredentials: Bool) async {
        
        let response: Response
        do {
            response = try await loginClient.login(username: username, password: password)
        } catch {
            if let clientError = error as? ClientError {
                if clientError == .Unauthorized {
                    MessageManager.instance.addMessage(Lang.Login.failed.localized(), type: .WARN)
                    return
                }
            }
            MessageManager.instance.addMessage(Lang.Login.error.localized(), type: .ERROR)
            return
        }
        
        if response.success {
            Stats.user.loginCount += 1
            if storeCredentials {
                do {
                    try Keychain.storeCredentials(username: username, password: password, alias: nil)
                    Keychain.setRecent(username)
                } catch {
                    Log.warning("Store credentials failed: \(error)")
                }
            }
            isLoggedIn = true
            
        } else {
            MessageManager.instance.addMessage(response.message, type: Type.ERROR)
        }
        
    }
    
    func logout() async {
        isLoading = true
        
        if let response = try? await loginClient.logout() {
            if !response.success {
                MessageManager.instance.addMessage(response.message, type: Type.ERROR)
            }
        }
        
        clearUser()
    }
    
    func selectedAccount() -> Credentials {
        Keychain.getRecent(accounts) ?? Credentials(username: "", password: "")
    }
    
    func setSelectedAccount(_ account: Credentials?) {
        Keychain.setRecent(account?.username ?? "")
    }

    func loadAccounts() throws -> [Credentials] {
        let context = LAContext()
        var error: NSError?

        if authenticated != nil && authenticated!.addingTimeInterval(5 * 60) > Date.now() {
            return accounts
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
            accounts = stored
            authenticated = Date.now()
        } else {
            throw AuthenticationError.Failed
        }
        return accounts
    }
    
    func addAccount(username: String, password: String, alias: String?) {
        try? Keychain.storeCredentials(username: username, password: password, alias: alias)
        accounts = Keychain.retrieveCredentials()
    }
    
    func updateAccount(_ account: Credentials, username: String, password: String, alias: String?) {
        let isRecent = Keychain.getRecent(accounts)?.username == account.username
        try? Keychain.updateCredentials(account, username: username, password: password, alias: alias)
        accounts = Keychain.retrieveCredentials()
        if isRecent {
            Keychain.setRecent(username)
        }
    }

    func deleteAccount(_ account: Credentials) {
        let isRecent = Keychain.getRecent(accounts)?.username == account.username
        try? Keychain.deleteCredentials(account: account)
        accounts = Keychain.retrieveCredentials()
        if isRecent {
            Keychain.setRecent(accounts.first?.username)
        }
    }

}
