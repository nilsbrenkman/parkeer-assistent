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
    
    static let versionKey = "version"
    
    init() {
        if useMockClient() {
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
    
    private func useMockClient() -> Bool {
        guard let appVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String else {
            return true
        }
        guard let userVersion = UserDefaults.standard.string(forKey: parkeerassistentApp.versionKey) else {
            return mockVersion(appVersion)
        }
        if appVersion == userVersion {
            return false
        }
        return mockVersion(appVersion)
    }
    
    private func mockVersion(_ version: String) -> Bool {
        guard let url = URL(string: ApiClient.BASE_URL + "version/" + version) else {
            return true
        }
        
        var mock = true
        let semaphore = DispatchSemaphore(value: 0)
        
        URLSession.shared.dataTask(with: url) { data, response, error in
            if let httpResponse = response as? HTTPURLResponse {
                if httpResponse.statusCode == 200 {
                    mock = false
                }
            }
            semaphore.signal()
        }
        .resume()
        
        _ = semaphore.wait(timeout: DispatchTime.now().advanced(by: DispatchTimeInterval.seconds(10)))
        
        if mock {
            return true
        }
        UserDefaults.standard.set(version, forKey: parkeerassistentApp.versionKey)
        return false
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
    
}
