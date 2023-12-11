//
//  SectionHeader.swift
//  app
//
//  Created by Nils Brenkman on 09/12/2023.
//

import SwiftUI

struct SectionHeader: View {
    
    var header: String
    
    init(_ header: String) {
        self.header = header
    }
    
    var body: some View {
        Text("\(header):")
            .foregroundStyle(.secondary)
            .font(Font.ui.sectionHeader)
    }
}
