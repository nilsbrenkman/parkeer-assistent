//
//  ParkingDetailView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 03/07/2021.
//

import SwiftUI

@MainActor
struct ParkingDetailView: View {
    
    @EnvironmentObject var user: User
    @Environment(\.presentationMode) var presentationMode: Binding<PresentationMode>
    
    var parking: Parking
    
    var body: some View {
        Form {

            Section {
                VStack(alignment: .leading, spacing: Constants.spacing.normal) {
                    LicenseView(license: parking.license)
                        .centered()

                    Property(label: Lang.Visitor.name.localized(), text: parking.name ?? "")
                    Property(label: Lang.Parking.cost.localized(), text: "€ \(Util.formatCost(parking.cost))")
                    Property(label: Lang.Parking.startTime.localized(), text: Util.getParkingTime(parking.startTime))
                    Property(label: Lang.Parking.endTime.localized(), text: Util.getParkingTime(parking.endTime))
                }
                .padding(.vertical)
            }
            
            Section {
                Button(action: {
                    Task {
                        await user.stopParking(parking)
                    }
                    self.presentationMode.wrappedValue.dismiss()
                }){
                    Text(Lang.Parking.stop.localized())
                        .font(.title3)
                        .bold()
                        .centered()
                }
                .style(.danger)
                
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

struct ParkingDetailView_Previews: PreviewProvider {
    static var previews: some View {
        ParkingDetailView(parking: Parking(id: 0, license: "12-AB-CD", startTime:"", endTime: "", cost: 12.34))
    }
}
