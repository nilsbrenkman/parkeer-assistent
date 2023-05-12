//
//  UserClient.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 22/06/2021.
//

import Foundation

protocol UserClient {
    func get() async throws -> UserResponse
    func balance() async throws -> BalanceResponse
    func regime(_ date: Date) async throws -> RegimeResponse
}

class UserClientApi: UserClient {
    
    static let client = UserClientApi()

    private init() {
        //
    }
    
    func get() async throws -> UserResponse {
        return try await ApiClient.client.call(UserResponse.self, path: "user", method: Method.GET)
    }
    
    func balance() async throws -> BalanceResponse {
        return try await ApiClient.client.call(BalanceResponse.self, path: "user/balance", method: Method.GET)
    }
    
    func regime(_ date: Date) async throws -> RegimeResponse {
        return try await ApiClient.client.call(RegimeResponse.self, path: "user/regime/\(Util.dateFormatter.string(from: date))", method: Method.GET)
    }
    
}

class UserClientMock: UserClient {
    
    static let client = UserClientMock()
    
    private let hourRate = 2.1
    
    private init() {
        //
    }

    func get() async throws -> UserResponse {
        guard MockClient.client.authorized() else { throw ClientError.Unauthorized }
        
        return UserResponse(balance: getBalance(),
                            hourRate: hourRate,
                            regimeTimeStart: Util.dateTimeFormatter.string(from: getRegimeStart(Date.now())),
                            regimeTimeEnd: Util.dateTimeFormatter.string(from: getRegimeEnd(Date.now())))
    }
    
    func balance() async throws -> BalanceResponse {
        guard MockClient.client.authorized() else { throw ClientError.Unauthorized }
        
        return BalanceResponse(balance: getBalance())
    }
    
    func regime(_ date: Date) async throws -> RegimeResponse {
        guard MockClient.client.authorized() else { throw ClientError.Unauthorized }
        
        return RegimeResponse(regimeTimeStart: Util.dateTimeFormatter.string(from: getRegimeStart(date)),
                              regimeTimeEnd: Util.dateTimeFormatter.string(from: getRegimeEnd(date)))
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
