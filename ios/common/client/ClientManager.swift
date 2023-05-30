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
