//
//  VisitorListView.swift
//  watch WatchKit Extension
//
//  Created by Nils Brenkman on 16/10/2021.
//

import SwiftUI

struct VisitorListView: View {
    
    @EnvironmentObject var user: User
    
    var body: some View {
        if let visitors = $user.visitors.wrappedValue {
            if visitors.isEmpty {
                Text(Lang.Visitor.noVisitors.localized())
                    .centered()
            } else {
                List(visitors, id: \.self) { visitor in
                    
                    NavigationLink(destination: AddParkingView(visitor: visitor)) {
                        VisitorView(visitor: visitor)
                    }
                    .listRowBackground(
                        RoundedRectangle(cornerRadius: Constants.radius.normal, style: .continuous)
                            .fill(Color.ui.header)
                    )
//                    Button(action: { self.user.selectedVisitor = visitor }) {
//                        ZStack {
//                            NavigationLink(destination: AddParkingView(visitor: visitor),
//                                           tag: visitor,
//                                           selection: $user.selectedVisitor) {
//                                EmptyView()
//                            }
//                            .hidden()
//
//                            HStack {
//                                VisitorView(visitor: visitor)
//                                Spacer()
//                            }
//                        }
//                    }
                }
                .navigationTitle(Lang.Visitor.header.localized())
                .frame(minHeight: 20, maxHeight: .infinity)
//                .id(UUID.init())
            }
        } else {
            ProgressView()
                .centered()
        }
    }
}

struct VisitorListView_Previews: PreviewProvider {
    static var previews: some View {
        VisitorListView()
    }
}
