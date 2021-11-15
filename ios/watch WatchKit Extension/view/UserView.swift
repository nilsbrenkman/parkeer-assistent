//
//  UserView.swift
//  watch WatchKit Extension
//
//  Created by Nils Brenkman on 16/10/2021.
//

import SwiftUI

struct UserView: View {
    
    @EnvironmentObject var user: User

    @State var selectedTab: Int = 0
    
    var body: some View {
        TabView(selection: $selectedTab) {
            ParkingListView()
            VisitorListView()
        }
        .tabViewStyle(PageTabViewStyle())
        .onAppear {
            if !user.isLoaded {
                user.getUser()
                user.isLoaded = true
            }
        }
    }
}

struct UserView_Previews: PreviewProvider {
    static var previews: some View {
        UserView()
    }
}
