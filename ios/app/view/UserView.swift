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
                    startRefreshTask()
                }
            } else {
                if Stats.user.requestReview() {
                    if let windowScene = UIApplication.shared.windows.first?.windowScene {
                        Stats.user.requested = Date.now()
                        SKStoreReviewController.requestReview(in: windowScene)
                    }
                }
                startRefreshTask()
            }
        }
        .onDisappear {
            Log.info("Cancelling refresh task")
            refreshTask?.cancel()
            refreshTask = nil
        }
        .navigationBarHidden(true)

    }
    
    private func startRefreshTask() {
        guard refreshTask == nil else {
            return
        }
        refreshTask = Task.detached(priority: .background) {
            Log.info("Starting refresh task")

            let checkUpdate: (String) -> Double = { time in
                if let date = try? Util.parseDate(time) {
                    if date < Date.now() {
                        return 0
                    }
                    let interval = Date.now().distance(to: date)
                    Log.debug("Interval until \(time, privacy: .public) is \(interval, privacy: .public)")
                    return interval
                }
                Log.warning("Error parsing time \(time, privacy: .public)")
                return 60
            }
            
            while !Task.isCancelled {
                var delay = 60.0
                
                Log.debug("Running refresh task")
                
                guard let parking = await user.parking else {
                    Log.warning("No parking data, wait 10 seconds")
                    try? await Task.sleep(nanoseconds: UInt64(10 * 1_000_000_000))
                    continue
                }
                
                for active in parking.active {
                    delay = min(delay, checkUpdate(active.endTime))
                }
                for scheduled in parking.scheduled {
                    delay = min(delay, checkUpdate(scheduled.startTime))
                }
                
                if delay > 0 {
                    try? await Task.sleep(nanoseconds: UInt64(delay * 1_000_000_000))
                }
                await user.getParking()
            }
            Log.debug("Exiting refresh task")
        }
    }
    
}

struct UserView_Previews: PreviewProvider {
    static var previews: some View {
        UserView()
    }
}
