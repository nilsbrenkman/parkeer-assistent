//
//  AccountRowView.swift
//  app
//
//  Created by Nils Brenkman on 10/02/2022.
//

import SwiftUI

struct AccountRowView: View {
    
    @EnvironmentObject var login: Login
    
    var credentials: Credentials
    @Binding var list: [Credentials]
    
    @State private var alias = ""
    @State private var username = ""
    @State private var password = ""
    @State private var confirmationShow = false
    
    var body: some View {
        HStack(alignment: .center, spacing: Constants.spacing.normal) {
            VStack {
                HStack {
                    Text(Lang.Login.username.localized())
                    Spacer()
                    TextField("", text: $username, onEditingChanged: updateAccount)
                        .foregroundColor(Color.ui.bw30)
                        .multilineTextAlignment(.trailing)
                }
                
                Divider().padding(.bottom, Constants.spacing.xSmall)
                
                HStack {
                    Text(Lang.Login.password.localized())
                    Spacer()
                    TextField("", text: $password, onEditingChanged: updateAccount)
                        .foregroundColor(Color.ui.bw30)
                        .multilineTextAlignment(.trailing)
                }
                
                Divider().padding(.bottom, Constants.spacing.xSmall)
                
                HStack {
                    Text(Lang.Account.alias.localized())
                    Spacer()
                    TextField("", text: $alias, onEditingChanged: updateAccount)
                        .foregroundColor(Color.ui.bw30)
                        .multilineTextAlignment(.trailing)
                }
            }

            VStack(alignment: .center, spacing: Constants.spacing.large) {
                Button {
                    updateAccount(start: false)
                    login.autoLogin = true
                    Keychain.setRecent(username)
                } label: {
                    Image(systemName: "checkmark.circle")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 24)
                        .foregroundColor(selectColor())
                }
                Button {
                    confirmationShow = true
                } label: {
                    Image(systemName: "trash")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 24)
                        .foregroundColor(Color.ui.danger)
                }
                .alert(isPresented: $confirmationShow) {
                    Alert(
                        title: Text(Lang.Account.confirmDelete.localized()),
                        primaryButton: .destructive(
                            Text(Lang.Common.delete.localized()),
                            action: {
                                login.deleteAccount(credentials)
                                list = (try? login.accounts()) ?? []
                            }
                        ),
                        secondaryButton: .cancel(
                            Text(Lang.Common.cancel.localized()),
                            action: {}
                        )
                    )
                }
            }
        }
        .padding(.all)
        .background(RoundedRectangle(cornerRadius: Constants.radius.normal)
                        .fill(Color.ui.bw100))
        .onAppear {
            self.alias = credentials.alias ?? ""
            self.username = credentials.username
            self.password = credentials.password
        }
        
    }
    
    private func updateAccount(start: Bool) {
        if !start {
            login.updateAccount(credentials, username: username, password: password, alias: alias.isEmpty ? nil : alias)
            list = (try? login.accounts()) ?? []
        }
    }
    
    private func selectColor() -> Color {
        return Keychain.getRecent(list)?.username == credentials.username ? Color.ui.success : Color.ui.disabled
    }
    
}

struct AccountRowView_Previews: PreviewProvider {
    @State private static var list: [Credentials] = []
    static var previews: some View {
        AccountRowView(credentials: Credentials(alias: "My account", username: "1234", password: "0000"), list: $list)
    }
}
