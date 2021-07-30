//
//  Components.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 28/07/2021.
//

import SwiftUI

struct Centered: ViewModifier {
    
    func body(content: Content) -> some View {
        HStack {
            Spacer()
            content
            Spacer()
        }
    }
    
}

extension View {
    func centered() -> some View {
        self.modifier(Centered())
    }
}

struct ButtonView: ViewModifier {
    
    var main: Color
    var disabled: Color
    var enabled: Bool
    
    func body(content: Content) -> some View {
        if enabled {
            content.foregroundColor(Color.ui.enabled)
                   .listRowBackground(main)
        } else {
            content.foregroundColor(Color.ui.disabled)
                   .listRowBackground(disabled)
        }
    }
}

extension Button {
    func color(_ main: Color, disabled: Color, enabled: Bool = true) -> some View {
        self.modifier(ButtonView(main: main, disabled: disabled, enabled: enabled))
    }
}
