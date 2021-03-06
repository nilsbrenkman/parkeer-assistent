//
//  VisitorClient.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 22/06/2021.
//

import Foundation

protocol VisitorClient {
    func get(onComplete: @escaping (VisitorResponse) -> Void)
    func add(license: String, name: String, onComplete: @escaping (Response) -> Void)
    func delete(visitorId: Int, onComplete: @escaping (Response) -> Void)
}

class VisitorClientApi: VisitorClient {
 
    static let client = VisitorClientApi()
    
    private init() {
        //
    }

    func get(onComplete: @escaping (VisitorResponse) -> Void) {
        ApiClient.client.call(VisitorResponse.self, path: "visitor", method: Method.GET, onComplete: onComplete)
    }
    
    func add(license: String, name: String, onComplete: @escaping (Response) -> Void) {
        let body = AddVisitorRequest(license: license, name: name)
        ApiClient.client.call(Response.self, path: "visitor", method: Method.POST, body: body, onComplete: onComplete)
    }
    
    func delete(visitorId: Int, onComplete: @escaping (Response) -> Void) {
        ApiClient.client.call(Response.self, path: "visitor/\(visitorId)", method: Method.DELETE, onComplete: onComplete)
    }

}

class VisitorClientMock: VisitorClient {
    
    static let client = VisitorClientMock()
    
    private var nextId = 0
    private var visitors: [Int:Visitor] = [:]
    private let permitId = 999

    private init() {
        for visitor in MockVisitor.visitors {
            add(license: visitor.license, name: visitor.name) { response in }
        }
        UserClientMock.client.regime(Date.now()) { regime in
            ParkingClientMock.client.start(visitor: self.visitors[1]!, timeMinutes: 1, start: Date.now(), regimeTimeEnd: regime.regimeTimeEnd) { response in }
            ParkingClientMock.client.start(visitor: self.visitors[2]!, timeMinutes: 10, start: Date.now().addingTimeInterval(10.0), regimeTimeEnd: regime.regimeTimeEnd) { response in }
        }
        UserClientMock.client.regime(Date(timeInterval: 24*60*60, since: Date.now())) { regime in
            try? ParkingClientMock.client.start(visitor: self.visitors[2]!, timeMinutes: 10, start: Util.parseDate(regime.regimeTimeStart), regimeTimeEnd: regime.regimeTimeEnd) { response in }
        }
        UserClientMock.client.regime(Date(timeInterval: -24*60*60, since: Date.now())) { regime in
            try? ParkingClientMock.client.start(visitor: self.visitors[2]!, timeMinutes: 60, start: Util.parseDate(regime.regimeTimeStart), regimeTimeEnd: regime.regimeTimeEnd) { response in }
        }
    }
    
    func get(onComplete: @escaping (VisitorResponse) -> Void) {
        guard MockClient.client.authorized() else { return }

        onComplete(VisitorResponse(visitors: Array(visitors.values.sorted(by: { $0.id < $1.id } ))))
    }
    
    func add(license: String, name: String, onComplete: @escaping (Response) -> Void) {
        guard MockClient.client.authorized() else { return }
        
        if (License.normalise(license).count != 6) {
            onComplete(Response(success: false, message: "Invalid license"))
            return
        }
        
        nextId += 1
        let visitor = Visitor(visitorId: nextId, permitId: permitId, license: license, formattedLicense: License.formatLicense(license), name: name.count > 0 ? name : nil)
        visitors[visitor.visitorId] = visitor
        onComplete(Response(success: true))
    }
    
    func delete(visitorId: Int, onComplete: @escaping (Response) -> Void) {
        guard MockClient.client.authorized() else { return }

        guard let visitor = visitors[visitorId] else {
            onComplete(Response(success: false, message: "Not found"))
            return
        }
        for parking in ParkingClientMock.client.parking.values {
            guard let endDate = try? Util.parseDate(parking.endTime) else {
                continue
            }
            if endDate > Date.now() && parking.license == visitor.license {
                onComplete(Response(success: false, message: "Can't delete visitor with active or scheduled sessions"))
                return
            }
        }
        
        if visitors.removeValue(forKey: visitorId) != nil {
            onComplete(Response(success: true))
        } else {
            onComplete(Response(success: false, message: "Not found"))
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
