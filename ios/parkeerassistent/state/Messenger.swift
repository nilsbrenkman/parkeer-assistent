//
//  Message.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 08/07/2021.
//

import Foundation

class Messenger: ObservableObject {
    
    @Published var message: Message? = nil
 
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
