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
    func regime(_ date: Date, onComplete: @escaping (RegimeResponse) -> Void)
}

class UserClientApi: UserClient {
    
    static let client = UserClientApi()

    private init() {
        //
    }

    func get(onComplete: @escaping (UserResponse) -> Void) {
        ApiClient.client.call(UserResponse.self, path: "user", method: Method.GET, onComplete: onComplete)
    }
    
    func balance(onComplete: @escaping (BalanceResponse) -> Void) {
        ApiClient.client.call(BalanceResponse.self, path: "user/balance", method: Method.GET, onComplete: onComplete)
    }
    
    func regime(_ date: Date, onComplete: @escaping (RegimeResponse) -> Void) {
        ApiClient.client.call(RegimeResponse.self, path: "user/regime/\(Util.dateFormatter.string(from: date))", method: Method.GET, onComplete: onComplete)
    }
    
}

class UserClientMock: UserClient {
    
    static let client = UserClientMock()
    
    private let hourRate = 2.1
    
    private init() {
        //
    }

    func get(onComplete: @escaping (UserResponse) -> Void) {
        guard MockClient.client.authorized() else { return }
        
        onComplete(UserResponse(balance: getBalance(),
                                hourRate: hourRate,
                                regimeTimeStart: Util.dateTimeFormatter.string(from: getRegimeStart(Date.now())),
                                regimeTimeEnd: Util.dateTimeFormatter.string(from: getRegimeEnd(Date.now()))))
    }
    
    func balance(onComplete: @escaping (BalanceResponse) -> Void) {
        guard MockClient.client.authorized() else { return }
        
        onComplete(BalanceResponse(balance: getBalance()))
    }
 
    func regime(_ date: Date, onComplete: @escaping (RegimeResponse) -> Void) {
        guard MockClient.client.authorized() else { return }
        
        onComplete(RegimeResponse(regimeTimeStart: Util.dateTimeFormatter.string(from: getRegimeStart(date)),
                                  regimeTimeEnd: Util.dateTimeFormatter.string(from: getRegimeEnd(date))))
    }
    
    private func getBalance() -> String {
        return Util.formatCost(ParkingClientMock.client.getBalance())
    }

    private func getRegimeStart(_ date: Date) -> Date {
        if Calendar.current.isDateInWeekend(date) {
            return Calendar.current.date(bySettingHour: 12, minute: 0, second: 0, of: date) ?? date
        }
        return Calendar.current.date(bySettingHour: 9, minute: 0, second: 0, of: date) ?? date
    }
    
    private func getRegimeEnd(_ date: Date) -> Date {
        if Calendar.current.isDateInWeekend(date) {
            return Calendar.current.date(bySettingHour: 21, minute: 0, second: 0, of: date) ?? date
        }
        let tomorrow = Date(timeInterval: 24*60*60, since: date)
        return Calendar.current.startOfDay(for: tomorrow)
    }
 
}
