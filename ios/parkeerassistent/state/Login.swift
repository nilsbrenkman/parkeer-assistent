//
//  UserAuth.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 13/06/2021.
//

import Foundation
import SwiftUI

class Login: ObservableObject {
    
    @Published var isLoading: Bool = true
    @Published var isBackground: Bool = false
    @Published var isLoggedIn: Bool = false
    @Published var loggedOut: Bool = false

    let loginClient: LoginClient
    var messenger: ((String, Type) -> Void)? = nil

    init() throws {
        loginClient = try ClientManager.instance.get(LoginClient.self)
    }
    
    func loggedIn() {
        DispatchQueue.main.async {
            self.isLoading = true
        }
        
        DispatchQueue.global().async {
            self.loginClient.loggedId() { response in
                DispatchQueue.main.async {
                    self.isLoggedIn = response.success
                    self.isLoading = false
                }
            }
        }
    }
    
    func login(username: String, password: String, storeCredentials: Bool, onComplete: @escaping () -> Void) {
        DispatchQueue.global().async {
            self.loginClient.login(username: username, password: password) { response in
                if response.success {
                    if storeCredentials {
                        do {
                            try Keychain.store.storeCredentials(username: username, password: password)
                        } catch {
                            print("Store credentials failed: \(error)")
                        }
                    }
                    DispatchQueue.main.async {
                        self.isLoggedIn = true
                    }
                } else {
                    MessageManager.instance.addMessage(response.message, type: Type.WARN)
                }
                onComplete()
            }
        }
    }
    
    func logout(_ onComplete: @escaping () -> Void) {
        DispatchQueue.main.async {
            self.isLoading = true
        }
        
        DispatchQueue.global().async {
            self.loginClient.logout() { response in
                if response.success {
                    DispatchQueue.main.async {
                        self.loggedOut = true
                        self.isLoggedIn = false
                    }
                } else {
                    MessageManager.instance.addMessage(response.message, type: Type.WARN)
                }
                onComplete()
                DispatchQueue.main.async {
                    self.isLoading = false
                }
            }
        }
    }

}


