//
//  CalendarView.swift
//  app
//
//  Created by Nils Brenkman on 19/05/2023.
//

import SwiftUI

@available(iOS 16.0, *)
struct CalendarView: UIViewRepresentable {
    
    @Binding var date: Date
    var delegate: CalenderViewDelegate

    func makeUIView(context: Context) -> some UIView {
        
        var selector = UICalendarSelectionSingleDate(delegate: delegate)
        selector.selectedDate = Calendar.current.dateComponents([.year, .month, .day], from: date)
        
        let calendarView = UICalendarView()
        calendarView.availableDateRange = DateInterval(start: Date(), end: Date.distantFuture)
        calendarView.selectionBehavior = selector
        calendarView.setContentCompressionResistancePriority(.defaultLow, for: .vertical)
        calendarView.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        return calendarView
    }
    
    func updateUIView(_ uiView: UIViewType, context: Context) {
        //
    }

}

struct CalendarView_Previews: PreviewProvider {
    static var previews: some View {
        if #available(iOS 16.0, *) {
//            CalendarView()
        } else {
            EmptyView()
        }
    }
}
