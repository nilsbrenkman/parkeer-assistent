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
    
    @Binding var showInfo: Bool
    
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
                    Image("logo")
                        .resizable()
                        .scaledToFit()
                        .frame(height: 48)
                        .animation(.linear)
                        .onTapGesture {
                            showInfo = true
                        }
                    Spacer()
                    if login.isLoggedIn {
                        Button(action: logout){
                            Text("Logout")
                        }
                        .frame(height: 46)
                        .padding(.horizontal)
                        .foregroundColor(Color.white)
                        .overlay(RoundedRectangle(cornerRadius: Constants.radius.small)
                                    .stroke(Color.white, lineWidth: 1)
                        )
                    }
                }
                .padding(.horizontal)
            }
            
            if login.isLoggedIn && user.balance != nil {

                HStack {
                    Spacer()
                    Text("Saldo:")
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
    
    private func logout() {
        login.logout() {
            DispatchQueue.main.async {
                user.selectedVisitor = nil
                user.addVisitor = false
                user.isLoaded = false
            }
        }
    }
    
}

struct HeaderView_Previews: PreviewProvider {
    @State static var showInfo = false
    static var previews: some View {
        HeaderView(showInfo: $showInfo)
    }
}
