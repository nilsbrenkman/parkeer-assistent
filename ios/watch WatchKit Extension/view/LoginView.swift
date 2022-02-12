//
//  LoginView.swift
//  watch WatchKit Extension
//
//  Created by Nils Brenkman on 16/10/2021.
//

import SwiftUI

struct LoginView: View {
    
    @EnvironmentObject var login: Login
    
    @State private var username: String = ""
    @State private var password: String = ""
    @State private var storeCredentials: Bool = false
    
    var body: some View {
        VStack {
            TextField("Meldcode", text: $username)
            SecureField("Pincode", text: $password)
            Button(action: startLogin) {
                Text("Login")
            }
            Toggle(Lang.Login.rememberShort.localized(), isOn: $storeCredentials)
        }
        .onAppear(perform: retrieveCredentials)
    }
    
    private func startLogin() {
        login.login(username: username, password: password, storeCredentials: storeCredentials) {
            //
        }
    }
    
    private func retrieveCredentials() {
        let credentials = Keychain.retrieveCredentials()
        if credentials.isEmpty {
            return
        }

        self.username = credentials[0].username
        self.password = credentials[0].password
    }
}

struct LoginView_Previews: PreviewProvider {
    static var previews: some View {
        LoginView()
    }
}
