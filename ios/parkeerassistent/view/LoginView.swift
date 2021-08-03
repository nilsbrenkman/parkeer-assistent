//
//  LoginView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 12/06/2021.
//

import SwiftUI
import LocalAuthentication

struct LoginView: View {
    
    @Environment(\.scenePhase) private var scenePhase

    @EnvironmentObject var login: Login
    @EnvironmentObject var messenger: Messenger

    @State private var username: String = ""
    @State private var password: String = ""
    @State private var storeCredentials: Bool = false
    @State private var isBackground: Bool = false

    @State private var wait: Bool = false

    var body: some View {
        Form {
            Section {
                VStack(alignment: .leading, spacing: Constants.spacing.normal) {
                    VStack(alignment: .leading, spacing: Constants.spacing.small) {
                        Text("Meldcode:")
                            .font(.title3)
                            .bold()
                        TextField("", text: $username)
                            .disableAutocorrection(true)
                            .frame(height: 36)
                            .padding(.horizontal)
                            .overlay(
                                RoundedRectangle(cornerRadius: Constants.radius.small)
                                    .stroke(Color.ui.bw0, lineWidth: 1)
                            )
                    }
                    VStack(alignment: .leading, spacing: Constants.spacing.small) {
                        Text("Pincode:")
                            .font(.title3)
                            .bold()
                        SecureField("", text: $password)
                            .frame(height: 36)
                            .padding(.horizontal)
                            .overlay(
                                RoundedRectangle(cornerRadius: Constants.radius.small)
                                    .stroke(Color.ui.bw0, lineWidth: 1)
                            )
                    }
                }
                .padding(.vertical)
            }
            Section {
                Button(action: startLogin){
                    Text("Inloggen")
                        .font(.title3)
                        .bold()
                        .wait($wait)
                }
                .buttonStyle(ButtonStyles.Enabled(main: Color.ui.success,
                                                  disabled: Color.ui.successDisabled,
                                                  enabled: self.username.count > 0 && self.password.count > 0))
            }
            Section {
                Toggle("Onthoud inlog gegevens", isOn: $storeCredentials)
            }
        }
        .onAppear(perform: authenticate)
        .onChange(of: scenePhase) { phase in
            if phase == .background {
                isBackground = true
            } else if isBackground {
                isBackground = false
                if username.isEmpty && password.isEmpty {
                    authenticate()
                }
            }
        }
    }
    
    private func startLogin() {
        if !wait {
            wait = true
            login.login(username: username, password: password, storeCredentials: storeCredentials) {
                wait = false
            }
        }
    }
    
    private func authenticate() {
        let context = LAContext()
        var error: NSError?

        if context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) {
            guard let credentials = Keychain.store.retrieveCredentials() else {
                return
            }
            
            let reason = "Opgeslagen inlog gegevens laden"

            context.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, localizedReason: reason) { success, authenticationError in
                DispatchQueue.main.async {
                    if success {
                        self.username = credentials.username
                        self.password = credentials.password
                        
                        if login.loggedOut {
                            login.loggedOut = false
                            return
                        }
                      
                        startLogin()
                    }
                }
            }
        }
    }
    
}

struct LoginView_Previews: PreviewProvider {
    static var previews: some View {
        LoginView()
    }
}
