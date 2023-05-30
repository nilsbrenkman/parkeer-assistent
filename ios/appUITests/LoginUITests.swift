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
        app.launchEnvironment = ["RUNMODE" : "uitest"]
        app.launch()
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testLoginScreen() throws {
        XCTAssertTrue(app.staticTexts.element(matching: Label.username).exists)
        XCTAssertTrue(app.staticTexts.element(matching: Label.password).exists)
        
        let username = app.textFields["username"]
        let password = app.secureTextFields["password"]
        XCTAssertTrue(username.exists)
        XCTAssertTrue(password.exists)
    
        XCTAssertTrue(app.buttons.element(matching: Label.login).exists)
    }
    
    func testLoginSuccess() throws {
        LoginUITests.login(app, usernameInput: "test", passwordInput: "1234")
        
        let menu = app.images["menu"]
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
                
        let menu = app.images["menu"]
        XCTAssertTrue(menu.waitForExistence(timeout: TestUtil.timeout))
        wait(for: [XCTNSPredicateExpectation(predicate: NSPredicate(format: "isHittable == true"), object: menu)], timeout: TestUtil.timeout)
        menu.tap()

        let logout = app.buttons.element(matching: Label.logout)
        XCTAssertTrue(logout.waitForExistence(timeout: TestUtil.timeout))
        logout.tap()
        
        let username = app.staticTexts.element(matching: Label.username)
        XCTAssertTrue(username.waitForExistence(timeout: TestUtil.timeout))
        XCTAssertFalse(logout.exists)
    }
  
    static func login(_ app: XCUIApplication, usernameInput: String, passwordInput: String) {
        let username = app.textFields["username"]
        username.tap()
        username.typeText(usernameInput)

        let password = app.secureTextFields["password"]
        password.tap()
        password.typeText(passwordInput)
        
        let login = app.buttons.element(matching: Label.login)
        login.tap()
        
        let menu = app.images["menu"]
        if menu.waitForExistence(timeout: TestUtil.timeout) {
            let dismiss = app.scrollViews.otherElements.buttons.element(matching: Label.dismiss)
            if dismiss.waitForExistence(timeout: TestUtil.timeout) {
                dismiss.tap()
            }
        }
    }

}
