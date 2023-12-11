//
//  HistoryRowView.swift
//  app
//
//  Created by Nils Brenkman on 17/11/2021.
//

import SwiftUI

@MainActor
struct HistoryRowView: View {
    
    var history: History
    
    var body: some View {
        HStack {
            CalendarDate(date: history.date)

            LicenseView(license: License.formatLicense(history.license))
                .padding(.leading)

            Spacer()
            
            Text("€ \(Util.formatCost(history.cost))")
                .padding(.trailing)
        }
        .padding(.vertical, Constants.padding.mini)
    }
}

struct HistoryRowView_Previews: PreviewProvider {
    static var previews: some View {
        HistoryRowView(history: History(id: 0, license: "", name: "", startTime: "", endTime: "", cost: 0))
    }
}
