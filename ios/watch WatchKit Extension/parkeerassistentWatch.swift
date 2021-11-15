//
//  parkeerassistentApp.swift
//  watch WatchKit Extension
//
//  Created by Nils Brenkman on 13/10/2021.
//

import SwiftUI

@main
struct parkeerassistentWatch: App {
 
    init() {
        if useMockClient() {
            ClientManager.instance.register(LoginClient.self,   client: LoginClientMock.client)
            ClientManager.instance.register(UserClient.self,    client: UserClientMock.client)
            ClientManager.instance.register(ParkingClient.self, client: ParkingClientMock.client)
            ClientManager.instance.register(VisitorClient.self, client: VisitorClientMock.client)
            ClientManager.instance.register(PaymentClient.self, client: PaymentClientMock.client)
        } else {
            ClientManager.instance.register(LoginClient.self,   client: LoginClientApi.client)
            ClientManager.instance.register(UserClient.self,    client: UserClientApi.client)
            ClientManager.instance.register(ParkingClient.self, client: ParkingClientApi.client)
            ClientManager.instance.register(VisitorClient.self, client: VisitorClientApi.client)
            ClientManager.instance.register(PaymentClient.self, client: PaymentClientApi.client)
        }
        ClientManager.instance.register(WatchCommunicator.self, client: WatchCommunicator.instance)
    }
    
    func useMockClient() -> Bool {
        return true
    }
   
    @SceneBuilder var body: some Scene {
        WindowGroup {
            NavigationView {
                ContentView()
            }
        }

        WKNotificationScene(controller: NotificationController.self, category: "myCategory")
    }
}
