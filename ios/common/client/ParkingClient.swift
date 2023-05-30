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

class ParkingClientMock: ParkingClient {
      
    static let client = ParkingClientMock()
    
    public var parkingSessions: [Int:Parking] = [:]

    private var nextId = 0
    private let dateFormatter = Util.dateTimeFormatter
    private let hourRate = 2.1

    private init() {
        //
    }
    
    func get() async throws -> ParkingResponse {
        guard MockClient.client.authorized() else { throw ClientError.Unauthorized }
        
        return ParkingResponse(
            active: Array(parkingSessions.values.filter({ parking in
                guard let startDate = try? Util.parseDate(parking.startTime),
                      let endDate = try? Util.parseDate(parking.endTime) else {
                    return false
                }
                return startDate < Date.now() && endDate > Date.now()
            })),
            scheduled: Array(parkingSessions.values.filter({ parking in
                guard let startDate = try? Util.parseDate(parking.startTime),
                      let endDate = try? Util.parseDate(parking.endTime) else {
                    return false
                }
                return startDate > Date.now() && endDate > Date.now()
            }))
        )
    }
    
    func start(visitor: Visitor, timeMinutes: Int, start: Date, regimeTimeEnd: String) async throws -> Response {
        guard MockClient.client.authorized() else { throw ClientError.Unauthorized }
        
        Log.debug("Adding session: \(visitor.formattedLicense)")
        
        if visitor.name == "Invalid visitor" {
            return Response(success: false, message: "Visitor is not allowed to park")
        }
        
        let cost = Double(timeMinutes) * hourRate / 60.0
        if cost > getBalance() {
            return Response(success: false, message: "Not enough balance")
        }
        
        nextId += 1
        let timeInterval = 60 * Double(timeMinutes)
        let end = start.addingTimeInterval(timeInterval)
        
        let p = Parking(id: nextId,
                        license: visitor.license,
                        startTime: dateFormatter.string(from: start),
                        endTime: dateFormatter.string(from: end),
                        cost: cost)
        parkingSessions[p.id] = p
        return Response(success: true)
    }
    
    func stop(_ parking: Parking) async throws -> Response {
        guard MockClient.client.authorized() else { throw ClientError.Unauthorized }

        if let p = parkingSessions[parking.id] {
            guard let startDate = try? Util.parseDate(p.startTime) else {
                return Response(success: false, message: "Invalid start time")
            }
            if startDate > Date.now() {
                parkingSessions.removeValue(forKey: parking.id)
            } else {
                let minutes = startDate.timeIntervalSinceNow / -60.0
                let cost = minutes * hourRate / 60.0
                parkingSessions.updateValue(Parking(id: p.id,
                                                    license: p.license,
                                                    startTime: p.startTime,
                                                    endTime: dateFormatter.string(from: Date.now()),
                                                    cost: cost),
                                            forKey: parking.id)
            }
            return Response(success: true)
        } else {
            return Response(success: false, message: "Not found")
        }
    }
    
    func history() async throws -> HistoryResponse {
        guard MockClient.client.authorized() else { throw ClientError.Unauthorized }

        return HistoryResponse(history: Array(parkingSessions.values.filter({
            guard let endDate = try? Util.parseDate($0.endTime) else {
                return false
            }
            return endDate < Date.now()
        }).sorted(by: {
            try! Util.parseDate($0.startTime) > Util.parseDate($1.startTime)
        }).map { p in
            let visitor = MockVisitor.visitors.first(where: { $0.license == p.license })
            return History(id: p.id, license: p.license, name: visitor?.name, startTime: p.startTime, endTime: p.endTime, cost: p.cost)
        }))
    }
    
    func getBalance() -> Double {
        var balance = 10.0
        for t in PaymentClientMock.client.transactions.values {
            if PaymentClientMock.getTransactionStatus(t) == "success" {
                let amount = Double(t.amount.replacingOccurrences(of: ",", with: ".")) ?? 0.0
                balance += amount
            }
        }
        for p in parkingSessions.values {
            balance -= p.cost
        }
        return balance
    }

}
