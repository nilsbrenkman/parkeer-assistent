//
//  DataBoxView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 29/06/2021.
//

import SwiftUI

struct DataBox: View  {
    
    var title: String
    var content: String
    
    var body: some View {
        
        VStack(alignment: .leading, spacing: Constants.spacing.xSmall) {
            Text("\(title):")
                .font(Font.ui.dataBoxTitle)
            ZStack {
                RoundedRectangle(cornerRadius: Constants.radius.small, style: .continuous)
                    .fill(Color.ui.bw70)
                    .frame(height: 48)
                    .overlay(RoundedRectangle(cornerRadius: Constants.radius.small, style: .continuous)
                                .stroke(Color.ui.bw0, lineWidth: 1))
                
                Text(content)
                    .font(Font.ui.dataBoxContent)
                
            }
        }
        
    }
}

struct DataBox_Previews: PreviewProvider {
    static var previews: some View {
        DataBox(title: "Title", content: "12.34")
    }
}
