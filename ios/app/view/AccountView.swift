//
//  AccountView.swift
//  app
//
//  Created by Nils Brenkman on 10/02/2022.
//

import SwiftUI
import LocalAuthentication

@MainActor
struct AccountView: View {
    
    @Environment(\.presentationMode) var presentationMode: Binding<PresentationMode>
    
    @EnvironmentObject var login: Login
    @EnvironmentObject var user: User
    
    @State private var autoLogin: Bool = false
    @State private var newAccount: Bool = false
    
    var body: some View {
        Form {
            Section {
                List {
                    ForEach(login.accounts) { _account in
                        NavigationLink(destination: AccountDetailView(account: _account)) {
                            Text(_account.alias ?? _account.username)
                        }
                        .padding(.vertical, Constants.padding.mini)
                    }
                    .onDelete { index in
                        for i in index {
                            login.deleteAccount(login.accounts[i])
                        }
                    }
                }
            }
            Section {
                Button(action: { newAccount.toggle() }) {
                    ZStack {
                        NavigationLink(destination: AccountDetailView(account: Credentials(username: "", password: "")),
                                       isActive: $newAccount) {
                            EmptyView()
                        }
                        .hidden()
                        Text(Lang.Common.add.localized())
                            .font(.title3)
                            .bold()
                    }
                }
                .style(.success, disabled: false)
            }
        }
        .background(Color.system.groupedBackground.ignoresSafeArea())
        .onAppear(perform: load)
        .pageTitle(Lang.Account.header.localized(), dismiss: { user.page = nil })
    }
    
    private func load() {
        do {
            _ = try login.loadAccounts()
        } catch {
            switch error {
            case AuthenticationError.Unavailable:
                MessageManager.instance.addMessage(Lang.Account.errorUnavailable.localized(), type: Type.ERROR)
                break
            case AuthenticationError.Failed:
                MessageManager.instance.addMessage(Lang.Account.errorFailed.localized(), type: Type.WARN)
                break
            default:
                break
            }
            self.presentationMode.wrappedValue.dismiss()
        }
        self.autoLogin = Keychain.autoLogin()
    }
}
//
//struct AccountView_Previews: PreviewProvider {
//    static var previews: some View {
//        AccountView()
//    }
//}
