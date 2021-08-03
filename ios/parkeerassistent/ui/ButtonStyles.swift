//
//  ButtonStyles.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 03/08/2021.
//

import SwiftUI

struct ButtonStyles {
    
    static let success = ButtonStyles.Static(main: Color.ui.success)
    static let danger  = ButtonStyles.Static(main: Color.ui.danger)
    static let cancel  = Cancel()

    struct Static: ButtonStyle {
        var main: Color
        
        func makeBody(configuration: Configuration) -> some View {
            configuration.label
                .foregroundColor(Color.ui.enabled)
                .listRowBackground(main)
        }
    }

    struct Enabled: ButtonStyle {
        var main: Color
        var disabled: Color
        var enabled: Bool
        
        func makeBody(configuration: Configuration) -> some View {
            if enabled {
                configuration.label
                    .foregroundColor(Color.ui.enabled)
                    .listRowBackground(main)
            } else {
                configuration.label
                    .foregroundColor(Color.ui.disabled)
                    .listRowBackground(disabled)
            }
        }
    }
    
    struct Cancel: ButtonStyle {
        func makeBody(configuration: Configuration) -> some View {
            configuration.label
                .foregroundColor(Color.ui.danger)
                .listRowBackground(Color.ui.light)
        }
    }
    
}
