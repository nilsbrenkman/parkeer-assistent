//
//  ParkingListView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 29/06/2021.
//

import SwiftUI

@MainActor
struct ParkingListView: View {
    
    @EnvironmentObject var user: User
    
    var title: String
    var parkingList: [Parking]
    
    var body: some View {
        
        Section(header: SectionHeader(title)) {
            ForEach(parkingList, id: \.self) { parking in
                NavigationLink(destination: ParkingDetailView(parking: parking)) {
                    ParkingRowView(parking: parking)
                }
                .accessibility(identifier: "parking")
            }
            .onDelete { offsets in
                for i in offsets {
                    let parking = parkingList[i]
                    Task {
                        await user.stopParking(parking)
                    }
                }
            }
        }
    }
    
}

struct ParkingListView_Previews: PreviewProvider {
    static var previews: some View {
        ParkingListView(title: "Actief", parkingList: [])
    }
}
