//
//  VisitorNameView.swift
//  watch WatchKit Extension
//
//  Created by Nils Brenkman on 20/10/2021.
//

import SwiftUI

struct VisitorNameView: View {
    
    var name: String?
    
    var body: some View {
        Text("\(name ?? "")")
            .font(.title3)
            .foregroundColor(Color.white)
            .bold()
            .minimumScaleFactor(0.5)
            .lineLimit(1)
    }
}

struct VisitorNameView_Previews: PreviewProvider {
    static var previews: some View {
        VisitorNameView()
    }
}
