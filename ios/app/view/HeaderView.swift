//
//  HeaderView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 30/06/2021.
//

import SwiftUI

struct HeaderView: View {
    
    @EnvironmentObject var login: Login
    @EnvironmentObject var user: User
    @EnvironmentObject var payment: Payment
    
    @Binding var showInfo: Bool
    @Binding var showHistory: Bool
    @Binding var showAccounts: Bool
    @Binding var showSettings: Bool
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            
            ZStack {
                Rectangle()
                    .fill(Color.ui.header)
                    .frame(height: 68)
                HStack {
                    if !login.isLoggedIn {
                        Spacer()
                    }
                    Image("Image-logo")
                        .resizable()
                        .scaledToFit()
                        .frame(height: 48)
                        .animation(.linear, value: 0)
                        .onTapGesture {
                            showInfo = true
                        }
                    Spacer()
                    if login.isLoggedIn {
                        Menu {
                            Button(action: { showHistory = true }) {
                                Text(Lang.Parking.history.localized())
                                Image(systemName: "clock")
                            }
                            Button(action: showPayment) {
                                Text(Lang.User.addBalance.localized())
                                Image(systemName: "eurosign.circle")
                            }
                            Button(action: { showAccounts = true }) {
                                Text("Accounts")
                                Image(systemName: "person.circle")
                            }
                            Button(action: { showSettings = true }) {
                                Text("Settings")
                                Image(systemName: "gearshape.circle")
                            }
                            Button(action: logout) {
                                Text(Lang.Login.logout.localized())
                                Image(systemName: "square.and.arrow.up")
                            }
                        } label: {
                            Image(systemName: "list.bullet")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 32, height: 38)
                                .padding(.vertical, 5)
                                .padding(.horizontal, 8)
                                .foregroundColor(Color.white)
                                .overlay(RoundedRectangle(cornerRadius: Constants.radius.small)
                                            .stroke(Color.white, lineWidth: 1)
                                )
                                .accessibilityIdentifier("menu")

                        }
                    }
                }
                .padding(.horizontal)
            }
            
            if login.isLoggedIn && user.balance != nil {

                HStack {
                    Spacer()
                    Text("\(Lang.User.balance.localized()):")
                        .foregroundColor(Color.ui.header)
                        .padding(.vertical, 8)
                    Text("€ \(user.balance!)")
                        .bold()
                        .foregroundColor(Color.ui.header)
                        .padding(.vertical, 8)
                        .accessibilityIdentifier("balance")

                }
                .padding(.horizontal)
                .background(Color.ui.light)

                Rectangle()
                    .frame(height: 1)
                    .border(Color.ui.header, width: 1)
            }
                        
        }
    }
    
    private func showPayment() {
        payment.show = true
        user.selectedVisitor = nil
        user.addVisitor = false
    }
    
    private func logout() {
        Task {
            await login.logout()
        }
    }

}

struct HeaderView_Previews: PreviewProvider {
    @State static var showInfo = false
    @State static var showHistory = false
    @State static var showSettings = false
    static var previews: some View {
        HeaderView(showInfo: $showInfo, showHistory: $showHistory, showAccounts: $showHistory, showSettings: $showSettings)
//            .setupPreview(loggedIn: true)
    }
}
