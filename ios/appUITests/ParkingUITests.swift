//
//  ParkingUITests.swift
//  parkeerassistentUITests
//
//  Created by Nils Brenkman on 07/08/2021.
//

import XCTest

class ParkingUITests: XCTestCase {

    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launchEnvironment = ["RUNMODE" : "uitest"]
        app.launch()
        
        LoginUITests.login(app, usernameInput: "test", passwordInput: "1234")
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testNoParking() throws {
        ParkingUITests.initialParkingList(app)
    }
    
    func testAddParkingNow() throws {
        ParkingUITests.initialParkingList(app)
        VisitorUITests.initialVisitorList(app)

        let erik = app.buttons["22-BBB-2, Erik"]
        erik.tap()
        
        let wheel = app.otherElements["wheel-selector"]
        XCTAssertTrue(wheel.waitForExistence(timeout: TestUtil.timeout))
        wheel.swipeLeft()

        app.buttons.element(matching: Label.add).tap()
        
        ParkingUITests.numberOfParking(app, count: 1)
        
        let empty = app.staticTexts.element(matching: Label.parkingEmpty)
        XCTAssertFalse(empty.exists)
        
        let active = app.staticTexts.element(matching: Label.parkingActive)
        XCTAssertTrue(active.exists)
    }
 
    func testAddParkingLater() throws {
        ParkingUITests.initialParkingList(app)
        VisitorUITests.initialVisitorList(app)

        let erik = app.buttons["22-BBB-2, Erik"]
        erik.tap()
        
        let wheel = app.otherElements["wheel-selector"]
        XCTAssertTrue(wheel.waitForExistence(timeout: TestUtil.timeout))
        wheel.swipeLeft()
        
        app.staticTexts.element(matching: Label.start).tap()
        wheel.swipeLeft()
        
        app.buttons.element(matching: Label.add).tap()
        
        ParkingUITests.numberOfParking(app, count: 1)
        
        let empty = app.staticTexts.element(matching: Label.parkingEmpty)
        XCTAssertFalse(empty.exists)
        
        let scheduled = app.staticTexts.element(matching: Label.parkingScheduled)
        XCTAssertTrue(scheduled.exists)
    }
    
    static func initialParkingList(_ app: XCUIApplication) {
        let header = app.staticTexts.element(matching: Label.parkingHeader)
        XCTAssertTrue(header.waitForExistence(timeout: TestUtil.timeout))
  
        let empty = app.staticTexts.element(matching: Label.parkingEmpty)
        XCTAssertTrue(empty.waitForExistence(timeout: TestUtil.timeout))
    }

    static func numberOfParking(_ app: XCUIApplication, count: Int) {
        let visitor = app.buttons.matching(identifier: "parking")
        let predicate = NSPredicate(format: "count == \(count)")
        let expectation = XCTNSPredicateExpectation(predicate: predicate, object:  visitor)
        let result = XCTWaiter().wait(for: [expectation], timeout: TestUtil.timeout)
        XCTAssertEqual(.completed, result)
    }

}
