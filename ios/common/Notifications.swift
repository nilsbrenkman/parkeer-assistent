//
//  Notifications.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 12/07/2021.
//

import Foundation
import UserNotifications

class Notifications {
    
    static let store = Notifications()
    
    var authorised = false
    
    private init() {
        //
    }
    
    func parking(_ parking: ParkingResponse, visitors: [Visitor]?) {
        UNUserNotificationCenter.current().removeAllPendingNotificationRequests()
        for parking in parking.active {
            try? scheduleEnd(parking, visitors: visitors)
        }
        for parking in parking.scheduled {
            try? scheduleStart(parking, visitors: visitors)
            try? scheduleEnd(parking, visitors: visitors)
        }
    }

    func scheduleStart(_ parking: Parking, visitors: [Visitor]?) throws {
        let subtitle = try subtitle(parking, visitors: visitors)
        let date = try Util.parseDate(parking.startTime)
        schedule(String(format: "\(parking.id)_start"), title: "Parkeer sessie begint", subtitle: subtitle, date: date)
    }
     
    func scheduleEnd(_ parking: Parking, visitors: [Visitor]?) throws {
        let subtitle = try subtitle(parking, visitors: visitors)
        let date = try Util.parseDate(parking.endTime)
        schedule(String(format: "\(parking.id)_end"), title: "Parkeer sessie loopt af", subtitle: subtitle, date: date)
    }
    
    func subtitle(_ parking: Parking, visitors: [Visitor]?) throws -> String {
        guard let visitor = Util.getVisitor(parking, visitors: visitors) else {
            print("Can not find visitor")
            throw GenericError.VisitorNotFound
        }
        let license = License.formatLicense(visitor.license)
        if let name = visitor.name {
            return "\(name) | [ \(license) ]"
        }
        return "[ \(license) ]"
    }
    
    func schedule(_ identifier: String, title: String, subtitle: String, date: Date) {
        if !authorised {
            requestAuthorization()
        }
        
        let content = UNMutableNotificationContent()
        content.title = title
        content.subtitle = subtitle
#if os(iOS)
        content.sound = UNNotificationSound(named: UNNotificationSoundName(Constants.sound.carHorn))
#endif
        if #available(iOS 15.0, *) {
            content.interruptionLevel = .timeSensitive
        }


//        let timeInterval = Date.now().distance(to: date)
        let timeInterval = Date.now().distance(to: Date.now().addingTimeInterval(10.0))
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: timeInterval, repeats: false)

        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)

        UNUserNotificationCenter.current().add(request)
    }
    
    private func requestAuthorization() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { success, error in
            if success {
                self.authorised = true
            } else if let error = error {
                print(error.localizedDescription)
            }
        }
    }

}
