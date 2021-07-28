//
//  DataBoxView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 29/06/2021.
//

import SwiftUI

struct DataBoxView: View  {
    
    var title: String
    var content: String
    
    var body: some View {
        
        VStack(alignment: .leading) {
            Text(title)
            ZStack {
                RoundedRectangle(cornerRadius: 6.0, style: .continuous)
                    .fill(AppColor.lightGrey)
                    .frame(height: 60)
                    .overlay(RoundedRectangle(cornerRadius: 6.0, style: .continuous)
                                .stroke(Color.black, lineWidth: 1))
                
                Text(content)
                    .font(.title2)
                
            }
        }
        
    }
}

struct DataBoxView_Previews: PreviewProvider {
    static var previews: some View {
        DataBoxView(title: "Title", content: "12.34")
    }
}
