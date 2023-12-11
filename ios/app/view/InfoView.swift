//
//  InfoView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 02/08/2021.
//

import SwiftUI

@MainActor
struct InfoView: View {
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: Constants.spacing.normal) {
                Text(Lang.Info.header.localized())
                    .font(.title2)
                
                Group {
                    Text(Lang.Info.text1.localized())
                    Text(Lang.Info.text2.localized())
                    createLink(Lang.Info.website.localized(), url: "https://aanmeldenparkeren.amsterdam.nl/")
                }
                Group {
                    Text(Lang.Info.text3.localized())
                    Text(Lang.Info.text4.localized())
                    Text(Lang.Info.text5.localized())
                    createLink(Lang.Info.sourceCode.localized(), url: "https://github.com/nilsbrenkman/parkeer-assistent")
                }
                Group {
                    Text(Lang.Info.text6.localized())
                    createLink(Lang.Info.feedback.localized(), url: "https://parkeerassistent.nl/feedback")
                }
                if let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String,
                   let build = Bundle.main.infoDictionary?["CFBundleVersion"] as? String {
                    Text("\(Lang.Info.version.localized()): \(version) (\(build))")
                        .font(.footnote)
                }
            }
            .padding()
        }
        .frame(height: UIScreen.main.bounds.height * 0.5)
        .overlay(
            RoundedRectangle(cornerRadius: Constants.radius.normal, style: .continuous)
                .stroke(Color.ui.header, lineWidth: 1)
        )
        .background(.background)
        .cornerRadius(Constants.radius.normal)
        .padding(.horizontal)
    }
    
    private func createLink(_ title: String, url: String) -> some View {
        Link(title, destination: URL(string: url)!)
            .foregroundColor(Color.ui.header)
    }
    
}

struct InfoView_Previews: PreviewProvider {
    static var previews: some View {
        InfoView()
    }
}
