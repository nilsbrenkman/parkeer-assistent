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
    
    @State private var accounts: [Credentials] = []
    @State private var autoLogin: Bool = false
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: Constants.spacing.xLarge) {
                ForEach($accounts.wrappedValue, id: \.username) { account in
                    AccountRowView(credentials: account, list: $accounts)
                }
                
                Button(action: {
                    login.addAccount()
                    self.accounts = (try? login.accounts()) ?? []
                }) {
                    Text(Lang.Common.add.localized())
                        .font(.title3)
                        .bold()
                        .centered()
                }
                .style(.success)
                .padding(.vertical, Constants.padding.small)
                .background(RoundedRectangle(cornerRadius: Constants.radius.normal)
                                .fill(Color.ui.success))
                
                Toggle(Lang.Account.autoLogin.localized(), isOn: $autoLogin)
                    .onChange(of: autoLogin) { toggle in
                        Keychain.autoLogin(enabled: toggle)
                    }
                    .padding(.vertical, Constants.padding.small)
                    .padding(.horizontal)
                    .background(RoundedRectangle(cornerRadius: Constants.radius.normal)
                                    .fill(Color.system.groupedRowBackground))
                
            }
            .padding(.all)
        }
        .background(Color.system.groupedBackground.ignoresSafeArea())
        .onAppear(perform: load)
        .navigationBarTitle(Text(Lang.Account.header.localized()))
        .navigationBarBackButtonHidden(true)
        .navigationBarItems(leading:
            Button(action: { self.presentationMode.wrappedValue.dismiss() }) {
                Image(systemName: "arrow.left")
            }
        )
    }
    
    private func load() {
        do {
            self.accounts = try login.accounts()
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

struct AccountView_Previews: PreviewProvider {
    static var previews: some View {
        AccountView()
    }
}
