//
//  Timer.swift
//  watch WatchKit Extension
//
//  Created by Nils Brenkman on 16/10/2021.
//

import SwiftUI


struct TimerView: View {
    
    let formatter: DateComponentsFormatter = {
        let formatter = DateComponentsFormatter()
        formatter.allowedUnits = [.hour, .minute, .second]
        formatter.zeroFormattingBehavior = .pad
        return formatter
    }()

    let update = Timer.publish(every: 1, on: .main, in: .common).autoconnect()
    
    var endTime: Date
    
    @State private var timer: String?
    
    var body: some View {
        Text("\(timer ?? "00:00:00")")
            .onAppear {
                updateTimer()
            }
            .onReceive(update, perform: { _ in
                updateTimer()
            })
    }
    
    private func updateTimer() {
        if Date.now() > endTime {
            self.timer = nil
            return
        }
        let remaining = Date.now().distance(to: endTime)
        self.timer = formatter.string(from: remaining)
    }
}

struct Timer_Previews: PreviewProvider {
    static var previews: some View {
        TimerView(endTime: Date())
    }
}
