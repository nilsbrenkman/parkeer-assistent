//
//  ParkingClient.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 22/06/2021.
//

import Foundation

protocol ParkingClient {
    func get(onComplete: @escaping (ParkingResponse) -> Void)
    func start(visitor: Visitor, timeMinutes: Int, start: Date, regimeTimeEnd: String, onComplete: @escaping (Response) -> Void)
    func stop(parkingId: Int, onComplete: @escaping (Response) -> Void)
    func history(onComplete: @escaping (HistoryResponse) -> Void)
}

class ParkingClientApi: ParkingClient {
    
    static let client = ParkingClientApi()
    
    private init() {
        //
    }

    func get(onComplete: @escaping (ParkingResponse) -> Void) {
        do {
            try ApiClient.client.call(ParkingResponse.self, path: "parking", method: Method.GET, onComplete: onComplete)
        } catch {
            print("Error: \(error)")
        }
    }
    
    func start(visitor: Visitor, timeMinutes: Int, start: Date, regimeTimeEnd: String, onComplete: @escaping (Response) -> Void) {
        let startTime = Util.dateTimeFormatter.string(from: start)
        let body = AddParkingRequest(visitor: visitor, timeMinutes: timeMinutes, start: startTime, regimeTimeEnd: regimeTimeEnd)
        do {
            try ApiClient.client.call(Response.self, path: "parking", method: Method.POST, body: body, onComplete: onComplete)
        } catch {
            print("Error: \(error)")
            onComplete(Response(success: false, message: "Client error"))
        }
    }
    
    func stop(parkingId: Int, onComplete: @escaping (Response) -> Void) {
        do {
            try ApiClient.client.call(Response.self, path: "parking/\(parkingId)", method: Method.DELETE, onComplete: onComplete)
        } catch {
            print("Error: \(error)")
            onComplete(Response(success: false, message: "Client error"))
        }
    }
    
    func history(onComplete: @escaping (HistoryResponse) -> Void) {
        do {
            try ApiClient.client.call(HistoryResponse.self, path: "parking/history", method: Method.GET, onComplete: onComplete)
        } catch {
            print("Error: \(error)")
            onComplete(HistoryResponse(history: []))
        }
    }

}

class ParkingClientMock: ParkingClient {
      
    static let client = ParkingClientMock()
    
    public var parking: [Int:Parking] = [:]

    private var nextId = 0
    private let dateFormatter = Util.dateTimeFormatter
    private let hourRate = 2.1

    private init() {
        //
    }

    func get(onComplete: @escaping (ParkingResponse) -> Void) {
        MockClient.mockDelay()
        
        onComplete(ParkingResponse(
            active: Array(parking.values.filter({ parking in
                guard let startDate = try? Util.parseDate(parking.startTime),
                      let endDate = try? Util.parseDate(parking.endTime) else {
                    return false
                }
                return startDate < Date.now() && endDate > Date.now()
            })),
            scheduled: Array(parking.values.filter({ parking in
                guard let startDate = try? Util.parseDate(parking.startTime),
                      let endDate = try? Util.parseDate(parking.endTime) else {
                    return false
                }
                return startDate > Date.now() && endDate > Date.now()
            }))
        ))
    }
    
    func start(visitor: Visitor, timeMinutes: Int, start: Date, regimeTimeEnd: String, onComplete: @escaping (Response) -> Void) {
        MockClient.mockDelay()
        
        if visitor.name == "Invalid visitor" {
            onComplete(Response(success: false, message: "Visitor is not allowed to park"))
            return
        }
        
        let cost = Double(timeMinutes) * hourRate / 60.0
        if cost > getBalance() {
            onComplete(Response(success: false, message: "Not enough balance"))
            return
        }
        
        nextId += 1
        let timeInterval = 60 * Double(timeMinutes)
        let end = start.addingTimeInterval(timeInterval)
        
        let p = Parking(id: nextId,
                        license: visitor.license,
                        startTime: dateFormatter.string(from: start),
                        endTime: dateFormatter.string(from: end),
                        cost: cost)
        parking[p.id] = p
        onComplete(Response(success: true))
    }
    
    func stop(parkingId: Int, onComplete: @escaping (Response) -> Void) {
        MockClient.mockDelay()

        if let p = parking[parkingId] {
            guard let startDate = try? Util.parseDate(p.startTime) else {
                onComplete(Response(success: false, message: "Invalid start time"))
                return
            }
            if startDate > Date.now() {
                parking.removeValue(forKey: parkingId)
            } else {
                let minutes = startDate.timeIntervalSinceNow / -60.0
                let cost = minutes * hourRate / 60.0
                parking.updateValue(Parking(id: p.id,
                                            license: p.license,
                                            startTime: p.startTime,
                                            endTime: dateFormatter.string(from: Date.now()),
                                            cost: cost),
                                    forKey: parkingId)
            }
            onComplete(Response(success: true))
        } else {
            onComplete(Response(success: false, message: "Not found"))
        }
    }
    
    func history(onComplete: @escaping (HistoryResponse) -> Void) {
        MockClient.mockDelay()

        onComplete(HistoryResponse(history: Array(parking.values.filter({
            guard let endDate = try? Util.parseDate($0.endTime) else {
                return false
            }
            return endDate < Date.now()
        }).sorted(by: {
            try! Util.parseDate($0.startTime) > Util.parseDate($1.startTime)
        }).map { p in
            let visitor = MockVisitor.visitors.first(where: { $0.license == p.license })
            return History(id: p.id, license: p.license, name: visitor?.name, startTime: p.startTime, endTime: p.endTime, cost: p.cost)
        })))
    }
    
    func getBalance() -> Double {
        var balance = 10.0
        for t in PaymentClientMock.client.transactions.values {
            if PaymentClientMock.getTransactionStatus(t) == "success" {
                let amount = Double(t.amount.replacingOccurrences(of: ",", with: ".")) ?? 0.0
                balance += amount
            }
        }
        for p in parking.values {
            balance -= p.cost
        }
        return balance
    }

}
