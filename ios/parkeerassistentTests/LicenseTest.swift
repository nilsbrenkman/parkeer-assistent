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
        XCTAssertEqual("AB-12-CD", License.formatLicense(license: "ab12cd"))
        XCTAssertEqual("A-123-BC", License.formatLicense(license: "a123bc"))
        XCTAssertEqual("AB-123-C", License.formatLicense(license: "ab123c"))
        XCTAssertEqual("ABC-12-D", License.formatLicense(license: "abc12d"))
        XCTAssertEqual("A-12-BCD", License.formatLicense(license: "a12bcd"))
        
        XCTAssertEqual("AB-12-CD", License.formatLicense(license: "AB-12-CD"))
        XCTAssertEqual("AB12C",    License.formatLicense(license: "ab12c"))
    }

}
