//
//  InfoView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 02/08/2021.
//

import SwiftUI

struct InfoView: View {
    
    @Binding var showInfo: Bool
    
    var body: some View {
        ZStack {
            Color.gray.opacity(0.5)
                .onTapGesture {
                    showInfo = false
                }
            
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    Text("Over deze app")
                        .font(.title2)
                    
                    Text("Parkeer Assistent is een persoonlijke app die niet verbonden is aan de gemeente Amsterdam.")
                    
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
            .overlay(RoundedRectangle(cornerRadius: 10, style: .continuous)
                        .stroke(Color.ui.header, lineWidth: 1))
            .background(Color.ui.bw100)
            .cornerRadius(10)
            .padding(.horizontal)
        }
        .background(Color.clear)
        .ignoresSafeArea()
    }
}

struct InfoView_Previews: PreviewProvider {
    @State static var showInfo = true
    static var previews: some View {
        InfoView(showInfo: $showInfo)
    }
}
