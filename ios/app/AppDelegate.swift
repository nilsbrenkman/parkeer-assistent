//
//  AppDelegate.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 27/07/2021.
//

import os
import Foundation
import SwiftUI

class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(_ application: UIApplication, willFinishLaunchingWithOptions
                     launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        return true
    }
    func applicationDidFinishLaunching(_ application: UIApplication) {
        UserDefaults.standard.register(defaults: [
            Notifications.START_KEY: true,
            Notifications.STOP_KEY: true,
            Notifications.REMINDER_KEY: false,
            Notifications.INTERVAL_KEY: 1.0
        ])
        if Util.isUITest() {
            UIView.setAnimationsEnabled(false)
        }
    }
}

extension AppDelegate: UNUserNotificationCenterDelegate {
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.banner, .sound])
    }
}
