//
//  HistoryView.swift
//  app
//
//  Created by Nils Brenkman on 16/11/2021.
//

import SwiftUI

@MainActor
struct HistoryListView: View {
    
    static let groupFormatter = Util.createDateFormatter("MMMM yyyy")

    @Environment(\.presentationMode) var presentationMode: Binding<PresentationMode>
    
    @State var history: [History]? = nil

    var body: some View {
        List {
            if let history = history {
                if history.isEmpty {
                    Section {
                        Text(Lang.Parking.noHistory.localized())
                            .centered()
                    } header: {
                        Rectangle()
                            .fill(Color.clear)
                            .frame(height: 1)
                    }
                } else {
                    let groupedHistory = groupHistory(history)
                    ForEach(groupedHistory, id: \.date) { group in
                        Section {
                            ForEach(group.history, id: \.self) { h in
                                NavigationLink(destination: HistoryView(history: h)) {
                                    HistoryRowView(history: h)
                                }
                            }
                        } header: {
                            Text(HistoryListView.groupFormatter.string(from: group.date))
                                .font(Font.ui.historyListHeader)
                        }
                    }
                }
            } else {
                Section {
                    ProgressView()
                        .centered()
                } header: {
                    Rectangle()
                        .fill(Color.clear)
                        .frame(height: 1)
                }
            }
        }
        .listStyle(InsetGroupedListStyle())
        .onAppear() {
            Task {
                let parkingClient = try! ClientManager.instance.get(ParkingClient.self)
                let response = try await parkingClient.history()
                self.history = response.history
            }
            
//            DispatchQueue.global().async {
//                parkingClient.history() { response in
//                    DispatchQueue.main.async {
//                        self.history = response.history
//                    }
//                }
//            }
        }
        .navigationBarTitle(Text(Lang.Parking.history.localized()))
        .navigationBarBackButtonHidden(true)
        .navigationBarItems(leading:
            Button(action: { self.presentationMode.wrappedValue.dismiss() }) {
                Image(systemName: "arrow.left")
            }
        )
    }
    
    private func groupHistory(_ history: [History]) -> [HistoryGroup] {
        var list: [HistoryGroup] = []
        var group: HistoryGroup? = nil
        for h in history {
            group = addToGroup(h, group: group, list: &list)
        }
        if group != nil {
            list.append(group!)
        }
        return list
    }
    
    private func addToGroup(_ history: History, group: HistoryGroup?, list: inout [HistoryGroup]) -> HistoryGroup {
        guard var group = group else {
            return HistoryGroup(date: history.date, history: [history])
        }
        if Calendar.current.isDate(history.date, equalTo: group.date, toGranularity: .month) {
            group.history.append(history)
            return group
        }
        list.append(group)
        return HistoryGroup(date: history.date, history: [history])
    }
    
}

struct HistoryGroup {
    var date: Date
    var history: [History]
}

struct HistoryView_Previews: PreviewProvider {
    static var previews: some View {
        HistoryListView()
    }
}
