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
                            .padding(.all, 40)
                        
                        VStack {
                            Button(action: {
                                DispatchQueue.main.async {
                                    self.message = nil
                                }
                            }) {
                                Text("OK")
                                    .bold()
                                    .foregroundColor(Color.ui.enabled)
                                    .centered()
                            }
                            .frame(height: 42)
                            .background(
                                ZStack {
                                    VStack(alignment: .center, spacing: 0) {
                                        Rectangle()
                                            .fill(Color.ui.danger)
                                            .frame(height: 21)
                                        RoundedRectangle(cornerRadius: 12.0, style: .continuous)
                                            .fill(Color.ui.danger)
                                            .frame(height: 21)
                                    }
                                    RoundedRectangle(cornerRadius: 12.0, style: .continuous)
                                        .fill(Color.ui.danger)
                                        .frame(height: 42)
                                }
                            )
                        }
                    }
                    .background(
                        RoundedRectangle(cornerRadius: 12.0, style: .continuous)
                            .fill(Color.ui.bw100)
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 12.0, style: .continuous)
                            .stroke(Color.ui.danger, lineWidth: 1)
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