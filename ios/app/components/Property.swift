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
        HStack {
            Text(label)
                .frame(alignment: .leading)
            Text(text)
                .foregroundStyle(.secondary)
                .frame(maxWidth: .infinity, alignment: .trailing)
        }
        .padding(.vertical, Constants.padding.mini)
    }
}

struct Property_Previews: PreviewProvider {
    static var previews: some View {
        Property(label: "Label", text: "Value / text")
    }
}
