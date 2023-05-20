//
//  Util.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 27/06/2021.
//

import Foundation

class Util {
        
    static let dateTimeFormatter = createDateFormatter("yyyy-MM-dd'T'HH:mm:ssZZZZZ")
    static let timeFormatter = createDateFormatter("HH:mm")
    static let dateFormatter = createDateFormatter("yyyy-MM-dd")
    static let dayMonthFormatter = createDateFormatter("d MMM")
    static let dowDayMonthFormatter = createDateFormatter("E d MMM")
    static let parkingFormatter = createDateFormatter("dd/MM HH:mm")

    static func createDateFormatter(_ format: String) -> DateFormatter {
        let formatter = DateFormatter()
        formatter.dateFormat = format
        return formatter
    }

    static func parseDate(_ date: String) throws -> Date {
        guard let date = Util.dateTimeFormatter.date(from: date) else {
            print("Could not parse date")
            throw GenericError.InvalidDate
        }
        return date
    }
    
    static func calculateCost(minutes: Int, hourRate: Double?) -> String {
        if let hourRate = hourRate {
            let cost = (hourRate * Double(minutes)) / 60
            return formatCost(cost)
        }
        return formatCost(0)
    }
    
    static func formatCost(_ cost: Double) -> String {
        return String(format: "%.2f", cost)
    }
    
    static func calculateTimeBalance(balance: String?, hourRate: Double?) -> Int {
        if let balance = Double(balance ?? "0"), let hourRate = hourRate {
            return Int(balance / hourRate * 60)
        }
        return 0
    }
 
    static func getParkingTime(_ time: String) -> String {
        guard let date = Util.dateTimeFormatter.date(from: time) else {
            print("Could not parse date")
            return ""
        }
        return parkingFormatter.string(from: date)
    }
    
    static func convertDate(_ time: String, formatter: DateFormatter) throws -> String {
        guard let date = Util.dateTimeFormatter.date(from: time) else {
            throw GenericError.InvalidDate
        }
        return formatter.string(from: date)
    }
    
    static func getRegimeDay(regime: Regime, date: Date) -> RegimeDay? {
        let weekday = Calendar.current.component(Calendar.Component.weekday, from: date)
        let dayOfWeek = Util.weekdays[weekday - 1]
        let regimeDay = regime.days.first(where: { d in
            d.weekday == dayOfWeek
        })
        return regimeDay
    }
    
    static let weekdays = ["SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"]
    
    static func getVisitor(_ parking: Parking, visitors: [Visitor]?) -> Visitor? {
        if let visitors = visitors {
            for visitor in visitors {
                if visitor.formattedLicense == parking.license || visitor.license == parking.license {
                    return visitor
                }
            }
        }
        return nil
    }
    
    static func getSetting(_ name: String) -> String {
        let settings = Bundle.main.infoDictionary?["AppSettings"] as! [AnyHashable:Any]
        return settings[name] as! String
    }
    
    static func isUITest() -> Bool {
        #if DEBUG
        if CommandLine.arguments.contains("ui-test") {
            return true
        }
        #endif
        return false
    }
    
}

extension Date {

    static let systemTimeOffset = systemTimeOverride()
    
    static func now() -> Date {
        #if DEBUG
        if let interval = systemTimeOffset {
            return Date().addingTimeInterval(interval)
        }
        #endif
        return Date()
    }
    
    static func systemTimeOverride() -> TimeInterval? {
        #if DEBUG
        if Util.isUITest() {
            return try? Util.parseDate("2021-08-01T14:00:00+02:00").timeIntervalSinceNow
        }
        #endif
        return nil
    }
    
}

struct TimeUnit {
    
    static let seconds = TimeUnit(multiplier: 1)
    static let minutes = TimeUnit(multiplier: seconds.toInterval(value: 60))
    static let hours = TimeUnit(multiplier: minutes.toInterval(value: 60))
    static let days = TimeUnit(multiplier: hours.toInterval(value: 24))
    
    private let multiplier: Double
    
    func toInterval(value: Double) -> TimeInterval {
        return TimeInterval(multiplier * value)
    }
    
}
