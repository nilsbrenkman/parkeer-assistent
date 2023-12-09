//
//  ContentView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 12/06/2021.
//

import SwiftUI
import WatchConnectivity

@MainActor
struct ContentView: View {
    
    @Environment(\.scenePhase) private var scenePhase
    
    @StateObject var login = try! Login()
    @StateObject var user = try! User()
    @StateObject var payment = try! Payment()
    @StateObject var messenger = AppMessenger()
    
    @State var initialised = false
    @State var showInfo = false
    @State var showHistory = false
    @State var showAccounts = false
    @State var showSettings = false
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HeaderView(showInfo: $showInfo, showHistory: $showHistory, showAccounts: $showAccounts, showSettings: $showSettings)
            
            ZStack {
                if login.isLoading || login.isBackground {
                    LoadingView()
                } else if login.isLoggedIn {
                    if payment.show {
                        NavigationView {
                            PaymentView()
                        }
                        .navigationViewStyle(.stack)
                    } else {
                        NavigationView {
                            UserView()
                                .background (
                                    List {
                                        NavigationLink(destination: HistoryListView(), isActive: $showHistory) {
                                            EmptyView()
                                        }
                                        NavigationLink(destination: AccountView(), isActive: $showAccounts) {
                                            EmptyView()
                                        }
                                        NavigationLink(destination: SettingsView(), isActive: $showSettings) {
                                            EmptyView()
                                        }
                                    }
                                )
                        }
                        .navigationViewStyle(.stack)
                        .padding(.top, Constants.padding.normal)
                        .background(Color.system.groupedBackground)
                    }
                } else {
                    NavigationView {
                        LoginView()
                            .navigationBarHidden(true)
                    }
                    .navigationViewStyle(.stack)
                }
            }
            .ignoresSafeArea()
        }
        .environmentObject(login)
        .environmentObject(user)
        .environmentObject(payment)
        .message(message: $messenger.message)
        .modal(visible: $showInfo) {
            InfoView()
        }
        .onAppear {
            if !initialised {
                Task {
                    await login.loggedIn()
                    initialised = true
                    login.user = user
                    login.payment = payment
                }
            }
        }
        .onChange(of: scenePhase) { phase in
            if phase == .background {
                login.isBackground = true
            } else if login.isBackground {
                login.isBackground = false
                Task {
                    await login.loggedIn()
                }
            }
        }
        .onOpenURL { url in
            let split = url.absoluteString.split(separator: "?")
            if split.count == 2 {
                let query = String(split[1])
                payment.completeData = query
            }
        }
    }
    
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
