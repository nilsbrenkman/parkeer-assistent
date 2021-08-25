//
//  Language.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 24/08/2021.
//

import Foundation

struct Lang {
    
    enum Common: String, LocalizationEnum {
        case ok, add, back, cancel, select
    }

    enum Login: String, LocalizationEnum {
        case login, logout, username, password, remember, reason
    }

    enum User: String, LocalizationEnum {
        case balance
    }

    enum Visitor: String, LocalizationEnum {
        case header, noVisitors, add, license, name
    }

    enum Parking: String, LocalizationEnum {
        case header, active, scheduled, noSessions, date, startTime, endTime, minutes, cost, stop
    }

    enum Payment: String, LocalizationEnum {
        case amount, bank, start, status, successMsg, pendingMsg, errorMsg, unknownMsg
    }

}

protocol LocalizationEnum {
    var rawValue: String { get }
}

extension LocalizationEnum where Self : RawRepresentable, Self.RawValue == String {
    func localized() -> String {
        let prefix = String(describing: type(of: self)).lowercased()
        let key = prefix + "." + self.rawValue
        return NSLocalizedString(key, tableName: "Language", bundle: .main, value: key, comment: key)
    }
}
