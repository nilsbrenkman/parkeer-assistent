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

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HeaderView()
            
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
            
            Spacer()
            
        }
        .environmentObject(login)
        .environmentObject(user)
        .environmentObject(messenger)
        .message(message: $messenger.message)
        .onAppear {
            if !initialised {
                login.messenger = messenger.addMessage
                user.messenger = messenger.addMessage
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
