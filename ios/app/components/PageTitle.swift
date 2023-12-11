//
//  PageTitle.swift
//  app
//
//  Created by Nils Brenkman on 11/12/2023.
//

import SwiftUI

extension View {
    func pageTitle(_ title: String, dismiss: @escaping () -> Void) -> some View {
        self.navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .navigationBarBackButtonHidden()
            .navigationBarItems(
                leading: Button(action: dismiss) {
                    Image(systemName: "arrow.left")
                }
            )
            .foregroundStyle(.primary)
    }
}
