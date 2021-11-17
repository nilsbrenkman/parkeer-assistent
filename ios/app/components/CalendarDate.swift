//
//  CalendarDate.swift
//  app
//
//  Created by Nils Brenkman on 17/11/2021.
//

import SwiftUI

struct CalendarDate: View {
    
    static let dowFormatter = Util.createDateFormatter("E")
    static let dayFormatter = Util.createDateFormatter("d")

    var date: Date
    
    var body: some View {
        VStack {
            Text(CalendarDate.dowFormatter.string(from: date).uppercased())
                .font(Font.ui.calenderDow)
                .foregroundColor(Color.ui.danger)
            Text(CalendarDate.dayFormatter.string(from: date))
                .font(Font.ui.calenderDay)
        }
        .frame(width: 40, alignment: .center)
    }
    
}

struct CalendarDate_Previews: PreviewProvider {
    static var previews: some View {
        CalendarDate(date: Date())
    }
}
