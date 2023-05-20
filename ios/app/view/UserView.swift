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
                Task {
                    await user.getUser()
                    user.isLoaded = true
                }
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
                Log.info("Starting refresh task")
                let isCancelled: () -> Bool = {
                    do {
                        try Task.checkCancellation()
                        return false
                    } catch {
                        return true
                    }
                }
                
                var delay = 60.0
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
                    try? await Task.sleep(nanoseconds: UInt64(delay.rounded()) * 1_000_000_000)
                    
                    delay = 60.0
                    update = false
                    
                    Log.debug("Running refresh task")
                    
                    if let parking = await user.parking {
                        
                        for active in parking.active {
                            checkUpdate(active.endTime)
                        }
                        for scheduled in parking.scheduled {
                            checkUpdate(scheduled.startTime)
                        }
                    }
                    if update {
                        Log.debug("Retrieving new data")
                        await user.getParking()
                    }
                    
                }
                Log.debug("Exiting refresh task")

            }
        }
        .onDisappear {
            Log.info("Cancelling refresh task")
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
