//
//  PickerModal.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 20/08/2021.
//

import SwiftUI

struct InsetPicker: View {
    
    @Environment(\.presentationMode) var presentationMode: Binding<PresentationMode>
    
    var labels: [String]
    @Binding var selected: Int
    
    var body: some View {
        Form {
            Section {
                ForEach(0 ..< labels.count, id: \.self) { i in
                    Button(action: {
                        self.selected = i
                        self.presentationMode.wrappedValue.dismiss()
                    }){
                        HStack {
                            Text(labels[i])
                                .foregroundColor(Color.ui.bw0)
                            Spacer()
                            if self.selected == i {
                                Image(systemName: "checkmark")
                                    .foregroundColor(Color.ui.header)
                            }
                        }
                    }
                    
                }
            }
        }
        .navigationBarHidden(true)
    }
}

struct PickerModal_Previews: PreviewProvider {
    @State static var selected = 0
    static var previews: some View {
        InsetPicker(labels: [""], selected: $selected)
    }
}
