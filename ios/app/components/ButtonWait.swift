//
//  ButtonWai.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 03/08/2021.
//

import SwiftUI

struct ButtonWait: ViewModifier {
    
    @Binding var wait: Bool

    func body(content: Content) -> some View {
        ZStack {
            if wait {
                ProgressView()
            } else {
                content
            }
        }
        .centered()
        .animation(nil, value: 0)
    }
    
}

extension View {
    func wait(_ wait: Binding<Bool>) -> some View {
        self.modifier(ButtonWait(wait: wait))
    }
}
