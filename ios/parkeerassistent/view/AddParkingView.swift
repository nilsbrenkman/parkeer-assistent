//
//  AddParkingView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 27/06/2021.
//

import SwiftUI

struct AddParkingView: View {
    
    @EnvironmentObject var user: User
    
    var visitor: Visitor
    
    let config = Config(
        radius: 50,
        size: 12
    )
    
    @State private var minutes = 0
    @State private var minutesBegin = 0
    @State private var endTime = Util.calculateEndTime(minutes: 0)
    @State private var cost = "0.00"

    @State private var wait: Bool = false
    @State private var message: String?

    var body: some View {
        Form {
            Section {
                
                VStack(alignment:.center, spacing: 20) {
                    
                    LicenseView(license: visitor.formattedLicense)
                    
                    Text("\(visitor.name ?? "")")
                        .font(.title3)
                        .bold()
                    
                    HStack(alignment: .center, spacing: 20) {
                        DataBoxView(title: "Minutes:", content: "\(minutes)")
                        DataBoxView(title: "Eindtijd:", content: "\(endTime)")
                        DataBoxView(title: "Kosten:", content: "€ \(cost)")
                    }
                    
                    WheelSelector(config: Config(
                        radius: 75,
                        size: 16
                    ), onChange: self.onChange, onEnd: {
                        minutesBegin = minutes
                    })
                }
                .padding(.vertical, 20)
            }

            Section {
                Button(action: {
                    if !wait && minutes > 0 {
                        wait = true
                        user.startParking(visitor, timeMinutes: minutes) {
                            wait = false
                        }
                    }
                }){
                    if wait {
                        ProgressView()
                            .centered()
                    } else {
                        Text("Toevoegen")
                            .font(.title3)
                            .bold()
                            .centered()
                    }
                }
                .color(AppColor.success, enabled: minutes > 0)
                
                Button(action: { user.selectedVisitor = nil }) {
                    Text("Annuleren")
                        .font(.title3)
                        .bold()
                        .centered()
                }
                .foregroundColor(AppColor.danger.main)
            }
        }
        .navigationBarHidden(true)
    }
    
    private func onChange(value: Int) {
        let minutesVal = minutesBegin + value
        if minutesVal < 0 {
            minutes = 0
            minutesBegin = 0 - value
        } else if minutesVal > user.timeBalance {
            minutes = user.timeBalance
            print("time balance: \(user.timeBalance)")
        } else if let regimeTimeEnd = user.regimeTimeEnd {
            let endTime = Date(timeIntervalSinceNow: TimeInterval(minutesVal*60))
            if endTime > regimeTimeEnd {
                let minutesToEnd = Int(regimeTimeEnd.timeIntervalSinceNow / 60)
                minutes = minutesToEnd
                minutesBegin = minutesBegin - (minutesVal - minutesToEnd)
                print("end time exceeded")
            } else {
                minutes = minutesVal
            }
        } else {
            minutes = minutesVal
        }
        endTime = Util.calculateEndTime(minutes: minutes)
        cost = Util.calculateCost(minutes: minutes, hourRate: user.hourRate)
    }
    
//    private func handleStartParking(success: Bool) {
//        if success {
//            DispatchQueue.main.async {
//                user.selectedVisitor = nil
//                user.loadingParking = true
//            }
//            user.getParking()
//            user.getBalance()
//        } else {
//            self.message = "Starten parkeer sessie mislukt"
//            wait = false
//        }
//    }
    
}

struct AddParkingView_Previews: PreviewProvider {
    static var previews: some View {
        let visitor = Visitor(visitorId: 1, permitId: 1, license: "12-AB-34", formattedLicense: "12-AB-34", name: "Jan")
        AddParkingView(visitor: visitor)
    }
}
