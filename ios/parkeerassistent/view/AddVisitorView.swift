//
//  AddVisitorView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 22/06/2021.
//

import SwiftUI

struct AddVisitorView: View {
    
    @EnvironmentObject var user: User
    
    @State private var license: String = ""
    @State private var name: String = ""
    
    @State private var wait: Bool = false
    @State private var message: String?
    
    var body: some View {
        
        Form {
            Section {
                VStack(alignment: .leading, spacing: Constants.spacing.small) {
                    
                    Text("Kenteken:")
                        .font(.title3)
                    
                    ZStack() {
                        RoundedRectangle(cornerRadius: Constants.radius.small, style: .continuous)
                            .fill(Color.ui.licenseBg)
                            .frame(width: Constants.license.width, height: Constants.license.height)
                        
                        TextField("", text: $license)
                            .accessibility(identifier: "license")
                            .font(Font.ui.license)
                            .foregroundColor(Color.ui.license)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, Constants.license.padding)
                            .frame(width: Constants.license.width, height: Constants.license.height)
                            .minimumScaleFactor(0.5)
                            .lineLimit(1)
                            .disableAutocorrection(true)
                            .overlay(
                                RoundedRectangle(cornerRadius: Constants.radius.small)
                                    .stroke(Color.ui.licenseBorder, lineWidth: 1)
                            )
                            .onChange(of: license, perform: { value in
                                license = License.formatLicense(license)
                            })
                    }
                    
                    Text("Naam:")
                        .font(.title3)
                    TextField("", text: $name)
                        .accessibility(identifier: "name")
                        .font(Font.ui.name)
                        .disableAutocorrection(true)
                        .frame(height: 36)
                        .padding(.horizontal)
                        .overlay(
                            RoundedRectangle(cornerRadius: Constants.radius.small)
                                .stroke(Color.ui.bw0, lineWidth: 1)
                        )
                }
                .padding(.vertical)
            }
            
            Section {
                Button(action: {
                    if !wait {
                        wait = true
                        user.addVisitor(license: license, name: name) {
                            wait = false
                        }
                    }
                }){
                    Text("Toevoegen")
                        .font(.title3)
                        .bold()
                        .wait($wait)
                }
                .buttonStyle(ButtonStyles.Enabled(main: Color.ui.success,
                                                  disabled: Color.ui.successDisabled,
                                                  enabled: self.license.count > 0 && self.name.count > 0))
                
                Button(action: { user.addVisitor = false }) {
                    Text("Annuleren")
                        .font(.title3)
                        .bold()
                        .centered()
                }
                .buttonStyle(ButtonStyles.cancel)
            }
            
        }
        .listStyle(InsetGroupedListStyle())
        .navigationBarHidden(true)
    }
    
    private func handleAddVisitor(success: Bool) {
        if success {
            DispatchQueue.main.async {
                user.visitors = nil
                user.addVisitor = false
            }
            user.getVisitors()
        } else {
            self.message = "Bezoeker toevoegen mislukt"
            wait = false
        }
    }
    
}

struct AddVisitorView_Previews: PreviewProvider {
    static var previews: some View {
        AddVisitorView()
    }
}
