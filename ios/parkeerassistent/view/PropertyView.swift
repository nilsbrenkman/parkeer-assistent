//
//  LabeledTextView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 03/07/2021.
//

import SwiftUI

struct PropertyView: View {
    
    var label: String
    var text: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: Spacing.small) {
            Text("\(label):")
                .foregroundColor(Color.ui.bw30)
            Text(text)
                .bold()
        }
    }
}

struct PropertyView_Previews: PreviewProvider {
    static var previews: some View {
        PropertyView(label: "Label", text: "Value / text")
    }
}
