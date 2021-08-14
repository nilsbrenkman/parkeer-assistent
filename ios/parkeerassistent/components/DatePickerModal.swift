//
//  DatePickerModal.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 03/08/2021.
//

import SwiftUI

struct DatePickerModal: View {
    
    @Binding var date: Date

    var body: some View {
        VStack {
            VStack {
                DatePicker(
                    "",
                    selection: $date,
                    in: Date.now()...,
                    displayedComponents: [.date]
                )
                .datePickerStyle(GraphicalDatePickerStyle())
                .padding()
            }
            .background(Color.ui.bw100)
            .cornerRadius(Constants.radius.normal)
        }
        .cornerRadius(Constants.radius.normal)
        .padding(.horizontal)
    }
  
}

struct DatePickerModal_Previews: PreviewProvider {
    @State private static var date = Date()
    static var previews: some View {
        DatePickerModal(date: $date)
    }
}
