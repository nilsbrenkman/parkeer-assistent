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
        }
        .background(Color.system.groupedBackground.ignoresSafeArea())
        .onAppear(perform: load)
        .navigationBarTitle(Text(Lang.Account.header.localized()))
        .navigationBarBackButtonHidden(true)
        .navigationBarItems(
            leading: Button(action: { self.presentationMode.wrappedValue.dismiss() }) {
                Image(systemName: "arrow.left")
            },
            trailing: Button(action: { newAccount.toggle() }) {
                ZStack {
                    NavigationLink(destination: AccountDetailView(account: Credentials(username: "", password: "")),
                                   isActive: $newAccount) {
                        EmptyView()
                    }
                                   .hidden()
                    Image(systemName: "plus")
                }
            }
        )
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
