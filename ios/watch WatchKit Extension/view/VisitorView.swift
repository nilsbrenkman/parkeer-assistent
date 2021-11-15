//
//  VisitorView.swift
//  watch WatchKit Extension
//
//  Created by Nils Brenkman on 16/10/2021.
//

import SwiftUI

struct VisitorView: View {
    
    @EnvironmentObject var login: Login
    
    var visitor: Visitor
    
    var body: some View {
        ZStack {
            Color.clear
            
            VStack(alignment: .center, spacing: 0) {
                LicenseView(license: visitor.license)
                VisitorNameView(name: visitor.name)
            }
        }

        .padding(.vertical, Constants.padding.xSmall)
//        .gesture(TapGesture(count: 2).onEnded {
//            login.logout() {}
//        })
    }
}

struct VisitorView_Previews: PreviewProvider {
    static var previews: some View {
        let visitor = Visitor(visitorId: 1, permitId: 1, license: "12AB34", formattedLicense: "12-AB-34", name: "Jan")
        VisitorView(visitor: visitor)
    }
}
