//
//  Messenger.swift
//  watch WatchKit Extension
//
//  Created by Nils Brenkman on 19/10/2021.
//

import Foundation
import SwiftUI

class WatchMessenger: ObservableObject, Messenger {
    
    @Published var show: Bool = false
    @Published var message: Message? = nil
    
    init() {
        MessageManager.instance.register(self)
    }
 
    func addMessage(message: String, type: Type, ok: (() -> Void)? = nil) {
//        DispatchQueue.main.async {
//            self.message = Message(message: message, type: type, ok: ok)
//            if type == .ERROR || type == .WARN {
//                self.show = true
//            }
//        }
    }
    
}
