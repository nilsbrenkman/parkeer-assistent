//
//  WatchCommunicator.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 17/10/2021.
//

import Foundation
import WatchConnectivity

class WatchCommunicator: NSObject, WCSessionDelegate {
    
    static let instance = WatchCommunicator()
    
    var listeners: [ListenerKey:[Listener]] = [:]
    let lock = NSLock()
    
    override private init() {
        super.init()
        print("Cookie Listener")
        addListener(.cookies) { message in
            print("Received cookie")
            if let cookies = message as? String {
                ApiClient.client.setCookies(cookies)
                UserDefaults.standard.set(cookies, forKey: ApiClient.COOKIE_KEY)
            }
        }
    }
    
    func addListener(_ key: ListenerKey, listener: @escaping Listener) {
        lock.lock()
        var keyListeners: [Listener] = listeners[key, default: []]
        keyListeners.append(listener)
        listeners[key] = keyListeners
        lock.unlock()
    }
    
    func sendCookies(_ cookies: String) {
        print("Send cookies")
        sendMessage(.cookies, message: cookies)
        sendMessage(.login, message: "login")
    }
 
    func sendMessage(_ key: ListenerKey, message: String, replyHandler: ReplyHandler? = nil) {
        print("Sending")
        WCSession.default.sendMessage([key.rawValue: message], replyHandler: replyHandler)
    }

    func session(_ session: WCSession, didReceiveMessage message: [String : Any]) {
        print("Receiving message")
        for (key, value) in message {
            if let listenerKey = ListenerKey(rawValue: key),
               let listeners = listeners[listenerKey] {
                for listener in listeners {
                    listener(value)
                }
            }
        }
    }
    
    func session(_ session: WCSession, didReceiveMessage message: [String : Any], replyHandler: ReplyHandler) {
        print("Receiving message that expects reply")
        for (key, value) in message {
            if let listenerKey = ListenerKey(rawValue: key),
               let listeners = listeners[listenerKey] {
                for listener in listeners {
                    listener(value)
                    replyHandler([key:value])
                }
            }
        }
    }


    func session(_ session: WCSession, activationDidCompleteWith activationState: WCSessionActivationState, error: Error?) {
        //
    }
    
#if os(iOS)
    func sessionDidBecomeInactive(_ session: WCSession) {
        //
    }
    func sessionDidDeactivate(_ session: WCSession) {
        //
    }
#endif
}

enum ListenerKey: String {
    case cookies
    case login
    
}

typealias Listener = (_ message: Any) -> Void

typealias ReplyHandler = ([String : Any]) -> Void
