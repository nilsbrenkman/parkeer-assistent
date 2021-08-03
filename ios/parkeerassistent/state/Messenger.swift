//
//  Message.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 08/07/2021.
//

import Foundation

class MessageManager {
    
    static let instance = MessageManager()
    
    weak var messenger: Messenger?
    
    private init() {
        //
    }
    
    func register(_ messenger: Messenger?) {
        self.messenger = messenger
    }
    
    func addMessage(_ message: String?, type: Type) {
        guard let message = message else {
            print("Message is nil")
            return
        }
        messenger?.addMessage(message: message, type: type)
    }
    
}

class Messenger: ObservableObject {
    
    @Published var message: Message? = nil
    
    init() {
        MessageManager.instance.register(self)
    }
 
    func addMessage(message: String, type: Type) {
        DispatchQueue.main.async {
            self.message = Message(message: message, type: type)
        }
    }
    
}

struct Message {
    var message: String
    var type: Type
}

enum Type {
    case INFO
    case WARN
}
