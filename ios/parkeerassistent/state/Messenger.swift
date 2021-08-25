//
//  Message.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 08/07/2021.
//

import Foundation
import SwiftUI

class MessageManager {
    
    static let instance = MessageManager()
    
    weak var messenger: Messenger?
    
    private init() {
        //
    }
    
    func register(_ messenger: Messenger?) {
        self.messenger = messenger
    }
    
    func addMessage(_ message: String?, type: Type, ok: (() -> Void)? = nil) {
        guard let message = message else {
            print("Message is nil")
            return
        }
        messenger?.addMessage(message: message, type: type, ok: ok)
    }
    
}

class Messenger: ObservableObject {
    
    @Published var message: Message? = nil
    
    init() {
        MessageManager.instance.register(self)
    }
 
    func addMessage(message: String, type: Type, ok: (() -> Void)? = nil) {
        DispatchQueue.main.async {
            self.message = Message(message: message, type: type, ok: ok)
        }
    }
    
}

struct Message {
    var message: String
    var type: Type
    var ok: (() -> Void)?
}

enum Type {
    case SUCCESS
    case INFO
    case WARN
    case ERROR

    func color() -> Color {
        switch self {
        case .SUCCESS:
            return Color.ui.success
        case .INFO:
            return Color.ui.info
        case .WARN:
            return Color.ui.warning
        case .ERROR:
            return Color.ui.danger
        }
    }
}
