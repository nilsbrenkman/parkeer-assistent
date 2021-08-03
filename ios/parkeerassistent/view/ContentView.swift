//
//  ContentView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 12/06/2021.
//

import SwiftUI

struct ContentView: View {
    
    @Environment(\.scenePhase) private var scenePhase
    
    @StateObject var login = try! Login()
    @StateObject var user = try! User()
    @StateObject var messenger = Messenger()
    
    @State var initialised = false
    @State var showInfo = false

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HeaderView(showInfo: $showInfo)
            
            ZStack {
                if login.isLoading || login.isBackground {
                    LoadingView()
                } else if login.isLoggedIn {
                    NavigationView {
                        UserView()
                    }
                    .navigationViewStyle(StackNavigationViewStyle())
                } else {
                    LoginView()
                }
            }
        }
        .environmentObject(login)
        .environmentObject(user)
        .message(message: $messenger.message)
        .modal(visible: $showInfo) {
            InfoView()
        }
        .onAppear {
            if !initialised {
                login.loggedIn()
                initialised = true
            }
        }
        .onChange(of: scenePhase) { phase in
            if phase == .background {
                login.isBackground = true
            } else if login.isBackground {
                login.isBackground = false
                login.loggedIn()
            }
        }
    }
    
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
