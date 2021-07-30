//
//  Constants.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 30/06/2021.
//

import Foundation
import SwiftUI

extension Color {
    static let ui = Color.UI()
    
    struct UI {
        let header = Color("Color-header")
        
        let license = Color("Color-license")
        let licenseBg = Color("Color-license-bg")
        let licenseBorder = Color("Color-license-border")

        let light = Color("Color-light")
        let bw0 = Color("Color-bw-0")
        let bw30 = Color("Color-bw-30")
        let bw70 = Color("Color-bw-70")
        let bw100 = Color("Color-bw-100")
        
        let enabled = Color("Color-enabled")
        let disabled = Color("Color-disabled")
        let background = Color("Color-background")
        
        let success = Color("Color-success")
        let successDisabled = Color("Color-success-disabled")
        let danger = Color("Color-danger")
        let dangerDisabled = Color("Color-danger-disabled")
    }
}

enum AppSound {
    static let carHorn = UNNotificationSound(named: UNNotificationSoundName("car-horn.wav"))
}

struct Spacing {
    static let normal: CGFloat = 20
    static let small: CGFloat = 10
}

enum GenericError: Error {
    case VisitorNotFound
    case InvalidDate
}
