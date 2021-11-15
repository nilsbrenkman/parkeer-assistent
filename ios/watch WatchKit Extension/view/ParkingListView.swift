//
//  ParkingListView.swift
//  watch WatchKit Extension
//
//  Created by Nils Brenkman on 16/10/2021.
//

import SwiftUI

struct ParkingListView: View {
    
    @EnvironmentObject var user: User
    
    var body: some View {
        if let parking = $user.parking.wrappedValue {
            if parking.active.isEmpty && parking.scheduled.isEmpty {
                Text(Lang.Parking.noSessions.localized())
                    .centered()
            } else {
                List {
                    if !parking.active.isEmpty {
                        ForEach(parking.active, id: \.self) { p in
                            ParkingView(parking: p, active: true)
                        }
                    }
                    if !parking.scheduled.isEmpty {
                        ForEach(parking.scheduled, id: \.self) { p in
                            ParkingView(parking: p, active: false)
                        }
                    }
                }
                .navigationTitle(Lang.Parking.header.localized())
                .frame(minHeight: 200, maxHeight: .infinity)
            }
        } else {
            ProgressView()
                .centered()
        }
    }
}

struct ParkingListView_Previews: PreviewProvider {
    static var previews: some View {
        ParkingListView()
    }
}
