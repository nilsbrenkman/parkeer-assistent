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
                } else {
                    NavigationView {
                        if login.isLoggedIn {
                            UserView()
                                .background (
                                    List {
                                        NavigationLink(destination: PaymentView(), isActive: pageBinding(.payment)) {
                                            EmptyView()
                                        }
                                        NavigationLink(destination: HistoryListView(), isActive: pageBinding(.history)) {
                                            EmptyView()
                                        }
                                        NavigationLink(destination: AccountView(), isActive: pageBinding(.account)) {
                                            EmptyView()
                                        }
                                        NavigationLink(destination: SettingsView(), isActive: pageBinding(.settings)) {
                                            EmptyView()
                                        }
                                        NavigationLink(destination: AddVisitorView(), isActive: pageBinding(.visitor)) {
                                            EmptyView()
                                        }
                                        NavigationLink(destination: AddParkingView(), isActive: pageBinding(.parking)) {
                                            EmptyView()
                                        }
                                    }
                                )
                        } else {
                            LoginView()
                                .navigationBarHidden(true)
                        }
                    }
                    .navigationViewStyle(.stack)
                    .padding(.top, Constants.padding.normal)
                    .background(Color.system.groupedBackground)
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
    
    private func pageBinding(_ page: Page) -> Binding<Bool> {
        return Binding<Bool>(
            get: { return user.page == page },
            set: { _ in }
        );
    }
    
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
