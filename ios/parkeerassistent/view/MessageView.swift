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
                        .fill(AppColor.background)
                    
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
                                    .foregroundColor(Color.white)
                                    .centered()
                            }
                            .frame(height: 42)
                            .background(
                                ZStack {
                                    VStack(alignment: .center, spacing: 0) {
                                        Rectangle()
                                            .fill(AppColor.danger.main)
                                            .frame(height: 21)
                                        RoundedRectangle(cornerRadius: 12.0, style: .continuous)
                                            .fill(AppColor.danger.main)
                                            .frame(height: 21)
                                    }
                                    RoundedRectangle(cornerRadius: 12.0, style: .continuous)
                                        .fill(AppColor.danger.main)
                                        .frame(height: 42)
                                }
                            )
                        }
                    }
                    .background(
                        RoundedRectangle(cornerRadius: 12.0, style: .continuous)
                            .fill(Color.white)
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 12.0, style: .continuous)
                            .stroke(Color.red, lineWidth: 1)
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
