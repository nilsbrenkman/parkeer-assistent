//
//  ParkingResponseView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 27/07/2021.
//

import SwiftUI

@MainActor
struct ParkingView: View {
    
    @EnvironmentObject var user: User

    var body: some View {
        
        Section(header: SectionHeader(Lang.Parking.header.localized())) {
            if let parking = $user.parking.wrappedValue {
                if parking.active.isEmpty && parking.scheduled.isEmpty {
                    Text(Lang.Parking.noSessions.localized())
                        .foregroundStyle(.secondary)
                        .padding(.vertical, Constants.padding.mini)
                        .centered()
                } else {
                    if !parking.active.isEmpty {
                        ParkingListView(title: Lang.Parking.active.localized(), parkingList: parking.active)
                    }
                    if !parking.scheduled.isEmpty {
                        ParkingListView(title: Lang.Parking.scheduled.localized(), parkingList: parking.scheduled)
                    }
                }
            } else {
                ProgressView()
                    .centered()
            }
        }
        .id(UUID())
        
    }
    
}
