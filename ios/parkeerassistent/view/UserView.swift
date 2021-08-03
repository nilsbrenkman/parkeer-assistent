//
//  UserView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 13/06/2021.
//

import SwiftUI

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
