//
//  HistoryListView.swift
//  app
//
//  Created by Nils Brenkman on 16/11/2021.
//

import SwiftUI

@MainActor
struct HistoryView: View {
    
    @Environment(\.presentationMode) var presentationMode: Binding<PresentationMode>
    
    var history: History
    
    var body: some View {
        Form {
            Section {
                VStack(alignment: .leading, spacing: Constants.spacing.normal) {
                    LicenseView(license: history.license)
                        .centered()

                    Property(label: Lang.Visitor.name.localized(), text: history.name ?? "")
                    Property(label: Lang.Parking.cost.localized(), text: "€ \(Util.formatCost(history.cost))")
                    Property(label: Lang.Parking.startTime.localized(), text: Util.getParkingTime(history.startTime))
                    Property(label: Lang.Parking.endTime.localized(), text: Util.getParkingTime(history.endTime))
                }
                .padding(.vertical)
            }
            
            Section {
                Button(action: { self.presentationMode.wrappedValue.dismiss() }) {
                    Text(Lang.Common.back.localized())
                        .font(.title3)
                        .bold()
                        .centered()
                }
                .style(.cancel)
            }
        }
        .navigationBarHidden(true)
    }
}

struct HistoryListView_Previews: PreviewProvider {
    static var previews: some View {
        HistoryView(history: History(id: 0, license: "", name: "", startTime: "", endTime: "", cost: 0))
    }
}
