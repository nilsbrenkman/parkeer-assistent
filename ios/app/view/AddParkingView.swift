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
    
    let timer = Timer.publish(every: 10, on: .main, in: .common).autoconnect()
    let calendarDelegate = CalenderViewDelegate()

    @State private var minutes = 0
    @State private var startDate = Date.now()
    @State private var startTime = ""
    @State private var endTime = ""
    @State private var cost = "0.00"

    @State private var customStartDate = false
    @State private var modifyStartTime = false
    @State private var showDatePicker = false
    @State private var wait: Bool = false

    var body: some View {
        Form {
            Section {
                
                VStack(alignment:.center, spacing: Constants.spacing.normal) {
                                        
                    VisitorView(visitor: visitor)
                    
                    HStack(alignment: .center, spacing: Constants.spacing.normal) {
                        DataBox(title: Lang.Parking.date.localized(), content: "\(Util.dayMonthFormatter.string(from: startDate))")
                            .onTapGesture {
                                calendarDelegate.onSelectDate = { d in
                                    Log.info("Selected date: \(Util.dateFormatter.string(from: d))")
                                    startDate = d
                                    updateRegime()
                                    showDatePicker = false
                                }
                                calendarDelegate.regime = user.regime
                                showDatePicker.toggle()
                            }
                            .opacity(opacity(enabled: showDatePicker))
                            .accessibility(identifier: "start-date")
                        DataBox(title: Lang.Parking.startTime.localized(), content: "\(startTime)")
                            .onTapGesture {
                                self.modifyStartTime = true
                            }
                            .opacity(opacity(enabled: modifyStartTime))
                            .accessibility(identifier: "start-time")
                        DataBox(title: "", content: "").hidden()
                    }

                    HStack(alignment: .center, spacing: Constants.spacing.normal) {
                        DataBox(title: Lang.Parking.minutes.localized(), content: "\(minutes)")
                        DataBox(title: Lang.Parking.endTime.localized(), content: "\(endTime)")
                        DataBox(title: Lang.Parking.cost.localized(), content: "€ \(cost)")
                    }
                    .onTapGesture(perform: {
                        self.modifyStartTime = false
                    })
                    .opacity(opacity(enabled: !showDatePicker && !modifyStartTime))

                    WheelSelector(config: Config(
                        radius: 75,
                        size: 16
                    ), onChange: self.onChange)
                }
                .padding(.vertical, Constants.padding.normal)
            }

            Section {
                Button(action: {
                    if !wait && minutes > 0 {
                        Task {
                            wait = true
                            await user.startParking(visitor, timeMinutes: minutes, start: startDate)
                            wait = false
                        }
                    }
                }){
                    Text(Lang.Common.add.localized())
                        .font(.title3)
                        .bold()
                        .wait($wait)
                }
                .style(.success, disabled: minutes <= 0)
                
                Button(action: { user.selectedVisitor = nil }) {
                    ZStack {
                        Text(Lang.Common.cancel.localized())
                            .font(.title3)
                            .bold()
                            .centered()

                    }
                    .padding(.horizontal)
                }
                .style(.cancel)
            }
        }
        .modal(visible: $showDatePicker, onClose: updateRegime) {
            DatePickerModal(date: $startDate, delegate: calendarDelegate)
        }
        .navigationBarHidden(true)
        .onAppear(perform: {
            update()
        })
        .onReceive(timer, perform: { _ in
            if !showDatePicker {
                update()
            }
        })
    }
    
    private func onChange(diff: Int) -> Void {
        if modifyStartTime {
            customStartDate = true
            startDate.addTimeInterval(Double(diff * 60))
            update()
            return
        }
        let minutesVal = minutes + diff
        if minutesVal < 0 {
            minutes = 0
        } else if minutesVal > user.timeBalance {
            minutes = user.timeBalance
        } else {
            updateMinutes(minutesVal)
        }
        update()
    }
    
    private func update() {
        updateStartDate(customStartDate ? startDate : minimumStartTime())
        startTime = Util.timeFormatter.string(from: startDate)
        updateMinutes(minutes)
        let endDate = Date(timeInterval: TimeInterval(minutes * 60), since: startDate)
        endTime = Util.timeFormatter.string(from: endDate)
    }
    
    private func updateMinutes(_ minutes: Int) {
        let endTime = Date(timeInterval: TimeInterval(minutes*60), since: startDate)
        if endTime > regimeEndTime() {
            let minutesToEnd = Int((regimeEndTime().timeIntervalSince(startDate) / 60).rounded(.up))
            self.minutes = minutesToEnd
        } else {
            self.minutes = minutes
        }
        cost = Util.calculateCost(minutes: self.minutes, hourRate: user.hourRate)
    }

    private func updateStartDate(_ start: Date) {
        if start > regimeEndTime() {
            startDate = regimeEndTime()
            return
        }
        if start > minimumStartTime() {
            startDate = start
            customStartDate = true
        } else {
            startDate = minimumStartTime()
            customStartDate = false
        }
    }
    
    private func updateRegime() {
        Task {
            await user.getRegime(startDate)
            update()
        }
    }
   
    private func minimumStartTime() -> Date {
        if !today() {
            return regimeStartTime()
        }
        if regimeStartTime() > Date.now() {
            return regimeStartTime()
        }
        if regimeEndTime() < Date.now() {
            return regimeEndTime()
        }
        return Date.now()
    }
    
    private func today() -> Bool {
        return Calendar.current.isDate(startDate, inSameDayAs: Date.now())
    }
    
    private func regimeStartTime() -> Date {
        return user.regimeTimeStart ?? Date.now()
    }
    
    private func regimeEndTime() -> Date {
        return user.regimeTimeEnd ?? Date.now()
    }

    private func opacity(enabled: Bool) -> Double {
        if enabled {
            return 1.0
        }
        return 0.5
    }


    
}

struct AddParkingView_Previews: PreviewProvider {
    static var previews: some View {
        let visitor = Visitor(visitorId: 1, permitId: 1, license: "12-AB-34", formattedLicense: "12-AB-34", name: "Jan")
        AddParkingView(visitor: visitor)
    }
}
