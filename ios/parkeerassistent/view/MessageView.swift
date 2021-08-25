//
//  MessageView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 03/07/2021.
//

import SwiftUI

struct MessageView: ViewModifier {
    
    @Binding var message: Message?
    
    func body(content: Content) -> some View {
        ZStack {
            content
            
            if let msg = message {
                ZStack {
                    Rectangle()
                        .fill(Color.ui.background)
                    
                    VStack(alignment: .center, spacing: 0) {
                        Text(msg.message)
                            .accessibilityIdentifier("message")
                            .padding(.all, 40)
                        
                        VStack {
                            Button(action: {
                                DispatchQueue.main.async {
                                    if let ok = self.message?.ok {
                                        ok()
                                    }
                                    self.message = nil
                                }
                            }) {
                                Text(Lang.Common.ok.localized())
                                    .bold()
                                    .foregroundColor(Color.ui.enabled)
                                    .centered()
                            }
                            .frame(height: 42)
                            .background(
                                ZStack {
                                    VStack(alignment: .center, spacing: 0) {
                                        Rectangle()
                                            .fill(msg.type.color())
                                            .frame(height: 21)
                                        RoundedRectangle(cornerRadius: Constants.radius.normal, style: .continuous)
                                            .fill(msg.type.color())
                                            .frame(height: 21)
                                    }
                                    RoundedRectangle(cornerRadius: Constants.radius.normal, style: .continuous)
                                        .fill(msg.type.color())
                                        .frame(height: 42)
                                }
                            )
                        }
                    }
                    .background(
                        RoundedRectangle(cornerRadius: Constants.radius.normal, style: .continuous)
                            .fill(Color.ui.bw100)
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: Constants.radius.normal, style: .continuous)
                            .stroke(msg.type.color(), lineWidth: 1)
                    )
                    .padding(.horizontal)
                }
            }
        }
    }
    
}

extension View {
    func message(message: Binding<Message?>) -> some View {
        self.modifier(MessageView(message: message))
    }
}
