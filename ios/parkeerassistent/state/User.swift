//
//  User.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 22/06/2021.
//

import Foundation

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
    
    let userClient: UserClient
    let parkingClient: ParkingClient
    let visitorClient: VisitorClient
    
    var messenger: ((String, Type) -> Void)? = nil

    init() throws {
        userClient    = try ClientManager.instance.get(UserClient.self)
        parkingClient = try ClientManager.instance.get(ParkingClient.self)
        visitorClient = try ClientManager.instance.get(VisitorClient.self)
                
        DispatchQueue.global(qos: .background).async {
            self.background()
        }
    }

    func getUser() {
        DispatchQueue.global().async {
            self.userClient.get() { response in
                self.getVisitors()
                self.getParking()
                DispatchQueue.main.async {
                    self.balance = response.balance
                    self.hourRate = response.hourRate
                    self.regimeTimeStart = Util.dateTimeFormatter.date(from: response.regimeTimeStart)
                    self.regimeTimeEnd = Util.dateTimeFormatter.date(from: response.regimeTimeEnd)
                    self.timeBalance = Util.calculateTimeBalance(balance: response.balance, hourRate: response.hourRate)
                }
            }
        }
    }

    func getBalance() {
        DispatchQueue.global().async {
            self.userClient.balance() { response in
                DispatchQueue.main.async {
                    self.balance = response.balance
                    self.timeBalance = Util.calculateTimeBalance(balance: response.balance, hourRate: self.hourRate)
                }
            }
        }
    }
    
    func getRegime(_ date: Date, onComplete: @escaping () -> Void) {
        DispatchQueue.global().async {
            self.userClient.regime(date) { response in
                DispatchQueue.main.async {
                    self.regimeTimeStart = Util.dateTimeFormatter.date(from: response.regimeTimeStart)
                    self.regimeTimeEnd = Util.dateTimeFormatter.date(from: response.regimeTimeEnd)
                    onComplete()
                }
            }
        }
    }

    func getVisitor(_ parking: Parking) -> Visitor? {
        return Util.getVisitor(parking, visitors: self.visitors)
    }

    func getVisitors() {
        DispatchQueue.global().async {
            self.visitorClient.get() { response in
                DispatchQueue.main.async {
                    self.visitors = response.visitors
                }
            }
        }
    }
    
    func addVisitor(license: String, name: String, onComplete: @escaping () -> Void) {
        DispatchQueue.global().async {
            self.visitorClient.add(license: license, name: name) { response in
                if response.success {
                    DispatchQueue.main.async {
                        self.visitors = nil
                        self.addVisitor = false
                    }
                    self.getVisitors()
                } else {
                    self.addMessage(response.message, type: Type.WARN)
                }
                onComplete()
            }
        }
    }
    
    func deleteVisitor(_ visitor: Visitor) {
        DispatchQueue.global().async {
            self.visitorClient.delete(visitorId: visitor.visitorId) { response in
                if (response.success) {
                    self.getVisitors()
                } else {
                    self.getVisitors()
                    self.addMessage(response.message, type: Type.WARN)
                }
            }
        }
    }

    func getParking() {
        DispatchQueue.global().async {
            self.parkingClient.get() { response in
                DispatchQueue.main.async {
                    self.parking = ParkingResponse(active: Array(response.active), scheduled: Array(response.scheduled))
                }
                Notifications.store.parking(response, visitors: self.visitors)
            }
        }
    }
    
    func startParking(_ visitor: Visitor, timeMinutes: Int, start: Date, onComplete: @escaping () -> Void) {
        let regimeTimeEnd = Util.dateTimeFormatter.string(from: self.regimeTimeEnd!)
        DispatchQueue.global().async {
            self.parkingClient.start(visitor: visitor, timeMinutes: timeMinutes, start: start, regimeTimeEnd: regimeTimeEnd) { response in
                if response.success {
                    DispatchQueue.main.async {
                        self.selectedVisitor = nil
                        self.parking = nil
                    }
                    self.getParking()
                    self.getBalance()
                } else {
                    self.addMessage(response.message, type: Type.WARN)
                }
                onComplete()
            }
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
        
        DispatchQueue.global().async {
            self.parkingClient.stop(parkingId: parking.id) { response in
                if response.success {
                    self.getParking()
                    self.getBalance()
                } else {
                    self.getParking()
                    self.addMessage(response.message, type: Type.WARN)
                }
            }
        }
    }

    func addMessage(_ message: String?, type: Type) {
        guard let message = message else {
            print("Message is nil")
            return
        }
        if let messenger = self.messenger {
            messenger(message, type)
        } else {
            print("Messenger not set")
        }
    }
    
    func background() {
        while true {
            var delay = 60.0
            var update = false
            if let parking = self.parking {
                for active in parking.active {
                    if let endDate = try? Util.parseDate(active.endTime) {
                        if endDate < Date.init() {
                            update = true
                        } else {
                            let timeInterval = Date().distance(to: endDate)
                            if timeInterval < delay {
                                delay = timeInterval
                            }
                        }
                    }
                }
                for scheduled in parking.scheduled {
                    if let startDate = try? Util.parseDate(scheduled.startTime) {
                        if startDate < Date.init() {
                            update = true
                        } else {
                            let timeInterval = Date().distance(to: startDate)
                            if timeInterval < delay {
                                delay = timeInterval
                            }
                        }
                    }
                }
            }
            if update {
                self.getParking()
            }
            Thread.sleep(forTimeInterval: delay)
        }
    }

}
