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
        case ok, add, back, save, delete, cancel, select, stop
    }

    enum Login: String, Localization {
        case login, logout, username, password, remember, rememberShort, reason, failed, error
    }
    
    enum Account: String, Localization {
        case header, newAccount, details, label, alias, autoLogin, confirmDelete, errorUnavailable, errorFailed
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
        case header, start, active, scheduled, noSessions, date, startTime, endTime, minutes, cost, stop, history, details, noHistory, freeParking
    }

    enum Payment: String, Localization {
        case amount, bank, start, status, inProgress, recipientMsg, redirectMsg, redirectErrorMsg, successMsg, pendingMsg, errorMsg, unknownMsg
    }

    enum Settings: String, Localization {
        case header, notifications, onStart, onStop, reminders
    }

    enum Info: String, Localization {
        case header, text1, text2, text3, text4, text5, text6, website, sourceCode, feedback, version
    }
    
    enum Error: String, Localization {
        case unauthorized, serverUnknown
    }
}

protocol Localized {
    static var allCases: [Self] { get }
    var rawValue: String { get }
}

extension Localized where Self : RawRepresentable, Self.RawValue == String {
    func localized() -> String {
        let prefix = String(describing: type(of: self)).lowercased()
        let key = prefix + "." + rawValue
        return NSLocalizedString(key, tableName: "Language", bundle: .main, value: key, comment: key)
    }
    func predicate() -> NSPredicate {
        NSPredicate(format: "label CONTAINS %@", localized())
    }
}

typealias Localization = Localized & RawRepresentable & CaseIterable

