//
//  VisitorListView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 22/06/2021.
//

import SwiftUI
import Foundation

struct VisitorListView: View {
    
    @EnvironmentObject var user: User
    
    var body: some View {
        
        Section {
            Text("Bezoekers:")
                .font(.title2)
                .bold()
            
            if let visitors = $user.visitors.wrappedValue {
                
                if visitors.isEmpty {
                    Text("Voeg een bezoeker toe")
                        .centered()
                } else {
                    ForEach(visitors, id: \.self) { visitor in
                        Button(action: { self.user.selectedVisitor = visitor }) {
                            ZStack {
                                NavigationLink(destination: AddParkingView(visitor: visitor),
                                               tag: visitor,
                                               selection: $user.selectedVisitor) {
                                    EmptyView()
                                }
                                .hidden()

                                HStack {
                                    VisitorView(visitor: visitor)
                                    Spacer()
                                }
                            }
                        }
                        .accessibility(identifier: "visitor")
                        .foregroundColor(Color.ui.bw0)
                    }
                    .onDelete(perform: delete)
                }
                
            } else {
                ProgressView()
                    .centered()
            }
        }
        
        Section {
            Button(action: { self.user.addVisitor = true }) {
                ZStack {
                    NavigationLink(destination: AddVisitorView(), isActive: $user.addVisitor) {
                        
                    }
                    .hidden()
                    
                    Text("Nieuwe bezoeker")
                        .font(.title3)
                        .bold()
                        .centered()
                }
            }
            .buttonStyle(ButtonStyles.success)
        }

    }
    
    func delete(at offsets: IndexSet) {
        for i in offsets {
            let visitor = user.visitors![i]
            user.visitors!.remove(at: i)
            user.deleteVisitor(visitor)
        }
    }
    
}

struct VisitorListView_Previews: PreviewProvider {
    static var previews: some View {
        VisitorListView()
    }
}
