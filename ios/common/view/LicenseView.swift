//
//  LicenseView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 22/06/2021.
//

import SwiftUI

@MainActor
struct LicenseView: View {
    
    var license: String
    
    var body: some View {
        ZStack{
            RoundedRectangle(cornerRadius: Constants.radius.small, style: .continuous)
                .fill(Color.ui.licenseBg)
                .frame(width: Constants.license.width, height: Constants.license.height)
            
            Text("\(License.formatLicense(license))")
                .font(Font.ui.license)
                .foregroundColor(Color.ui.license)
                .tracking(1)
                .frame(width: Constants.license.width - (Constants.license.padding * 2), height: Constants.license.height)
                .padding(.horizontal, Constants.license.padding)
                .minimumScaleFactor(0.5)
                .lineLimit(1)
                .overlay(
                    RoundedRectangle(cornerRadius: Constants.radius.small)
                        .stroke(Color.ui.licenseBorder, lineWidth: 1)
                )
        }
    }
}

struct LicenseView_Previews: PreviewProvider {
    static var previews: some View {
        LicenseView(license: "12tbd1")
        LicenseView(license: "wwwwww")
    }
}
