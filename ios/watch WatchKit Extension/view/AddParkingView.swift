//
//  AddParkingView.swift
//  watch WatchKit Extension
//
//  Created by Nils Brenkman on 20/10/2021.
//

import SwiftUI

struct AddParkingView: View {
    
    @Namespace var mainNamespace
    
    @EnvironmentObject var user: User
    
    var visitor: Visitor
    
    @State private var startDate = Date()
    @State private var endDate = Date()
    @State private var minutes = 0
    @State private var cost = "0.00"
    
    @State private var dateCrown = 0.0
    @State private var startCrown = 0.0
    @State private var endCrown = 0.0


    var body: some View {
        VStack(alignment: .center, spacing: 5) {
            Text("\(Util.dowDayMonthFormatter.string(from: startDate))")
                .crownInput(sensitivity: .low) { diff in
                    if let date = Calendar.current.date(byAdding: .day, value: diff, to: startDate) {
                        startDate = date
                    }
                }
            HStack {
                Text("\(Util.timeFormatter.string(from: startDate))")
                    .crownInput(sensitivity: .medium) { diff in
                        if let date = Calendar.current.date(byAdding: .minute, value: diff, to: startDate) {
                            startDate = date
                            update()
                        }
                    }
                Text("\(Util.timeFormatter.string(from: endDate))")
                    .crownInput(sensitivity: .medium) { diff in
                        if let date = Calendar.current.date(byAdding: .minute, value: diff, to: endDate) {
                            endDate = date
                            update()
                        }
                    }
                    .prefersDefaultFocus(in: mainNamespace)

            }
            HStack {
                DataBox(text: "\(minutes)")
                DataBox(text: "€ \(cost)")
            }

        }
//        .focusScope(mainNamespace)
//        .frame(minHeight: 200, maxHeight: .infinity)
    }
    
    private func update() {
//        if endDate < startDate {
//            endDate = startDate
//        }
//        minutes = Int((endDate.timeIntervalSince(startDate) / 60).rounded(.up))
//        cost = Util.calculateCost(minutes: minutes, hourRate: user.hourRate)
    }
}

struct AddParkingView_Previews: PreviewProvider {
    static var previews: some View {
        let visitor = Visitor(visitorId: 1, permitId: 1, license: "12AB34", formattedLicense: "12-AB-34", name: "Jan")
        AddParkingView(visitor: visitor)
    }
}
