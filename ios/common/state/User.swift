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
    @Published var regime: Regime?
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

    func getUser() async {
        guard let response = try? await self.userClient.get() else { return }
            
        await self.getVisitors()
        await self.getParking()
        
        self.balance = response.balance
        self.hourRate = response.hourRate
        self.regimeTimeStart = Util.dateTimeFormatter.date(from: response.regimeTimeStart)
        self.regimeTimeEnd = Util.dateTimeFormatter.date(from: response.regimeTimeEnd)
        self.regime = response.regime
        self.timeBalance = Util.calculateTimeBalance(balance: response.balance,
                                                     hourRate: response.hourRate)
    }

    func getBalance() async {
        guard let response = try? await self.userClient.balance() else { return }
        
        self.balance = response.balance
        self.timeBalance = Util.calculateTimeBalance(balance: response.balance,
                                                     hourRate: self.hourRate)
    }
    
    func getRegime(_ date: Date) async {
        if regime != nil {
            setRegimeForDate(date)
            return
        }
        
        guard let response = try? await self.userClient.regime(date) else { return }
        
        self.regimeTimeStart = Util.dateTimeFormatter.date(from: response.regimeTimeStart)
        self.regimeTimeEnd = Util.dateTimeFormatter.date(from: response.regimeTimeEnd)
    }
    
    func setRegimeForDate(_ date: Date) {
        guard let regime,
              let regimeDay = Util.getRegimeDay(regime: regime, date: date) else {
            self.regimeTimeStart = Calendar.current.date(bySettingHour: 0, minute: 0, second: 0, of: date)
            self.regimeTimeEnd = Calendar.current.date(bySettingHour: 0, minute: 0, second: 0, of: date)
            return
        }
        self.regimeTimeStart = getRegimeTime(date: date, time: regimeDay.startTime)
        self.regimeTimeEnd = getRegimeTime(date: date, time: regimeDay.endTime)
    }
    
    func getRegimeTime(date: Date, time: String) -> Date? {
        let times = time.split(separator: ":")
        return Calendar.current.date(bySettingHour: Int(times[0]) ?? 0, minute: Int(times[1]) ?? 0, second: 0, of: date)
    }

    func getVisitor(_ parking: Parking) -> Visitor? {
        return Util.getVisitor(parking, visitors: self.visitors)
    }

    func getVisitors() async {
        guard let response = try? await self.visitorClient.get() else { return }
        
        self.visitors = response.visitors
    }
    
    func addVisitor(license: String, name: String) async {
        guard let response = try? await self.visitorClient.add(license: license, name: name) else { return }
        
        if response.success {
            Stats.user.visitorCount += 1
            self.visitors = nil
            self.addVisitor = false
            await self.getVisitors()
        } else {
            MessageManager.instance.addMessage(response.message, type: Type.ERROR)
        }
    }
    
    func deleteVisitor(_ visitor: Visitor) async {
        guard let response = try? await self.visitorClient.delete(visitor) else { return }
        
        if (!response.success) {
            MessageManager.instance.addMessage(response.message, type: Type.ERROR)
        }
        await self.getVisitors()
    }

    func getParking() async {
        guard let response = try? await self.parkingClient.get() else { return }

        self.parking = ParkingResponse(active: Array(response.active),
                                       scheduled: Array(response.scheduled))
        Notifications.store.parking(response, visitors: self.visitors)
    }
    
    func startParking(_ visitor: Visitor, timeMinutes: Int, start: Date) async {
        let regimeTimeEnd = Util.dateTimeFormatter.string(from: self.regimeTimeEnd!)

        guard let response = try? await self.parkingClient.start(visitor: visitor,
                                                                 timeMinutes: timeMinutes,
                                                                 start: start,
                                                                 regimeTimeEnd: regimeTimeEnd) else { return }
            
        if response.success {
            Stats.user.parkingCount += 1
            
            self.selectedVisitor = nil
            self.parking = nil
            
            await self.getParking()
            await self.getBalance()
            await self.getRegime(Date.now())
        } else {
            MessageManager.instance.addMessage(response.message, type: Type.ERROR)
        }
    }

    func stopParking(_ parking: Parking) async {
        self.parking = ParkingResponse(
            active: Array(self.parking!.active.filter({ p in
                return p.id != parking.id
            })),
            scheduled: Array(self.parking!.scheduled.filter({ p in
                return p.id != parking.id
            }))
        )
        
        guard let response = try? await self.parkingClient.stop(parking) else { return }
       
        if !response.success {
            MessageManager.instance.addMessage(response.message, type: Type.ERROR)
        }
        
        await self.getParking()
        await self.getBalance()
    }

}
