//
//  LoginClient.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 22/06/2021.
//

import Foundation

protocol LoginClient {
    func loggedId() async throws -> Response
    func login(username: String, password: String) async throws -> Response
    func logout() async throws -> Response
}

class LoginClientApi: LoginClient {
 
    static let client = LoginClientApi()
   
    private init() {
        //
    }

    func loggedId() async throws -> Response {
        return try await ApiClient.client.call(Response.self, path: "login", method: Method.GET)
    }
    
    func login(username: String, password: String) async throws -> Response {
        let body = LoginRequest(username: username, password: password)
        return try await ApiClient.client.call(Response.self, path: "login", method: Method.POST, body: body)
    }
    
    func logout() async throws -> Response {
        return try await ApiClient.client.call(Response.self, path: "logout", method: Method.GET)
    }
    
}

class LoginClientMock: LoginClient {

    static let client = LoginClientMock()


    private init() {
        //
    }

    func loggedId() async throws -> Response {
        MockClient.mockDelay()

        return Response(success: MockClient.client.isLoggedIn())
    }
    
    func login(username: String, password: String) async throws -> Response {
        MockClient.mockDelay()

        if MockClient.client.isLoggedIn() {
            return Response(success: false, message: "Already logged in")
        } else {
            if password.count != 4 {
                return Response(success: false, message: "Login failed")
            }
            MockClient.client.login(true)
            return Response(success: true)
        }
    }
    
    func logout() async throws -> Response {
        MockClient.mockDelay()

        if MockClient.client.isLoggedIn() {
            MockClient.client.login(false)
            return Response(success: true)
        } else {
            return Response(success: false, message: "Not logged in")
        }
    }
    
}
