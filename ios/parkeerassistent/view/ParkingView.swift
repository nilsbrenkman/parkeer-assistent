//
//  ParkingResponseView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 27/07/2021.
//

import SwiftUI

struct ParkingView: View {
    
    @EnvironmentObject var user: User

    var body: some View {
        
        Section {
            Text("Parkeren:")
                .font(.title2)
                .bold()
            
            if let parking = $user.parking.wrappedValue {
                if parking.active.isEmpty && parking.scheduled.isEmpty {
                    Text("Geen actieve of geplande sessies")
                        .centered()
                } else {
                    if !parking.active.isEmpty {
                        ParkingListView(title: "Actieve sessies", parkingList: parking.active)
                    }
                    if !parking.scheduled.isEmpty {
                        ParkingListView(title: "Geplande sessies", parkingList: parking.scheduled)
                    }
                }
            } else {
                ProgressView()
                    .centered()
            }
        }
        .id(UUID())
        .animation(nil)
        
    }
    
}
