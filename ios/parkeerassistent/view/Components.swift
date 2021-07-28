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
