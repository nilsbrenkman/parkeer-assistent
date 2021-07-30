//
//  VisitorView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 22/06/2021.
//

import SwiftUI

struct VisitorView: View {
    
    var visitor: Visitor
    
    var body: some View {
        HStack {
            LicenseView(license: visitor.formattedLicense)

            Text("\(visitor.name ?? "")")
                .font(/*@START_MENU_TOKEN@*/.title3/*@END_MENU_TOKEN@*/)
                .bold()
                .padding(.leading)

        }
    }
}

struct VisitorView_Previews: PreviewProvider {
    static var previews: some View {
        let visitor = Visitor(visitorId: 1, permitId: 1, license: "12-AB-34", formattedLicense: "12-AB-34", name: "Jan")
        VisitorView(visitor: visitor)
    }
}
