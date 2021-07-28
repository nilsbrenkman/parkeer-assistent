//
//  Constants.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 30/06/2021.
//

import Foundation
import SwiftUI

class AppColor {
    
    static let license    = hexColor("#F2BA00")!
    static let header     = hexColor("#007CBC")!
    static let lightGrey  = hexColor("#EEEEEE")!
    static let disabled   = hexColor("#DDDDDD")!
    static let darkGrey   = hexColor("#777777")!
    static let background = hexColor("#AAAAAAAA")!
    
    static let success = AppButton(main: "#198754", disabled: "#7BB299")
    static let danger  = AppButton(main: "#FF2601", disabled: "#EE826F")

    static func hexColor(_ hex: String) -> Color? {
        var hexColor = hex.uppercased()
        if hexColor.hasPrefix("#") {
            let start = hexColor.index(hexColor.startIndex, offsetBy: 1)
            hexColor = String(hex[start...])
        }
        if hexColor.count == 6 {
            hexColor = hexColor + "FF"
        }
        
        let scanner = Scanner(string: hexColor)
        var hexNumber: UInt64 = 0
        
        if scanner.scanHexInt64(&hexNumber) {
            let r = Double((hexNumber & 0xff000000) >> 24) / 255
            let g = Double((hexNumber & 0x00ff0000) >> 16) / 255
            let b = Double((hexNumber & 0x0000ff00) >> 8) / 255
            let a = Double(hexNumber & 0x000000ff) / 255

            return Color(red: r, green: g, blue: b, opacity: a)
        }
        return nil
    }
    
}

enum AppSound {
    static let carHorn = UNNotificationSound(named: UNNotificationSoundName("car-horn.wav"))
}

struct AppButton {
    let main: Color
    let disabled: Color

    init(main: String, disabled: String) {
        self.main = AppColor.hexColor(main)!
        self.disabled = AppColor.hexColor(disabled)!
    }
}

struct ButtonView: ViewModifier {
    
    var appButton: AppButton
    var enabled: Bool
    
    func body(content: Content) -> some View {
        if enabled {
            content.foregroundColor(Color.white)
                   .listRowBackground(appButton.main)
        } else {
            content.foregroundColor(AppColor.disabled)
                   .listRowBackground(appButton.disabled)
        }
    }
}

extension Button {
    func color(_ appButton: AppButton, enabled: Bool = true) -> some View {
        self.modifier(ButtonView(appButton: appButton, enabled: enabled))
    }
}

class Spacing {
    
    static let normal: CGFloat = 20
    static let small: CGFloat = 10

}

enum GenericError: Error {
    case VisitorNotFound
    case InvalidDate
}
