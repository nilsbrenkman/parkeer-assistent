//
//  Log.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 12/05/2023.
//

import os
import Foundation

//class Log {
//
//    static let logger = Log()
//
//    private let logger: Logger
//
//    private init() {
//        let subsystem = Bundle.main.infoDictionary?["CFBundleIdentifier"] as? String ?? "parkeerassistent"
//        logger = Logger(subsystem: subsystem, category: "application")
//    }
//
//    static func info(_ message: String) {
//        Log.logger.logger.info(OSLogMessage(stringLiteral: message))
//    }
//
//}

let Log = Logger(subsystem: Bundle.main.infoDictionary?["CFBundleIdentifier"] as? String ?? "parkeerassistent",
                 category: "application")
