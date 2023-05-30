//
//  ParkingClient.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 22/06/2021.
//

import Foundation

protocol ParkingClient {
    func get() async throws -> ParkingResponse
    func start(visitor: Visitor, timeMinutes: Int, start: Date, regimeTimeEnd: String) async throws -> Response
    func stop(_ parking: Parking) async throws -> Response
    func history() async throws -> HistoryResponse
}

class ParkingClientApi: ParkingClient {
    
    static let client = ParkingClientApi()
    
    private init() {
        //
    }

    func get() async throws -> ParkingResponse {
        return try await ApiClient.client.call(ParkingResponse.self, path: "parking", method: Method.GET)
    }
    
    func start(visitor: Visitor, timeMinutes: Int, start: Date, regimeTimeEnd: String) async throws -> Response {
        let startTime = Util.dateTimeFormatter.string(from: start)
        let body = AddParkingRequest(visitor: visitor, timeMinutes: timeMinutes, start: startTime, regimeTimeEnd: regimeTimeEnd)
        return try await ApiClient.client.call(Response.self, path: "parking", method: Method.POST, body: body)
    }
    
    func stop(_ parking: Parking) async throws -> Response {
        return try await ApiClient.client.call(Response.self, path: "parking/\(parking.id)", method: Method.DELETE)
    }
    
    func history() async throws -> HistoryResponse {
        return try await ApiClient.client.call(HistoryResponse.self, path: "parking/history", method: Method.GET)
    }

}
