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
                VStack(alignment: .leading, spacing: Constants.spacing.normal) {
                    LicenseView(license: visitor?.license ?? parking.license)
                        .centered()

                    Property(label: "Naam", text: visitor?.name ?? "")
                    Property(label: "Kosten", text: "€ \(Util.formatCost(parking.cost))")
                    Property(label: "Start tijd", text: Util.getParkingTime(parking.startTime))
                    Property(label: "Eind tijd", text: Util.getParkingTime(parking.endTime))
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
                .buttonStyle(ButtonStyles.danger)
                
                Button(action: { self.presentationMode.wrappedValue.dismiss() }) {
                    Text("Terug")
                        .font(.title3)
                        .bold()
                        .centered()
                }
                .buttonStyle(ButtonStyles.cancel)

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
