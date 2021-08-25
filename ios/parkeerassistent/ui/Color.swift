//
//  Color.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 03/08/2021.
//

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

        let info = Color("Color-info")
        let infoDisabled = Color("Color-info-disabled")
        let success = Color("Color-success")
        let successDisabled = Color("Color-success-disabled")
        let warning = Color("Color-warning")
        let warningDisabled = Color("Color-warning-disabled")
        let danger = Color("Color-danger")
        let dangerDisabled = Color("Color-danger-disabled")
        
        let grey50 = Color("Color-grey50")
        let grey70 = Color("Color-grey70")
        let grey80 = Color("Color-grey80")
        let grey90 = Color("Color-grey90")

    }
}
