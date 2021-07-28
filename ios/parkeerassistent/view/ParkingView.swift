//
//  ParkingResponseView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 27/07/2021.
//

import SwiftUI

struct ParkingView: View {
    
    var parking: ParkingResponse?
    
    var body: some View {
        
        Section {
            Text("Parkeren:")
                .font(.title2)
                .bold()
            
            if let parking = parking {
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

struct ParkingResponseView_Previews: PreviewProvider {
    static var previews: some View {
        ParkingView(parking: ParkingResponse(active: [], scheduled: []))
    }
}
