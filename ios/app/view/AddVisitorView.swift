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
                HStack {
                    Text(Lang.Visitor.license.localized())
                        .frame(width: 120, alignment: .leading)
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
                    Spacer()
                }
                .padding(.vertical, Constants.padding.mini)
                
                HStack {
                    Text(Lang.Visitor.name.localized())
                        .frame(width: 120, alignment: .leading)
                    TextField(Lang.Visitor.name.localized(), text: $name)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.leading)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                .padding(.vertical, Constants.padding.mini)

            }
            
            Section {
                Button(action: {
                    if !wait {
                        Task {
                            wait = true
                            await user.addVisitor(license: license, name: name)
                            wait = false
                        }
                    }
                }){
                    Text(Lang.Common.add.localized())
                        .font(.title3)
                        .bold()
                        .wait($wait)
                }
                .style(.success, disabled: license.count == 0 || name.count == 0)
            }
            
        }
        .listStyle(.insetGrouped)
        .pageTitle(Lang.Visitor.add.localized(), dismiss: { user.page = nil })
    }
    
    private func handleAddVisitor(success: Bool) {
        if success {
            user.visitors = nil
            user.page = nil
            
            Task {
                await user.getVisitors()
            }
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
