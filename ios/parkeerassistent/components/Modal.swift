//
//  Modal.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 03/08/2021.
//

import SwiftUI

struct Modal<M: View>: ViewModifier {
    
    @Binding var visible: Bool
    var onClose: (() -> Void)?
    @ViewBuilder var modalContent: () -> M
    
    func body(content: Content) -> some View {
        ZStack {
            content
            
            if visible {
                ZStack {
                    Color.gray.opacity(0.5)
                        .onTapGesture {
                            onClose?()
                            visible = false
                        }
                    modalContent()
                }
                .background(Color.clear)
                .ignoresSafeArea()
            }
        }
    }
}

extension View {
    func modal<M>(visible: Binding<Bool>, onClose: (() -> Void)? = nil, @ViewBuilder _ modalContent: @escaping () -> M) -> some View where M: View {
        self.modifier(Modal(visible: visible, onClose: onClose, modalContent: modalContent))
    }
}
