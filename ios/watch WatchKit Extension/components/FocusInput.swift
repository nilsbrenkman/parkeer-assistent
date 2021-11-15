//
//  CrownInput.swift
//  watch WatchKit Extension
//
//  Created by Nils Brenkman on 23/10/2021.
//

import SwiftUI

struct FocusInput: ViewModifier {
    
    @FocusState private var focused: Bool
    
    func body(content: Content) -> some View {
        ZStack{
            RoundedRectangle(cornerRadius: Constants.radius.small, style: .continuous)
                .fill(focused ? Color.ui.focusBg : Color.ui.noFocusBg)
                .frame(height: Constants.license.height)
            content
                .font(.title3)
                .foregroundColor(focused ? Color.ui.focus : Color.ui.noFocus)
        }
        .focusable()
        .focused($focused)
    }
    
}

extension View {
    func focusInput() -> some View {
        self.modifier(FocusInput())
    }
}
