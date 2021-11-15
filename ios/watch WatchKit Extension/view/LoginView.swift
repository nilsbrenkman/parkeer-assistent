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
        guard let credentials = Keychain.store.retrieveCredentials() else {
            return
        }

        self.username = credentials.username
        self.password = credentials.password
    }
}

struct LoginView_Previews: PreviewProvider {
    static var previews: some View {
        LoginView()
    }
}
