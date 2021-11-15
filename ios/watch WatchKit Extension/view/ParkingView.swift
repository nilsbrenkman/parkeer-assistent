//
//  ParkingView.swift
//  watch WatchKit Extension
//
//  Created by Nils Brenkman on 16/10/2021.
//

import SwiftUI

struct ParkingView: View {
    
    @EnvironmentObject var user: User
    
    var parking: Parking
    var active: Bool
    
    @State private var showAlert: Bool = false
    
    var body: some View {
        ZStack {
            Color.clear
            
            VStack(alignment: .center, spacing: 0) {
                let visitor = user.getVisitor(parking)
                
                LicenseView(license: visitor?.license ?? parking.license)
                VisitorNameView(name: visitor?.name)
                
                if active {
                    if let timer = try? Util.parseDate(parking.endTime) {
                        TimerView(endTime: timer)
                            .foregroundColor(Color.white)
                    }
                } else {
                    Text(formatScheduledDate())
                        .foregroundColor(Color.white)
                        .minimumScaleFactor(0.5)
                        .lineLimit(1)
                }
            }
        }
        .listRowBackground(
            RoundedRectangle(cornerRadius: Constants.radius.normal, style: .continuous)
                .fill(Color.ui.header)
        )
        .padding(.vertical, Constants.padding.xSmall)
        .alert(isPresented: $showAlert) {
            Alert(
                title: Text(Lang.Parking.stop.localized()),
                primaryButton: .destructive(Text(Lang.Common.stop.localized())) {
                    user.stopParking(parking)
                },
                secondaryButton: .cancel(Text(Lang.Common.cancel.localized()))
            )
        }
        .gesture(TapGesture(count: 2).onEnded {
            showAlert = true
        })
    }
    
    private func formatScheduledDate() -> String {
        guard let day = try? Util.convertDate(parking.startTime, formatter: Util.dayMonthFormatter),
              let start = try? Util.convertDate(parking.startTime, formatter: Util.timeFormatter),
              let end = try? Util.convertDate(parking.endTime, formatter: Util.timeFormatter) else {
                  return ""
              }
        return "\(day) \(start) - \(end)"
    }
}

struct ParkingView_Previews: PreviewProvider {
    static var previews: some View {
        ParkingView(parking: Parking(id: 0, license: "12-AB-CD", startTime:"2021-10-01T12:34:56+02:00", endTime: "2021-10-01T14:34:56+02:00", cost: 12.34), active: true)
    }
}
