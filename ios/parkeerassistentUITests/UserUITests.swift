//
//  UserUITests.swift
//  parkeerassistentUITests
//
//  Created by Nils Brenkman on 04/08/2021.
//

import XCTest

class UserUITests: XCTestCase {

    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launchArguments = ["ui-test"]
        app.launch()
        
        LoginUITests.login(app, usernameInput: "test", passwordInput: "1234")
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testBalance() throws {
        let saldo = app.staticTexts["Saldo:"]
        XCTAssertTrue(saldo.waitForExistence(timeout: TestUtil.timeout))
    
        XCTAssertEqual(10.00, UserUITests.getBalance(app))
    }
    
    static func getBalance(_ app: XCUIApplication) -> Double {
        let balance = app.staticTexts["balance"]
        XCTAssertTrue(balance.exists)
        
        let formatted = balance.label
        let amount = formatted.dropFirst(2)
        return Double(amount) ?? 0
    }

}
