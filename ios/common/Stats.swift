//
//  Review.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 04/09/2021.
//

import Foundation
import StoreKit


struct Stats: Codable {
    
    private static let STATS_KEY = "userStats"
    
    public static var user: Stats {
        get {
            if let data = UserDefaults.standard.data(forKey: Stats.STATS_KEY),
               let stats = try? JSONDecoder().decode(Stats.self, from: data) {
                return stats
            }
            return Stats(firstLogin: Date.now(), loginCount: 0, visitorCount: 0, parkingCount: 0, paymentCount: 0)
        }
        set {
            if let data = try? JSONEncoder().encode(newValue) {
                UserDefaults.standard.set(data, forKey: Stats.STATS_KEY)
            }
        }
    }
    
    var firstLogin: Date
    var requested: Date? = nil
    var loginCount: Int
    var visitorCount: Int
    var parkingCount: Int
    var paymentCount: Int
    
    func requestReview() -> Bool {
        if requested != nil {
            return false
        }
        if firstLogin > Date.now().addingTimeInterval(60 * 60 * 24 * 14) {
            return false
        }
        if loginCount < 10 {
            return false
        }
        if (visitorCount + parkingCount + paymentCount) < 10 {
            return false
        }
        return true
    }
    
}
