//
//  Language.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 24/08/2021.
//

import Foundation
import SwiftUI

struct Lang {
    
    enum Common: String, Localization {
        case ok, add, back, delete, cancel, select, stop
    }

    enum Login: String, Localization {
        case login, logout, username, password, remember, rememberShort, reason
    }
    
    enum Account: String, Localization {
        case header, label, alias, autoLogin, confirmDelete, errorUnavailable, errorFailed
    }
    
    enum Menu: String, Localization {
        case button
    }

    enum User: String, Localization {
        case balance, addBalance
    }

    enum Visitor: String, Localization {
        case header, noVisitors, add, license, name, tooManyMsg
    }

    enum Parking: String, Localization {
        case header, active, scheduled, noSessions, date, startTime, endTime, minutes, cost, stop, history, noHistory
    }

    enum Payment: String, Localization {
        case amount, bank, start, status, recipientMsg, redirectMsg, redirectErrorMsg, successMsg, pendingMsg, errorMsg, unknownMsg
    }

    enum Info: String, Localization {
        case header, text1, text2, text3, text4, text5, text6, website, sourceCode, feedback, version
    }
}

protocol Localized {
    static var allCases: [Self] { get }
    var rawValue: String { get }
}

extension Localized where Self : RawRepresentable, Self.RawValue == String {
    func localized() -> String {
        let prefix = String(describing: type(of: self)).lowercased()
        let key = prefix + "." + self.rawValue
        return NSLocalizedString(key, tableName: "Language", bundle: .main, value: key, comment: key)
    }
}

typealias Localization = Localized & RawRepresentable & CaseIterable
