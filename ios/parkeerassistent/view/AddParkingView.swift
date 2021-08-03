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

    @State private var minutes = 0
    @State private var startDate = Date()
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
                        DataBox(title: "Datum:", content: "\(Util.dayMonthFormatter.string(from: startDate))")
                            .onTapGesture {
                                showDatePicker.toggle()
                            }
                            .opacity(opacity(enabled: showDatePicker))
                        DataBox(title: "Start tijd:", content: "\(startTime)")
                            .onTapGesture {
                                self.modifyStartTime = true
                            }
                            .opacity(opacity(enabled: modifyStartTime))
                        DataBox(title: "", content: "").hidden()
                    }

                    HStack(alignment: .center, spacing: Constants.spacing.normal) {
                        DataBox(title: "Minuten:", content: "\(minutes)")
                        DataBox(title: "Eind tijd:", content: "\(endTime)")
                        DataBox(title: "Kosten:", content: "€ \(cost)")
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
                        wait = true
                        user.startParking(visitor, timeMinutes: minutes, start: startDate) {
                            wait = false
                        }
                    }
                }){
                    Text("Toevoegen")
                        .font(.title3)
                        .bold()
                        .wait($wait)
                }
                .buttonStyle(ButtonStyles.Enabled(main: Color.ui.success,
                                                  disabled: Color.ui.successDisabled,
                                                  enabled: minutes > 0))
                
                Button(action: { user.selectedVisitor = nil }) {
                    ZStack {
                        Text("Annuleren")
                            .font(.title3)
                            .bold()
                            .centered()

                    }
                    .padding(.horizontal)
                }
                .buttonStyle(ButtonStyles.cancel)
            }
        }
        .modal(visible: $showDatePicker, onClose: updateRegime) {
            DatePickerModal(date: $startDate)
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
        user.getRegime(startDate) {
            update()
        }
    }
   
    private func minimumStartTime() -> Date {
        if !today() {
            return regimeStartTime()
        }
        if regimeStartTime() > Date() {
            return regimeStartTime()
        }
        if regimeEndTime() < Date() {
            return regimeEndTime()
        }
        return Date()
    }
    
    private func today() -> Bool {
        return Calendar.current.isDateInToday(startDate)
    }
    
    private func regimeStartTime() -> Date {
        return user.regimeTimeStart ?? Date()
    }
    
    private func regimeEndTime() -> Date {
        return user.regimeTimeEnd ?? Date()
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
