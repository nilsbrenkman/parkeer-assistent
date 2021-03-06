//
//  LabeledTextView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 03/07/2021.
//

import SwiftUI

struct Property: View {
    
    var label: String
    var text: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: Constants.spacing.small) {
            Text("\(label):")
                .font(Font.ui.propertyLabel)
                .foregroundColor(Color.ui.bw30)
            Text(text)
                .font(Font.ui.propertyValue)
        }
    }
}

struct Property_Previews: PreviewProvider {
    static var previews: some View {
        Property(label: "Label", text: "Value / text")
    }
}
