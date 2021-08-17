//
//  ParkingView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 29/06/2021.
//

import SwiftUI

struct ParkingRowView: View {
    
    @EnvironmentObject var user: User
    
    var parking: Parking
    
    var body: some View {
        
        HStack {
            let visitor = user.getVisitor(parking)
            
            LicenseView(license: visitor?.license ?? parking.license)
            
            Text("\(visitor?.name ?? "")")
                .font(/*@START_MENU_TOKEN@*/.title3/*@END_MENU_TOKEN@*/)
                .bold()
                .padding(.leading)

        }
        
    }
    
}

struct ParkingView_Previews: PreviewProvider {
    static var previews: some View {
        ParkingRowView(parking: Parking(id: 0, license: "12-AB-CD", startTime:"", endTime: "", cost: 12.34))
    }
}
