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

    @State private var username: String = ""
    @State private var password: String = ""
    @State private var storeCredentials: Bool = false
    @State private var isBackground: Bool = false
    @State private var wait: Bool = false
    @State private var account = ""
    @State private var canAuthenticate = true
    @State private var authenticationFailed = false
    
    var body: some View {
        Form {
            Section {
                VStack(alignment: .leading, spacing: Constants.spacing.normal) {
                    VStack(alignment: .leading, spacing: Constants.spacing.small) {
                        Text("\(Lang.Login.username.localized()):")
                            .font(.title3)
                            .bold()
                        TextField("", text: $username)
                            .accessibilityIdentifier("username")
                            .disableAutocorrection(true)
                            .frame(height: 36)
                            .padding(.horizontal)
                            .overlay(
                                RoundedRectangle(cornerRadius: Constants.radius.small)
                                    .stroke(Color.ui.bw0, lineWidth: 1)
                            )
                    }
                    VStack(alignment: .leading, spacing: Constants.spacing.small) {
                        Text("\(Lang.Login.password.localized()):")
                            .font(.title3)
                            .bold()
                        SecureField("", text: $password)
                            .accessibilityIdentifier("password")
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
                    Text(Lang.Login.login.localized())
                        .font(.title3)
                        .bold()
                        .wait($wait)
                }
                .style(.success, disabled: self.username.count == 0 || self.password.count == 0)
            }
            
            if self.account.isEmpty {
                if self.authenticationFailed {
                    Section {
                        HStack {
                            Spacer()
                            Image(systemName: "faceid")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 50, height: 50)
                                .onTapGesture {
                                    login.autoLogin = true
                                    authenticate()
                                }
                            Spacer()
                        }
                    }
                    .listRowBackground(Color.system.groupedBackground)
                } else if self.canAuthenticate {
                    Section {
                        Toggle(Lang.Login.remember.localized(), isOn: $storeCredentials)
                    }
                }
            } else {
                Section {
                    NavigationLink(destination: AccountView()) {
                        HStack {
                            Text("Account")
                            Spacer()
                            Text(account)
                                .foregroundColor(Color.ui.bw30)
                        }
                    }
                }
            }
        }
        .listStyle(InsetGroupedListStyle())
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
        let accounts: [Credentials]
        do {
            accounts = try login.accounts()
        } catch {
            switch error {
                case AuthenticationError.Unavailable:
                    self.canAuthenticate = false
                    break
                case AuthenticationError.Failed:
                    self.authenticationFailed = true
                    break
                default:
                    break
            }
            self.account = ""
            self.username = ""
            self.password = ""
            return
        }
        if accounts.isEmpty {
            self.account = ""
            self.username = ""
            self.password = ""
            return
        }
        
        let recent = Keychain.getRecent(accounts) ?? accounts[0]
        
        self.account = (recent.alias?.isEmpty ?? true) ? recent.username : recent.alias!
        self.username = recent.username
        self.password = recent.password

        if login.autoLogin && Keychain.autoLogin() {
            login.autoLogin = false
            startLogin()
        }
        
    }
    
}

struct LoginView_Previews: PreviewProvider {
    static var previews: some View {
        LoginView()
    }
}
