//
//  parkeerassistentApp.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 12/06/2021.
//

import SwiftUI

@main
struct parkeerassistentApp: App {

    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    static let useMockClient = false
    
    init() {
        if parkeerassistentApp.useMockClient {
            ClientManager.instance.register(LoginClient.self,   client: LoginClientMock.client)
            ClientManager.instance.register(UserClient.self,    client: UserClientMock.client)
            ClientManager.instance.register(ParkingClient.self, client: ParkingClientMock.client)
            ClientManager.instance.register(VisitorClient.self, client: VisitorClientMock.client)
        } else {
            ClientManager.instance.register(LoginClient.self,   client: LoginClientApi.client)
            ClientManager.instance.register(UserClient.self,    client: UserClientApi.client)
            ClientManager.instance.register(ParkingClient.self, client: ParkingClientApi.client)
            ClientManager.instance.register(VisitorClient.self, client: VisitorClientApi.client)
        }
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
    
}
