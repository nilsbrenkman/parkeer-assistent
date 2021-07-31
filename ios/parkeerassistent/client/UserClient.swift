//
//  UserClient.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 22/06/2021.
//

import Foundation

protocol UserClient {
    func get(onComplete: @escaping (UserResponse) -> Void)
    func balance(onComplete: @escaping (BalanceResponse) -> Void)
}

class UserClientApi: UserClient {
    
    static let client = UserClientApi()

    private init() {
        //
    }

    func get(onComplete: @escaping (UserResponse) -> Void) {
        do {
            try ApiClient.client.call(UserResponse.self, path: "user", method: Method.GET, onComplete: onComplete)
        } catch {
            print("Error: \(error)")
        }
    }
    
    func balance(onComplete: @escaping (BalanceResponse) -> Void) {
        do {
            try ApiClient.client.call(BalanceResponse.self, path: "user/balance", method: Method.GET, onComplete: onComplete)
        } catch {
            print("Error: \(error)")
        }
    }
    
}

class UserClientMock: UserClient {
    
    static let client = UserClientMock()
    
    private let dateFormatter = Util.createDateFormatter(format: "yyyy-MM-dd")
    private let hourRate = 2.1
    
    private init() {
        //
    }

    func get(onComplete: @escaping (UserResponse) -> Void) {
        MockClient.mockDelay()
        let regimeTimeStart = "\(dateFormatter.string(from: Date()))T09:00:00+02:00"
        let tomorrow = Date(timeIntervalSinceNow: 24*60*60)
        let regimeTimeEnd = "\(dateFormatter.string(from: tomorrow))T00:00:00+02:00"
        onComplete(UserResponse(balance: "50.00", hourRate: hourRate, regimeTimeStart: regimeTimeStart, regimeTimeEnd: regimeTimeEnd))
    }
    
    func balance(onComplete: @escaping (BalanceResponse) -> Void) {
        MockClient.mockDelay()
        onComplete(BalanceResponse(balance: "50.00"))
    }
    
}
