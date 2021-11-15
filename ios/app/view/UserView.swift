//
//  UserView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 13/06/2021.
//

import SwiftUI
import StoreKit

struct UserView: View {
    
    @EnvironmentObject var user: User
    
    var body: some View {
        
        Form {

            ParkingView()
            
            VisitorListView()

        }
        .listStyle(InsetGroupedListStyle())
        .onAppear {
            if !user.isLoaded {
                user.getUser()
                user.isLoaded = true
            } else {
                if Stats.user.requestReview() {
                    if let windowScene = UIApplication.shared.windows.first?.windowScene {
                        Stats.user.requested = Date.now()
                        SKStoreReviewController.requestReview(in: windowScene)
                    }
                }
            }
        }
        .navigationBarHidden(true)

    }
    
}

struct UserView_Previews: PreviewProvider {
    static var previews: some View {
        UserView()
    }
}
