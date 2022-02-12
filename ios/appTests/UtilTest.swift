//
//  UtilTest.swift
//  parkeerassistentTests
//
//  Created by Nils Brenkman on 29/06/2021.
//

import XCTest
@testable import app

class UtilTest: XCTestCase {


    func testParseDateTime() throws {
        XCTAssertNotNil(Util.dateTimeFormatter.date(from: "2021-06-29T15:51:45+00:00"))
    }


}
