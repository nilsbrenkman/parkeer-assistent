//
//  Util.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 27/06/2021.
//

import Foundation

class Util {
        
    static let dateTimeFormatter = createDateFormatter(format: "yyyy-MM-dd'T'HH:mm:ssZZZZZ")
    static let timeFormatter = createDateFormatter(format: "HH:mm")
    static let dateFormatter = createDateFormatter(format: "yyyy-MM-dd")
    static let dayMonthFormatter = createDateFormatter(format: "d MMM")
    static let parkingFormatter = createDateFormatter(format: "dd/MM HH:mm")

    static func createDateFormatter(format: String) -> DateFormatter {
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
