//
//  DatePickerModal.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 03/08/2021.
//

import SwiftUI

struct DatePickerModal: View {
    
    @Binding var date: Date
    var delegate: CalenderViewDelegate
        
    var body: some View {
        VStack {
            VStack {
                if #available(iOS 16.0, *) {
                    HStack {
                        CalendarView(date: $date, delegate: delegate)
                            .scaledToFit()
                    }
                    .padding(.all, Constants.padding.normal)
                } else {
                    DatePicker(
                        "",
                        selection: $date,
                        in: Date.now()...,
                        displayedComponents: [.date]
                    )
                    .datePickerStyle(GraphicalDatePickerStyle())
                    .padding()
                }
            }
            .background(.background)
            .cornerRadius(Constants.radius.normal)
        }
        .cornerRadius(Constants.radius.normal)
        .padding(.horizontal)
    }
  
}

struct DatePickerModal_Previews: PreviewProvider {
    @State private static var date = Date()
    static var previews: some View {
        DatePickerModal(date: $date, delegate: CalenderViewDelegate())
    }
}

public class CalenderViewDelegate: NSObject, UICalendarSelectionSingleDateDelegate {

    var onSelectDate: ((Date) -> Void)? = nil
    var regime: Regime? = nil
    
    func setOnSelectDate(_ onSelectDate: @escaping ((Date) -> Void)) {
        self.onSelectDate = onSelectDate
    }
    
    @available(iOS 16.0, *)
    public func dateSelection(_ selection: UICalendarSelectionSingleDate, didSelectDate dateComponents: DateComponents?) {
        
        guard let dateComponents,
              let date = Calendar.current.date(from: dateComponents) else { return }
        
        onSelectDate?(date)
    }
    @available(iOS 16.0, *)
    public func dateSelection(_ selection: UICalendarSelectionSingleDate, canSelectDate dateComponents: DateComponents?) -> Bool {
        
        guard let dateComponents,
              let date = Calendar.current.date(from: dateComponents),
              let regime else { return false }
        
//        let weekday = Calendar.current.component(Calendar.Component.weekday, from: date)
//        let weekdayStr = CalenderViewDelegate.weekdays[weekday - 1]
//        let regimeDay = regime?.days.first(where: { d in
//            d.weekday == weekdayStr
//        })
        let regimeDay = Util.getRegimeDay(regime: regime, date: date)
        return regimeDay != nil
    }
    
    static let weekdays = ["SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"]
}
