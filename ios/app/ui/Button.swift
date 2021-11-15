//
//  ButtonStyles.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 03/08/2021.
//

import SwiftUI

extension Button {
        
    func style(_ style: Style, disabled: Bool = false) -> some View {
        switch style {
        case .success:
            if disabled {
                return self
                    .foregroundColor(Color.ui.disabled)
                    .listRowBackground(Color.ui.successDisabled)
            }
            return self
                .foregroundColor(Color.ui.enabled)
                .listRowBackground(Color.ui.success)

        case .danger:
            if disabled {
                return self
                    .foregroundColor(Color.ui.disabled)
                    .listRowBackground(Color.ui.dangerDisabled)
            }
            return self
                .foregroundColor(Color.ui.enabled)
                .listRowBackground(Color.ui.danger)

        case .cancel:
            return self
                .foregroundColor(Color.ui.danger)
                .listRowBackground(Color.ui.light)

        }
    }
    
    enum Style {
        case success
        case danger
        case cancel
    }
    
}
