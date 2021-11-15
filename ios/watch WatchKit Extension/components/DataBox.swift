//
//  DataBox.swift
//  watch WatchKit Extension
//
//  Created by Nils Brenkman on 23/10/2021.
//

import SwiftUI

struct DataBox: View {
    
    var text: String
    
    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: Constants.radius.small, style: .continuous)
                .stroke(Color.ui.focusBg, lineWidth: 1)
                .frame(height: Constants.license.height)
            Text("\(text)")
                .font(.title3)

        }
    }
}

struct DataBox_Previews: PreviewProvider {
    static var previews: some View {
        DataBox(text: "000")
    }
}
