//
//  LoadingView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 19/06/2021.
//

import SwiftUI

@MainActor
struct LoadingView: View {
    var body: some View {
        VStack {
            Spacer()
            ProgressView()
                .centered()
            Spacer()
        }
    }
}

struct LoadingView_Previews: PreviewProvider {
    static var previews: some View {
        LoadingView()
    }
}
