//
//  TestUtil.swift
//  parkeerassistentUITests
//
//  Created by Nils Brenkman on 07/08/2021.
//

import Foundation

struct TestUtil {
    
    static let timeout: TimeInterval = 10
    
}

struct Label {
    
    static let add = Label.buildPredicate("Add")
    static let start = Label.buildPredicate("Start")
    
    static let username = Label.buildPredicate("Permit code")
    static let password = Label.buildPredicate("Pin code")
    static let login = Label.buildPredicate("Login")
    static let logout = Label.buildPredicate("Logout")
    
    static let parkingHeader = Label.buildPredicate("Parking")
    static let parkingEmpty = Label.buildPredicate("No active or scheduled sessions")
    static let parkingActive = Label.buildPredicate("Active sessions")
    static let parkingScheduled = Label.buildPredicate("Scheduled sessions")
    
    static let visitorHeader = Label.buildPredicate("Visitors")
    static let addVisitor = Label.buildPredicate("Add visitor")
    
    static let dismiss = Label.buildPredicate("Not Now")
    
    private static func buildPredicate(_ label: String) -> NSPredicate {
        return NSPredicate(format: "label CONTAINS %@", label)
    }
    
}
