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
        do {
            try ApiClient.client.call(VisitorResponse.self, path: "visitor", method: Method.GET, onComplete: onComplete)
        } catch {
            print("Error: \(error)")
        }
    }
    
    func add(license: String, name: String, onComplete: @escaping (Response) -> Void) {
        let body = AddVisitorRequest(license: license, name: name)
        do {
            try ApiClient.client.call(Response.self, path: "visitor", method: Method.POST, body: body, onComplete: onComplete)
        } catch {
            print("Error: \(error)")
        }
    }
    
    func delete(visitorId: Int, onComplete: @escaping (Response) -> Void) {
        do {
            try ApiClient.client.call(Response.self, path: "visitor/\(visitorId)", method: Method.DELETE, onComplete: onComplete)
        } catch {
            print("Error: \(error)")
        }
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
    }
    
    func get(onComplete: @escaping (VisitorResponse) -> Void) {
        MockClient.mockDelay()

        onComplete(VisitorResponse(visitors: Array(visitors.values)))
    }
    
    func add(license: String, name: String, onComplete: @escaping (Response) -> Void) {
        MockClient.mockDelay()

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
        MockClient.mockDelay()

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
