//
//  LicenseTest.swift
//  parkeerassistentTests
//
//  Created by Nils Brenkman on 27/06/2021.
//

import XCTest
@testable import parkeerassistent

class LicenseTest: XCTestCase {

    func testFormatLicense() throws {
        XCTAssertEqual("AB-12-CD", License.formatLicense("ab12cd"))
        XCTAssertEqual("A-123-BC", License.formatLicense("a123bc"))
        XCTAssertEqual("AB-123-C", License.formatLicense("ab123c"))
        XCTAssertEqual("ABC-12-D", License.formatLicense("abc12d"))
        XCTAssertEqual("A-12-BCD", License.formatLicense("a12bcd"))
        
        XCTAssertEqual("AB-12-CD", License.formatLicense("AB-12-CD"))
        XCTAssertEqual("AB12C",    License.formatLicense("ab12c"))
    }

}
