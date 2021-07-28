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
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            
            ZStack {
                Rectangle()
                    .fill(AppColor.header)
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
                    Spacer()
                    if login.isLoggedIn {
                        Button(action: logout){
                            Text("Logout")
                        }
                        .frame(height: 46)
                        .padding(.horizontal)
                        .foregroundColor(Color.white)
                        .overlay(RoundedRectangle(cornerRadius: 6)
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
                        .foregroundColor(AppColor.header)
                        .padding(.vertical, 8)
                    Text("€ \(user.balance!)")
                        .bold()
                        .foregroundColor(AppColor.header)
                        .padding(.vertical, 8)

                }
                .padding(.horizontal)
                
                Rectangle()
                    .frame(height: 1)
                    .border(AppColor.header, width: 1)
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
    static var previews: some View {
        HeaderView()
    }
}
