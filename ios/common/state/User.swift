//
//  User.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 22/06/2021.
//

import Foundation

@MainActor
class User: ObservableObject {
    
    @Published var balance: String?
    @Published var hourRate: Double?
    @Published var timeBalance: Int = 0
    @Published var regimeTimeStart: Date?
    @Published var regimeTimeEnd: Date?
    @Published var visitors: [Visitor]?
    @Published var parking: ParkingResponse?
    
    @Published var isLoaded: Bool = false
    @Published var addVisitor: Bool = false
    @Published var selectedVisitor: Visitor?
    
    let loginClient: LoginClient
    let userClient: UserClient
    let parkingClient: ParkingClient
    let visitorClient: VisitorClient

    init() throws {
        loginClient   = try ClientManager.instance.get(LoginClient.self)
        userClient    = try ClientManager.instance.get(UserClient.self)
        parkingClient = try ClientManager.instance.get(ParkingClient.self)
        visitorClient = try ClientManager.instance.get(VisitorClient.self)
    }

    func getUser() {
        Task {
            let response = try await self.userClient.get()
            
            self.getVisitors()
            self.getParking()
            
            self.balance = response.balance
            self.hourRate = response.hourRate
            self.regimeTimeStart = Util.dateTimeFormatter.date(from: response.regimeTimeStart)
            self.regimeTimeEnd = Util.dateTimeFormatter.date(from: response.regimeTimeEnd)
            self.timeBalance = Util.calculateTimeBalance(balance: response.balance, hourRate: response.hourRate)

        }

    }

    func getBalance() {
        Task {
            let response = try await self.userClient.balance()
            self.balance = response.balance
            self.timeBalance = Util.calculateTimeBalance(balance: response.balance, hourRate: self.hourRate)
        }
    }
    
    func getRegime(_ date: Date, onComplete: @escaping () -> Void) {
        Task {
            let response = try await self.userClient.regime(date)
            
            self.regimeTimeStart = Util.dateTimeFormatter.date(from: response.regimeTimeStart)
            self.regimeTimeEnd = Util.dateTimeFormatter.date(from: response.regimeTimeEnd)
            onComplete()
        }
    }

    func getVisitor(_ parking: Parking) -> Visitor? {
        return Util.getVisitor(parking, visitors: self.visitors)
    }

    func getVisitors() {
        Task {
            self.visitors = try await self.visitorClient.get().visitors
        }
    }
    
    func addVisitor(license: String, name: String, onComplete: @escaping () -> Void) {
        Task {
            let response = try await self.visitorClient.add(license: license, name: name)
            
            if response.success {
                Stats.user.visitorCount += 1
                self.visitors = nil
                self.addVisitor = false
                self.getVisitors()
            } else {
                MessageManager.instance.addMessage(response.message, type: Type.ERROR)
            }
            onComplete()
        }
    }
    
    func deleteVisitor(_ visitor: Visitor) {
        Task {
            let response = try await self.visitorClient.delete(visitor)
            
            if (response.success) {
                self.getVisitors()
            } else {
                self.getVisitors()
                MessageManager.instance.addMessage(response.message, type: Type.ERROR)
            }
        }
    }

    func getParking() {
        Task {
            let response = try await self.parkingClient.get()
            
            self.parking = ParkingResponse(active: Array(response.active), scheduled: Array(response.scheduled))
            Notifications.store.parking(response, visitors: self.visitors)
        }
    }
    
    func startParking(_ visitor: Visitor, timeMinutes: Int, start: Date, onComplete: @escaping () -> Void) {
        let regimeTimeEnd = Util.dateTimeFormatter.string(from: self.regimeTimeEnd!)

        Task {
            let response = try await self.parkingClient.start(visitor: visitor, timeMinutes: timeMinutes, start: start, regimeTimeEnd: regimeTimeEnd)
            
            if response.success {
                Stats.user.parkingCount += 1
                
                self.selectedVisitor = nil
                self.parking = nil
                
                self.getParking()
                self.getBalance()
                self.getRegime(Date.now()) {
                    //
                }
            } else {
                MessageManager.instance.addMessage(response.message, type: Type.ERROR)
            }
            onComplete()
        }
    }

    func stopParking(_ parking: Parking) {
        self.parking = ParkingResponse(
            active: Array(self.parking!.active.filter({ p in
                return p.id != parking.id
            })),
            scheduled: Array(self.parking!.scheduled.filter({ p in
                return p.id != parking.id
            }))
        )
        
        Task {
            let response = try await self.parkingClient.stop(parking)
            
            if response.success {
                self.getParking()
                self.getBalance()
            } else {
                self.getParking()
                MessageManager.instance.addMessage(response.message, type: Type.ERROR)
            }
        }
    }

}
