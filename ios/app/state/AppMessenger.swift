//
//  Messenger.swift
//  app
//
//  Created by Nils Brenkman on 19/10/2021.
//

import Foundation

class AppMessenger: ObservableObject, Messenger {
    
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
