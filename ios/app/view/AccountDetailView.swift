//
//  AccountDetailView.swift
//  app
//
//  Created by Nils Brenkman on 09/12/2023.
//

import SwiftUI

struct AccountDetailView: View {
    
    @Environment(\.presentationMode) var presentationMode: Binding<PresentationMode>

    @EnvironmentObject var login: Login
    
    var account: Credentials
    
    @State private var alias: String = ""
    @State private var username: String = ""
    @State private var password: String = ""
    
    var body: some View {
        Form {
            Section {
                HStack {
                    Text(Lang.Account.alias.localized())
                        .foregroundStyle(Color.ui.bw30)
                        .frame(alignment: .leading)
                    TextField(Lang.Account.alias.localized(), text: $alias)
                        .multilineTextAlignment(.trailing)
                        .frame(maxWidth: .infinity, alignment: .trailing)
                }
                .padding(.vertical, Constants.padding.mini)
                HStack {
                    Text(Lang.Login.username.localized())
                        .foregroundStyle(Color.ui.bw30)
                        .frame(alignment: .leading)
                    TextField(Lang.Login.username.localized(), text: $username)
                        .multilineTextAlignment(.trailing)
                        .frame(maxWidth: .infinity, alignment: .trailing)
                }
                .padding(.vertical, Constants.padding.mini)
                HStack {
                    Text(Lang.Login.password.localized())
                        .foregroundStyle(Color.ui.bw30)
                        .frame(alignment: .leading)
                    TextField(Lang.Login.password.localized(), text: $password)
                        .multilineTextAlignment(.trailing)
                        .frame(maxWidth: .infinity, alignment: .trailing)
                }
                .padding(.vertical, Constants.padding.mini)
            }
            Section {
                Button(action: save) {
                    Text(Lang.Common.save.localized())
                        .font(.title3)
                        .bold()
                        .centered()
                }
                .style(.success, disabled: false)
            }
        }
        .onAppear {
            alias = account.alias ?? ""
            username = account.username
            password = account.password
        }
        .pageTitle(account.username.isEmpty
                       ? Lang.Account.newAccount.localized()
                       : Lang.Account.details.localized(),
                   dismiss: { presentationMode.wrappedValue.dismiss() })
    }
    
    private func save() {
        if account.username.isEmpty {
            login.addAccount(username: username, password: password, alias: alias)
        } else {
            login.updateAccount(account, username: username, password: password, alias: alias)
        }
        presentationMode.wrappedValue.dismiss()
    }
    
}

//#Preview {
//    @State var credentials = Credentials(alias: "alias", username: "user", password: "1234")
//    AccountDetailView(account: $credentials).pr
//}
