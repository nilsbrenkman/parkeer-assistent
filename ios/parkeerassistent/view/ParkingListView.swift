//
//  ParkingListView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 29/06/2021.
//

import SwiftUI

struct ParkingListView: View {
    
    @EnvironmentObject var user: User
    
    var title: String
    var parkingList: [Parking]
    
    var body: some View {
        
        Text("\(title):")
            .font(.title3)
            .centered()

        ForEach(parkingList, id: \.self) { parking in
            NavigationLink(destination: ParkingDetailView(parking: parking)) {
                ParkingRowView(parking: parking)
            }
        }
        .onDelete(perform: {offsets in
            for i in offsets {
                let parking = parkingList[i]
                user.stopParking(parking)
            }
        })
        .animation(nil)
        
    }
    
}

struct ParkingListView_Previews: PreviewProvider {
    static var previews: some View {
        ParkingListView(title: "Actief", parkingList: [])
    }
}
