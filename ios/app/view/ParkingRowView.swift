//
//  ParkingView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 29/06/2021.
//

import SwiftUI

@MainActor
struct ParkingRowView: View {
    
    @EnvironmentObject var user: User
    
    var parking: Parking
    
    var body: some View {
        
        HStack {
            LicenseView(license: parking.license)
            
            Text("\(parking.name ?? "")")
                .font(/*@START_MENU_TOKEN@*/.title3/*@END_MENU_TOKEN@*/)
                .bold()
                .padding(.leading)

        }
        .frame(minHeight: 42)
    }
    
}

struct ParkingView_Previews: PreviewProvider {
    static var previews: some View {
        ParkingRowView(parking: Parking(id: 0, license: "12-AB-CD", startTime:"", endTime: "", cost: 12.34))
    }
}
