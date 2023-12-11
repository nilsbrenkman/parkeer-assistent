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
    @State private var canAuthenticate = true
    @State private var authenticationFailed = false
    @State private var selectedAccount: Credentials = Credentials(username: "", password: "")
    
    var body: some View {
        Form {
            Section(header: SectionHeader(Lang.Login.login.localized())) {
                HStack {
                    Text(Lang.Login.username.localized())
                        .foregroundStyle(Color.ui.bw30)
                        .frame(alignment: .leading)
                    TextField("", text: $username)
                        .multilineTextAlignment(.trailing)
                        .frame(maxWidth: .infinity, alignment: .trailing)
                }
                .padding(.vertical, Constants.padding.mini)
                HStack {
                    Text(Lang.Login.password.localized())
                        .foregroundStyle(Color.ui.bw30)
                        .frame(alignment: .leading)
                    SecureField("", text: $password)
                        .multilineTextAlignment(.trailing)
                        .frame(maxWidth: .infinity, alignment: .trailing)
                }
                .padding(.vertical, Constants.padding.mini)

                
//                VStack(alignment: .leading, spacing: Constants.spacing.normal) {
//                    VStack(alignment: .leading, spacing: Constants.spacing.small) {
//                        Text("\(Lang.Login.username.localized()):")
//                            .font(.title3)
//                            .bold()
//                        TextField("", text: $username)
//                            .accessibilityIdentifier("username")
//                            .disableAutocorrection(true)
//                            .frame(height: 36)
//                            .padding(.horizontal)
//                            .overlay(
//                                RoundedRectangle(cornerRadius: Constants.radius.small)
//                                    .stroke(Color.ui.bw0, lineWidth: 1)
//                            )
//                    }
//                    VStack(alignment: .leading, spacing: Constants.spacing.small) {
//                        Text("\(Lang.Login.password.localized()):")
//                            .font(.title3)
//                            .bold()
//                        SecureField("", text: $password)
//                            .accessibilityIdentifier("password")
//                            .frame(height: 36)
//                            .padding(.horizontal)
//                            .overlay(
//                                RoundedRectangle(cornerRadius: Constants.radius.small)
//                                    .stroke(Color.ui.bw0, lineWidth: 1)
//                            )
//                    }
//                }
//                .padding(.vertical)
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
            if login.accounts.isEmpty {
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
                    Picker(Lang.Account.label.localized(), selection: $selectedAccount) {
                        ForEach(login.accounts) { _account in
                            Text(_account.alias ?? _account.username).tag(_account)
                        }
                    }
                    .onChange(of: selectedAccount, perform: changeAccount)
                }
            }
        }
        .listStyle(.insetGrouped)
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
            Task {
                wait = true
                await login.login(username: username,
                                  password: password,
                                  storeCredentials: storeCredentials)
                wait = false
            }
        }
    }
    
    private func authenticate() {
        do {
            let accounts = try login.loadAccounts()
            self.selectedAccount = Keychain.getRecent(accounts) ?? Credentials(username: "", password: "")
            changeAccount(selectedAccount)
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
            self.username = ""
            self.password = ""
            return
        }
                
        if login.autoLogin && Keychain.autoLogin() {
            login.autoLogin = false
            startLogin()
        }
    }
    
    private func changeAccount(_ account: Credentials) {
        Keychain.setRecent(account.username)
        self.username = account.username
        self.password = account.password
    }

}

struct LoginView_Previews: PreviewProvider {
    static var previews: some View {
        LoginView()
    }
}
