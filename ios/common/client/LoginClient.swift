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
        do {
            try ApiClient.client.call(Response.self, path: "login", method: Method.GET, onComplete: onComplete)
        } catch {
            print("Error: \(error)")
            onComplete(Response(success: false, message: "Client error"))
        }
    }
    
    func login(username: String, password: String, onComplete: @escaping (Response) -> Void) {
        let body = LoginRequest(username: username, password: password)
        do {
            try ApiClient.client.call(Response.self, path: "login", method: Method.POST, body: body, onComplete: onComplete)
        } catch {
            print("Error: \(error)")
            onComplete(Response(success: false, message: "Client error"))
        }
    }
    
    func logout(onComplete: @escaping (Response) -> Void) {
        do {
            try ApiClient.client.call(Response.self, path: "logout", method: Method.GET, onComplete: onComplete)
        } catch {
            print("Error: \(error)")
            onComplete(Response(success: false, message: "Client error"))
        }
    }
    
}

class LoginClientMock: LoginClient {

    static let client = LoginClientMock()

    private var loggedIn = false
    private var timeout = Date()

    private init() {
        //
    }

    func loggedId(onComplete: @escaping (Response) -> Void) {
        MockClient.mockDelay()

        let delay = Date(timeIntervalSinceNow: -60)
        if delay > timeout {
            loggedIn = false
            onComplete(Response(success: false, message: "Timeout"))
            return
        }

        timeout = Date()
        onComplete(Response(success: loggedIn))
    }
    
    func login(username: String, password: String, onComplete: @escaping (Response) -> Void) {
        MockClient.mockDelay()

        if loggedIn {
            onComplete(Response(success: false, message: "Already logged in"))
        } else {
            if password.count != 4 {
                onComplete(Response(success: false, message: "Login failed"))
                return
            }
            loggedIn = true
            timeout = Date()
            onComplete(Response(success: true))
        }
    }
    
    func logout(onComplete: @escaping (Response) -> Void) {
        MockClient.mockDelay()

        if loggedIn {
            loggedIn = false
            onComplete(Response(success: true))
        } else {
            onComplete(Response(success: false, message: "Not logged in"))
        }
    }
    
}
