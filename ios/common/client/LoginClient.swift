//
//  LoginClient.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 22/06/2021.
//

import Foundation

protocol LoginClient {
    func loggedId(onComplete: @escaping (Response) -> Void)
    func login(username: String, password: String, onComplete: @escaping (Response) -> Void)
    func logout(onComplete: @escaping (Response) -> Void)
}

class LoginClientApi: LoginClient {
 
    static let client = LoginClientApi()
   
    private init() {
        //
    }

    func loggedId(onComplete: @escaping (Response) -> Void) {
        ApiClient.client.call(Response.self, path: "login", method: Method.GET, onComplete: onComplete)
    }
    
    func login(username: String, password: String, onComplete: @escaping (Response) -> Void) {
        let body = LoginRequest(username: username, password: password)
        ApiClient.client.call(Response.self, path: "login", method: Method.POST, body: body, onComplete: onComplete)
    }
    
    func logout(onComplete: @escaping (Response) -> Void) {
        ApiClient.client.call(Response.self, path: "logout", method: Method.GET, onComplete: onComplete)
    }
    
}

class LoginClientMock: LoginClient {

    static let client = LoginClientMock()


    private init() {
        //
    }

    func loggedId(onComplete: @escaping (Response) -> Void) {
        MockClient.mockDelay()

        onComplete(Response(success: MockClient.client.isLoggedIn()))
    }
    
    func login(username: String, password: String, onComplete: @escaping (Response) -> Void) {
        MockClient.mockDelay()

        if MockClient.client.isLoggedIn() {
            onComplete(Response(success: false, message: "Already logged in"))
        } else {
            if password.count != 4 {
                onComplete(Response(success: false, message: "Login failed"))
                return
            }
            MockClient.client.login(true)
            onComplete(Response(success: true))
        }
    }
    
    func logout(onComplete: @escaping (Response) -> Void) {
        MockClient.mockDelay()

        if MockClient.client.isLoggedIn() {
            MockClient.client.login(false)
            onComplete(Response(success: true))
        } else {
            onComplete(Response(success: false, message: "Not logged in"))
        }
    }
    
}
