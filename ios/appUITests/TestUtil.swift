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
    
    static let username = NSPredicate(format: "label contains 'Permit code'")
    static let password = NSPredicate(format: "label contains 'Pin code'")
    static let login = NSPredicate(format: "label contains 'Login'")
    static let logout = NSPredicate(format: "label contains 'Logout'")
    
    static let addVisitor = NSPredicate(format: "label contains 'Add visitor'")
    
    
}
