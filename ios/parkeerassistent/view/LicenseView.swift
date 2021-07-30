//
//  LicenseView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 22/06/2021.
//

import SwiftUI

struct LicenseView: View {
    
    var license: String
    
    var body: some View {
        ZStack{
            RoundedRectangle(cornerRadius: 6.0, style: .continuous)
                .fill(Color.ui.licenseBg)
                .frame(width: 140, height: 36)
            
            Text("\(License.formatLicense(license))")
                .font(.title3)
                .foregroundColor(Color.ui.license)
                .bold()
                .tracking(2)
                .frame(width: 140, height: 36)
                .overlay(
                    RoundedRectangle(cornerRadius: 6)
                        .stroke(Color.ui.licenseBorder, lineWidth: 1)
                )
        }
    }
}

struct LicenseView_Previews: PreviewProvider {
    static var previews: some View {
        LicenseView(license: "12-TBD-1")
    }
}
