//
//  Model.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 22/06/2021.
//

import Foundation

struct Response: Codable {
    var success: Bool
    var message: String?
}

struct LoginRequest: Codable {
    var username: String
    var password: String
}

struct UserResponse: Codable {
    var balance: String
    var hourRate: Double
    var regimeTimeEnd: String
}

struct BalanceResponse: Codable {
    var balance: String
}

struct Parking: Codable, Hashable {
    var id: Int
    var license: String
    var startTime: String
    var endTime: String
    var cost: Double
    
    var visitor: Visitor?
}

struct ParkingResponse: Codable {
    var active: [Parking]
    var scheduled: [Parking]
}

struct AddParkingRequest: Codable {
    var visitor: Visitor
    var timeMinutes: Int
    var regimeTimeEnd: String
}

struct Visitor: Codable, Hashable {
    var visitorId: Int
    var permitId: Int
    var license: String
    var formattedLicense: String
    var name: String
    
    var id: Int {
        visitorId
    }
}

struct VisitorResponse: Codable {
    var visitors: [Visitor]
}

struct AddVisitorRequest: Codable {
    var license: String
    var name: String
}
