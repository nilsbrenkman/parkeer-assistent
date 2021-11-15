//
//  Centered.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 03/08/2021.
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
