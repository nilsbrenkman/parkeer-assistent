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
    @State private var date = Date()
    @State private var startDate: Date?
    @State private var startTime = ""
    @State private var endTime = ""
    @State private var cost = "0.00"

    @State private var modifyStartTime = false
    @State private var showDatePicker = false
    @State private var wait: Bool = false

    var body: some View {
        ZStack {
            Form {
                Section {
                    
                    VStack(alignment:.center, spacing: 20) {
                                            
                        VisitorView(visitor: visitor)
                        
                        HStack(alignment: .center, spacing: 20) {
                            DataBoxView(title: "Datum:", content: "\(Util.dateFormatter.string(from: date))")
                                .onTapGesture {
                                    showDatePicker.toggle()
                                }
                                .opacity(opacity(enabled: showDatePicker))
                            DataBoxView(title: "Start tijd:", content: "\(startTime)")
                                .onTapGesture {
                                    self.modifyStartTime = true
                                }
                                .opacity(opacity(enabled: modifyStartTime))
                            DataBoxView(title: "", content: "").hidden()
                        }

                        HStack(alignment: .center, spacing: 20) {
                            DataBoxView(title: "Minuten:", content: "\(minutes)")
                            DataBoxView(title: "Eind tijd:", content: "\(endTime)")
                            DataBoxView(title: "Kosten:", content: "€ \(cost)")
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
                    .padding(.vertical, 20)
                }

                Section {
                    Button(action: {
                        if !wait && minutes > 0 {
                            wait = true
                            user.startParking(visitor, timeMinutes: minutes, startTime: getParkingStart()) {
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
                    .color(Color.ui.success, disabled: Color.ui.successDisabled, enabled: minutes > 0)
                    
                    Button(action: { user.selectedVisitor = nil }) {
                        Text("Annuleren")
                            .font(.title3)
                            .bold()
                            .centered()
                    }
                    .foregroundColor(Color.ui.danger)
                    .listRowBackground(Color.ui.light)
                }
            }
            DatePickerView(show: $showDatePicker, date: $date, update: {
                calculateTimes()
            })
            .opacity(showDatePicker ? 1.0: 0)
        }
        .navigationBarHidden(true)
        .onAppear(perform: calculateTimes)
        .onReceive(timer, perform: { _ in
            calculateTimes()
        })
    }
    
    private func onChange(diff: Int) -> Void {
        if modifyStartTime {
            onChangeStartTime(diff)
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
        cost = Util.calculateCost(minutes: minutes, hourRate: user.hourRate)
        calculateTimes()
    }
    
    private func updateMinutes(_ minutes: Int) {
        let endTime = Date(timeInterval: TimeInterval(minutes*60), since: getStartDate())
        if endTime > regimeEndTime() {
            let minutesToEnd = Int((regimeEndTime().timeIntervalSince(getStartDate()) / 60).rounded(.up))
            self.minutes = minutesToEnd
        } else {
            self.minutes = minutes
        }
    }

    private func onChangeStartTime(_ diff: Int) {
        var start = startDate ?? minimumStartTime()
        start.addTimeInterval(Double(diff * 60))
        if start > regimeEndTime() {
            startDate = regimeEndTime()
            return
        }
        if start > minimumStartTime() {
            startDate = start
        } else {
            startDate = nil
        }
        calculateTimes()
    }

    private func calculateTimes() {
        let start = getStartDate()
        startTime = Util.timeFormatter.string(from: start)
        updateMinutes(minutes)
        let endDate = Date(timeInterval: TimeInterval(minutes * 60), since: start)
        endTime = Util.timeFormatter.string(from: endDate)
    }
    
    private func getParkingStart() -> Date? {
        if today() {
            if let start = startDate {
                return start
            }
            if minimumStartTime() > Date() {
                return minimumStartTime()
            }
            return startDate
        }
        var start = startDate ?? minimumStartTime()
        let diff = Calendar.current.startOfDay(for: start).distance(to: Calendar.current.startOfDay(for: date))
        start.addTimeInterval(diff)
        return start
    }
    
    private func getStartDate() -> Date {
        if let startDate = startDate {
            let minimumStartTime = minimumStartTime()
            if startDate < minimumStartTime {
                self.startDate = minimumStartTime
                return minimumStartTime
            }
            return startDate
        }
        return minimumStartTime()
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
        return Calendar.current.isDateInToday(date)
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
