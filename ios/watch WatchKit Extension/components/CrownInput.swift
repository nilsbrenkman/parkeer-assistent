//
//  CrownInput.swift
//  watch WatchKit Extension
//
//  Created by Nils Brenkman on 23/10/2021.
//

import SwiftUI

struct CrownInput: ViewModifier {
    
    var sensitivity: DigitalCrownRotationalSensitivity
    var onChange: (Int) -> Void
    
    @State private var crown = 0.0
    
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
        .digitalCrownRotation($crown, from: 0.0, through: 1000.0, by: 1.0, sensitivity: sensitivity, isContinuous: false, isHapticFeedbackEnabled: true)
        .onChange(of: crown) { [crown] newValue in
            if Int(crown) != Int(newValue) {
                let diff = Int(newValue) - Int(crown)
                onChange(diff)
            }
        }
    }
    
}

extension View {
    func crownInput(sensitivity: DigitalCrownRotationalSensitivity,
                    onChange: @escaping (Int) -> Void) -> some View {
        self.modifier(CrownInput(sensitivity: sensitivity, onChange: onChange))
    }
}
