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
                return
                    foregroundColor(Color.ui.disabled)
                    .listRowBackground(Color.ui.successDisabled)
            }
            return
                foregroundColor(Color.ui.enabled)
                .listRowBackground(Color.ui.success)

        case .danger:
            if disabled {
                return
                    foregroundColor(Color.ui.disabled)
                    .listRowBackground(Color.ui.dangerDisabled)
            }
            return
                foregroundColor(Color.ui.enabled)
                .listRowBackground(Color.ui.danger)

        case .cancel:
            return
                foregroundColor(Color.ui.danger)
                .listRowBackground(Color.ui.light)

        }
    }
    
    enum Style {
        case success
        case danger
        case cancel
    }
    
}
