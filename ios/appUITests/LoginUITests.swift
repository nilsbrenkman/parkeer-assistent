//
//  LoginUITests.swift
//  parkeerassistentUITests
//
//  Created by Nils Brenkman on 03/08/2021.
//

import XCTest

class LoginUITests: XCTestCase {

    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launchArguments = ["ui-test"]
        app.launch()
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testLoginScreen() throws {
        XCTAssertTrue(app.staticTexts["Meldcode:"].exists)
        XCTAssertTrue(app.staticTexts["Pincode:"].exists)

        let username = app.textFields["username"]
        let password = app.secureTextFields["password"]
        XCTAssertTrue(username.exists)
        XCTAssertTrue(password.exists)
        
        let login = app.buttons["Inloggen"]
        XCTAssertTrue(login.exists)
    }
    
    func testLoginSuccess() throws {
        LoginUITests.login(app, usernameInput: "test", passwordInput: "1234")
        
        let menu = app.buttons["Menu"]
        XCTAssertTrue(menu.waitForExistence(timeout: TestUtil.timeout))
    }
    
    func testLoginFailed() throws {
        LoginUITests.login(app, usernameInput: "fail", passwordInput: "invalid")
        
        let message = app.staticTexts["message"]
        XCTAssertTrue(message.waitForExistence(timeout: TestUtil.timeout))
        XCTAssertTrue(message.label == "Login failed")
    }
    
    func testLogout() throws {
        try testLoginSuccess()
        
        app.buttons["Menu"].tap()

        let logout = app.buttons["Uitloggen"]
        XCTAssertTrue(logout.waitForExistence(timeout: TestUtil.timeout))
        logout.tap()
        
        let meldcode = app.staticTexts["Meldcode:"]
        XCTAssertTrue(meldcode.waitForExistence(timeout: TestUtil.timeout))
        XCTAssertFalse(logout.exists)
    }
  
    static func login(_ app: XCUIApplication, usernameInput: String, passwordInput: String) {
        let username = app.textFields["username"]
        username.tap()
        username.typeText(usernameInput)

        let password = app.secureTextFields["password"]
        password.tap()
        password.typeText(passwordInput)
        
        let login = app.buttons["Inloggen"]
        login.tap()
    }

}
