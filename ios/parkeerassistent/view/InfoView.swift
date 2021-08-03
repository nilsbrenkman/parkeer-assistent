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
                Text("Over deze app")
                    .font(.title2)
                
                Text("Parkeren Amsterdam is een persoonlijke app die niet verbonden is aan de gemeente Amsterdam.")
                
                Text("Het doel van de app is het bieden van een alternatieve, meer gebruiksvriendelijke interface voor de website van de gemeente.")
                
                Link("Website", destination: URL(string: "https://aanmeldenparkeren.amsterdam.nl/")!)
                    .foregroundColor(Color.ui.header)
                Text("De app verzamelt geen gebruikersdata zoals meldcode, pincode of enige andere persoonlijke gegevens. Als je ervoor kiest je inlog gegevens te onthouden worden deze lokaal en veilig bewaard.")
                
                Text("De maker van de app is niet aansprakelijk voor enige vorm van gebruik.")
                
                Text("De broncode van deze app is open source en beschikbaar via GitHub.")
                
                Link("Broncode", destination: URL(string: "https://github.com/nilsbrenkman/parkeer-assistent")!)
                    .foregroundColor(Color.ui.header)
                
                if let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String,
                   let build = Bundle.main.infoDictionary?["CFBundleVersion"] as? String {
                    Text("Versie: \(version) (\(build))")
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
