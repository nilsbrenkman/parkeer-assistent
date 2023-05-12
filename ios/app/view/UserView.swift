//
//  UserView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 13/06/2021.
//

import SwiftUI
import StoreKit

@MainActor
struct UserView: View {
    
    @EnvironmentObject var user: User
    
    @State private var refreshTask: Task<Void, Never>?
    
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
            } else {
                if Stats.user.requestReview() {
                    if let windowScene = UIApplication.shared.windows.first?.windowScene {
                        Stats.user.requested = Date.now()
                        SKStoreReviewController.requestReview(in: windowScene)
                    }
                }
            }
            guard refreshTask == nil else {
                return
            }
            refreshTask = Task.detached(priority: .background) {
                let isCancelled: () -> Bool = {
                    do {
                        try Task.checkCancellation()
                        return false
                    } catch {
                        return true
                    }
                }
                
                var delay = 10.0
                var update = false

                let checkUpdate: (String) -> Void = { time in
                    if let date = try? Util.parseDate(time) {
                        if date < Date.now() {
                            update = true
                        } else {
                            let timeInterval = Date.now().distance(to: date)
                            if timeInterval < delay {
                                delay = timeInterval
                            }
                        }
                    }
                }
                
                while !isCancelled() {
                    delay = 10.0
                    update = false
                    
                    if let parking = await user.parking {
                        delay = 60.0
                        for active in parking.active {
                            checkUpdate(active.endTime)
                        }
                        for scheduled in parking.scheduled {
                            checkUpdate(scheduled.startTime)
                        }
                    }
                    if update {
                        await user.getParking()
                    }
                    try? await Task.sleep(nanoseconds: UInt64(delay.rounded()) * 1_000_000_000)
                }

            }
        }
        .onDisappear {
            refreshTask?.cancel()
            refreshTask = nil
        }
        .navigationBarHidden(true)

    }
    
}

struct UserView_Previews: PreviewProvider {
    static var previews: some View {
        UserView()
    }
}
