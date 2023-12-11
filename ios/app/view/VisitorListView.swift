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
        
        Section(header: SectionHeader(Lang.Visitor.header.localized())) {
            
            if let visitors = $user.visitors.wrappedValue {
                
                if visitors.isEmpty {
                    Text(Lang.Visitor.noVisitors.localized())
                        .centered()
                } else {
                    ForEach(visitors, id: \.self) { visitor in
                        Button(action: {
                            user.selectedVisitor = visitor
                            user.page = .parking
                        }) {
                            HStack {
                                VisitorView(visitor: visitor)
                                Spacer()
                            }
                        }
                        .accessibility(identifier: "visitor")
                        .foregroundColor(Color.ui.bw0)
                        .frame(minHeight: 42)
                    }
                    .onDelete(perform: delete)
                }
            } else {
                ProgressView()
                    .centered()
            }
        }

        Section {
            Button(action: {
                if let visitors = $user.visitors.wrappedValue {
                    if visitors.count >= 9 {
                        MessageManager.instance.addMessage(Lang.Visitor.tooManyMsg.localized(), type: Type.WARN)
                        return
                    }
                }
                user.page = .visitor
            }) {
                Text(Lang.Visitor.add.localized())
                    .font(.title3)
                    .bold()
                    .centered()
            }
            .style(.success)
        }
    }
    
    func delete(at offsets: IndexSet) {
        for i in offsets {
            let visitor = user.visitors![i]
            user.visitors!.remove(at: i)
            Task {
                await user.deleteVisitor(visitor)
            }
        }
    }
    
}

struct VisitorListView_Previews: PreviewProvider {
    static var previews: some View {
        VisitorListView()
    }
}
