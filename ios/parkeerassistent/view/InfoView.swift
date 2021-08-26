//
//  InfoView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 02/08/2021.
//

import SwiftUI

struct InfoView: View {
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: Constants.spacing.normal) {
                Text(Lang.Info.header.localized())
                    .font(.title2)
                
                Text(Lang.Info.text1.localized())
                
                Text(Lang.Info.text2.localized())
                
                Link(Lang.Info.website.localized(), destination: URL(string: "https://aanmeldenparkeren.amsterdam.nl/")!)
                    .foregroundColor(Color.ui.header)
                
                Text(Lang.Info.text3.localized())
                
                Text(Lang.Info.text4.localized())
                
                Text(Lang.Info.text5.localized())
                
                Link(Lang.Info.sourceCode.localized(), destination: URL(string: "https://github.com/nilsbrenkman/parkeer-assistent")!)
                    .foregroundColor(Color.ui.header)
                
                if let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String,
                   let build = Bundle.main.infoDictionary?["CFBundleVersion"] as? String {
                    Text("\(Lang.Info.version.localized()): \(version) (\(build))")
                        .font(.footnote)
                }
            }
            .padding()
        }
        .frame(height: UIScreen.main.bounds.height * 0.5)
        .overlay(RoundedRectangle(cornerRadius: Constants.radius.normal, style: .continuous)
                    .stroke(Color.ui.header, lineWidth: 1))
        .background(Color.ui.bw100)
        .cornerRadius(Constants.radius.normal)
        .padding(.horizontal)
    }
}

struct InfoView_Previews: PreviewProvider {
    static var previews: some View {
        InfoView()
    }
}
