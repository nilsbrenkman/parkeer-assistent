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
        return try await ApiClient.client.call(VisitorResponse.self, path: "visitor", method: Method.GET)
    }
    
    func add(license: String, name: String) async throws -> Response {
        let body = AddVisitorRequest(license: license, name: name)
        return try await ApiClient.client.call(Response.self, path: "visitor", method: Method.POST, body: body)
    }
    
    func delete(_ visitor: Visitor) async throws -> Response {
        return try await ApiClient.client.call(Response.self, path: "visitor/\(visitor.id)", method: Method.DELETE)
    }

}

class VisitorClientMock: VisitorClient {
    
    static let client = VisitorClientMock()
    
    private var nextId = 0
    private var visitors: [Int:Visitor] = [:]
    private let permitId = 999

    private init() {
        for visitor in MockVisitor.visitors {
            Log.debug("Adding visitor: \(visitor.name)")
            nextId += 1
            let visitor = Visitor(visitorId: nextId,
                                  permitId: permitId,
                                  license: visitor.license,
                                  formattedLicense: License.formatLicense(visitor.license),
                                  name: visitor.name.count > 0 ? visitor.name : nil)
            visitors[visitor.visitorId] = visitor
        }
        
        let semaphore = DispatchSemaphore(value: 0)

        Task {
            var regime = try! await UserClientMock.client.regime(Date.now())
            Log.debug("regime: \(regime.regimeTimeStart) - \(regime.regimeTimeEnd)")
            _ = try! await ParkingClientMock.client.start(visitor: self.visitors[1]!, timeMinutes: 1, start: Date.now(), regimeTimeEnd: regime.regimeTimeEnd)
            _ = try! await ParkingClientMock.client.start(visitor: self.visitors[2]!, timeMinutes: 10, start: Date.now().addingTimeInterval(10.0), regimeTimeEnd: regime.regimeTimeEnd)
            
            regime = try! await UserClientMock.client.regime(Date(timeInterval: 24*60*60, since: Date.now()))
            Log.debug("regime: \(regime.regimeTimeStart) - \(regime.regimeTimeEnd)")
            _ = try! await ParkingClientMock.client.start(visitor: self.visitors[2]!, timeMinutes: 10, start: Util.parseDate(regime.regimeTimeStart), regimeTimeEnd: regime.regimeTimeEnd)
            
            regime = try! await UserClientMock.client.regime(Date(timeInterval: -24*60*60, since: Date.now()))
            Log.debug("regime: \(regime.regimeTimeStart) - \(regime.regimeTimeEnd)")
            _ = try! await ParkingClientMock.client.start(visitor: self.visitors[2]!, timeMinutes: 60, start: Util.parseDate(regime.regimeTimeStart), regimeTimeEnd: regime.regimeTimeEnd)
            
            semaphore.signal()
        }
        semaphore.wait()
    }
    
    func get() async throws -> VisitorResponse {
        guard MockClient.client.authorized() else { throw ClientError.Unauthorized }

        return VisitorResponse(visitors: Array(visitors.values.sorted(by: { $0.id < $1.id } )))
    }
    
    func add(license: String, name: String) async throws -> Response {
        guard MockClient.client.authorized() else { throw ClientError.Unauthorized }
        
        if (License.normalise(license).count != 6) {
            return Response(success: false, message: "Invalid license")
        }
        
        nextId += 1
        let visitor = Visitor(visitorId: nextId, permitId: permitId, license: license, formattedLicense: License.formatLicense(license), name: name.count > 0 ? name : nil)
        visitors[visitor.visitorId] = visitor
        return Response(success: true)
    }
    
    func delete(_ visitor: Visitor) async throws -> Response {
        guard MockClient.client.authorized() else { throw ClientError.Unauthorized }

        guard let visitor = visitors[visitor.id] else {
            return Response(success: false, message: "Not found")
        }
        for parking in ParkingClientMock.client.parkingSessions.values {
            guard let endDate = try? Util.parseDate(parking.endTime) else {
                continue
            }
            if endDate > Date.now() && parking.license == visitor.license {
                return Response(success: false, message: "Can't delete visitor with active or scheduled sessions")
            }
        }
        
        if visitors.removeValue(forKey: visitor.id) != nil {
            return Response(success: true)
        } else {
            return Response(success: false, message: "Not found")
        }

    }
    
}

struct MockVisitor {
    static let visitors = [
        MockVisitor(name: "Suzanne", license: "111-AA-1"),
        MockVisitor(name: "Erik", license: "22-BBB-2"),
        MockVisitor(name: "Invalid visitor", license: "33-CC-33"),
        MockVisitor(name: "", license: "4-DDD-44")
    ]
    
    let name: String
    let license: String
}
