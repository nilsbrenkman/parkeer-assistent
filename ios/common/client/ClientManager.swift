//
//  ClientManager.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 22/06/2021.
//

import Foundation

public class ClientManager {
    
    static let instance = ClientManager()
    
    var clients: [String: Any] = [:]
    
    private init() {
        //
    }
    
    func register<Client>(_ type: Client.Type, client: Any) {
        clients["\(type)"] = client
     }

    func get<Client>(_ type: Client.Type) throws -> Client  {
        if clients["\(type)"] == nil {
            throw ClientManagerError.NotRegistered
        }
        if let client = clients["\(type)"] as? Client {
            return client
        }
        throw ClientManagerError.InvalidType
     }

}

enum ClientManagerError: Error {
    case NotRegistered
    case InvalidType
}

class MockClient {
    static func mockDelay() {
        do {
            usleep(250000)
        }
    }
    
    static let client = MockClient()

    private var loggedIn = false
    private var timeout = Date()
    private var startup = true

    private init() {
        //
    }
    
    func authorized() -> Bool {
        MockClient.mockDelay()
        if startup || isLoggedIn() {
            return true
        }
        ApiClient.client.throwError(.Unauthorized)
        return false
    }
    
    func login(_ login: Bool) {
        loggedIn = login
        timeout = Date()
    }
    
    func isLoggedIn() -> Bool {
        let delay = Date(timeIntervalSinceNow: -15 * 60)
        if delay > timeout {
            loggedIn = false
        } else {
            timeout = Date()
        }
        return loggedIn
    }
    
    func startupComplete() {
        startup = false
    }
    
}
