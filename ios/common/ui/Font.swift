//
//  Text.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 17/08/2021.
//

import SwiftUI

extension Font {
    
    static let ui = Font.UI()

    struct UI {
        
        let license = Font.system(size: 22.0, weight: .semibold, design: .default)
        let name = Font.system(size: 24.0, weight: .semibold, design: .default)

        let dataBoxTitle = Font.system(size: 20.0, weight: .regular, design: .default)
        let dataBoxContent = Font.system(size: 24.0, weight: .regular, design: .default)

        let propertyLabel = Font.system(size: 16.0, weight: .regular, design: .default)
        let propertyValue = Font.system(size: 20.0, weight: .semibold, design: .default)
        
        let calenderDow = Font.system(size: 10.0, weight: .bold, design: .default)
        let calenderDay = Font.system(size: 18.0, weight: .bold, design: .default)

        let sectionHeader = Font.system(size: 16.0, weight: .semibold, design: .default)

    }
}
