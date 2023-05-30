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
