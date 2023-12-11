//
//  VisitorClient.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 22/06/2021.
//

import Foundation

protocol VisitorClient {
    func get() async throws -> VisitorResponse
    func add(license: String, name: String) async throws -> Response
    func delete(_ visitor: Visitor) async throws -> Response
}

class VisitorClientApi: VisitorClient {
 
    static let client = VisitorClientApi()
    
    private init() {
        //
    }

    func get() async throws -> VisitorResponse {
        try await ApiClient.client.call(VisitorResponse.self, path: "visitor", method: Method.GET)
    }
    
    func add(license: String, name: String) async throws -> Response {
        let body = AddVisitorRequest(license: license, name: name)
        return try await ApiClient.client.call(Response.self, path: "visitor", method: Method.POST, body: body)
    }
    
    func delete(_ visitor: Visitor) async throws -> Response {
        try await ApiClient.client.call(Response.self, path: "visitor/\(visitor.id)", method: Method.DELETE)
    }

}
