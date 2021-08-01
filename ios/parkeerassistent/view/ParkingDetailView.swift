//
//  ParkingDetailView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 03/07/2021.
//

import SwiftUI

struct ParkingDetailView: View {
    
    @EnvironmentObject var user: User
    @Environment(\.presentationMode) var presentationMode: Binding<PresentationMode>
    
    var parking: Parking
    
    var body: some View {
        let visitor = user.getVisitor(parking)
        
        Form {

            Section {
                VStack(alignment: .leading, spacing: Spacing.normal) {
                    LicenseView(license: visitor?.formattedLicense ?? parking.license)
                        .centered()

                    PropertyView(label: "Naam", text: visitor?.name ?? "?")
                    PropertyView(label: "Kosten", text: "€ \(Util.formatCost(cost:parking.cost))")
                    PropertyView(label: "Start tijd", text: Util.getParkingTime(parking.startTime))
                    PropertyView(label: "Eind tijd", text: Util.getParkingTime(parking.endTime))
                }
                .padding(.vertical)
            }
            
            Section {
                Button(action: {
                    user.stopParking(parking)
                    self.presentationMode.wrappedValue.dismiss()
                }){
                    Text("Sessie beindigen")
                        .font(.title3)
                        .bold()
                        .centered()
                }
                .color(Color.ui.danger, disabled: Color.ui.dangerDisabled)
                
                Button(action: { self.presentationMode.wrappedValue.dismiss() }) {
                    Text("Terug")
                        .font(.title3)
                        .bold()
                        .centered()
                }
                .foregroundColor(Color.ui.danger)
                .listRowBackground(Color.ui.light)

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
