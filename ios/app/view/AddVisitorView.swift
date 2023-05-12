//
//  AddVisitorView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 22/06/2021.
//

import SwiftUI

@MainActor
struct AddVisitorView: View {
    
    @EnvironmentObject var user: User
    
    @State private var license: String = ""
    @State private var name: String = ""
    
    @State private var wait: Bool = false
    
    var body: some View {
        
        Form {
            Section {
                VStack(alignment: .leading, spacing: Constants.spacing.small) {
                    
                    Text("\(Lang.Visitor.license.localized()):")
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
                    
                    Text("\(Lang.Visitor.name.localized()):")
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
                    Text(Lang.Common.add.localized())
                        .font(.title3)
                        .bold()
                        .wait($wait)
                }
                .style(.success, disabled: self.license.count == 0 || self.name.count == 0)
                
                Button(action: { user.addVisitor = false }) {
                    Text(Lang.Common.cancel.localized())
                        .font(.title3)
                        .bold()
                        .centered()
                }
                .style(.cancel)
            }
            
        }
        .listStyle(InsetGroupedListStyle())
        .navigationBarHidden(true)
    }
    
    private func handleAddVisitor(success: Bool) {
        if success {
            user.visitors = nil
            user.addVisitor = false
            
            user.getVisitors()
        } else {
            wait = false
        }
    }
    
}

struct AddVisitorView_Previews: PreviewProvider {
    static var previews: some View {
        AddVisitorView()
    }
}
