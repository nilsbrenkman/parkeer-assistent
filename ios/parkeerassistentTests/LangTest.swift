//
//  LangTest.swift
//  parkeerassistentTests
//
//  Created by Nils Brenkman on 25/09/2021.
//

import XCTest
@testable import parkeerassistent

class LangTest: XCTestCase {

    override func setUpWithError() throws {
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    static let localizations: [Localized.Type] = [Lang.Common.self,
                                                  Lang.Login.self,
                                                  Lang.User.self,
                                                  Lang.Visitor.self,
                                                  Lang.Parking.self,
                                                  Lang.Payment.self,
                                                  Lang.Info.self]
    
    func testAllTranslationsExist() throws {
        let languages = Bundle.main.localizations
        XCTAssertTrue(languages.contains("nl"))
        XCTAssertTrue(languages.contains("en"))
        XCTAssertFalse(languages.contains("fr"))
        
        for language in languages {
            translationsExists(language)
        }
    }

    private func translationsExists(_ language: String) {
        guard let path = Bundle.main.path(forResource: language + ".lproj/Language", ofType: "strings"),
              let dict = NSDictionary(contentsOfFile: path) else {
            XCTFail("No translation file found for \(language)")
            return
        }
        
        print("Translation file found for \(language)")
        for localization in LangTest.localizations {
            translationsExists(dict, language: language, translations: localization.allCases)
        }
    }
    
    private func translationsExists(_ dict: NSDictionary, language: String, translations: [Localized]) {
        for translation in translations {
            let prefix = String(describing: type(of: translation)).lowercased()
            let key = prefix + "." + translation.rawValue
            guard (dict.value(forKey: key) as? String) != nil else {
                XCTFail("Missing \(language) translation for \(key)")
                return
            }
        }
    }
        
}
