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
                VStack(alignment: .leading, spacing: 10) {
                    
                    Text("Kenteken:")
                        .font(.title3)
                    
                    ZStack() {
                        RoundedRectangle(cornerRadius: 6.0, style: .continuous)
                            .fill(Color.yellow)
                            .frame(width: 140, height: 36)
                        
                        TextField("", text: $license)
                            .padding(.horizontal)
                            .font(.title3)
                            .multilineTextAlignment(.center)
                            .frame(width: 140, height: 36)
                            .disableAutocorrection(true)
                            .overlay(
                                RoundedRectangle(cornerRadius: 6)
                                    .stroke(Color.black, lineWidth: 1)
                            )
                            .onChange(of: license, perform: { value in
                                license = License.formatLicense(license)
                            })
                    }
                    
                    Text("Naam:")
                        .font(.title3)
                    TextField("", text: $name)
                        .font(.title3)
                        .disableAutocorrection(true)
                        .frame(height: 36)
                        .padding(.horizontal)
                        .overlay(
                            RoundedRectangle(cornerRadius: 6)
                                .stroke(Color.black, lineWidth: 1)
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
                    if wait {
                        ProgressView()
                            .centered()
                    } else {
                        Text("Toevoegen")
                            .font(.title3)
                            .bold()
                            .centered()
                    }
                }
                .color(AppColor.success, enabled: self.license.count > 0 && self.name.count > 0)
                
                Button(action: { user.addVisitor = false }) {
                    Text("Annuleren")
                        .font(.title3)
                        .bold()
                        .centered()
                }
                .foregroundColor(AppColor.danger.main)
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
