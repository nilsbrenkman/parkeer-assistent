//
//  ContentView.swift
//  watch WatchKit Extension
//
//  Created by Nils Brenkman on 13/10/2021.
//

import SwiftUI
import WatchConnectivity

struct ContentView: View {
    
    @Environment(\.scenePhase) private var scenePhase
    
    @StateObject var login = try! Login()
    @StateObject var user = try! User()
    @StateObject var messenger = WatchMessenger()
    
    @State var initialised = false
    
    var body: some View {
        ZStack {
            if login.isLoading || login.isBackground {
                ProgressView()
                    .centered()
            } else if login.isLoggedIn {
                UserView()
            } else {
                LoginView()
            }
        }
        .environmentObject(login)
        .environmentObject(user)
        .onAppear {
            if WCSession.isSupported() {
                let session = WCSession.default
                session.delegate = WatchCommunicator.instance
                session.activate()
                WatchCommunicator.instance.addListener(.login) { message in
                    print("Checking logged in state")
                    login.loggedIn()
                }
            }
            if !initialised {
                login.loggedIn()
                initialised = true
            }
        }
        .alert(isPresented: $messenger.show) {
            Alert(
                title: Text(messenger.message!.message),
                dismissButton: .cancel(Text(Lang.Common.ok.localized())) {
                    if let ok = messenger.message!.ok {
                        ok()
                    }
                }
            )
        }
//        .onChange(of: scenePhase) { phase in
//            if phase == .background {
//                login.isBackground = true
//            } else if login.isBackground {
//                login.isBackground = false
//                login.loggedIn()
//            }
//        }
    }

}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
