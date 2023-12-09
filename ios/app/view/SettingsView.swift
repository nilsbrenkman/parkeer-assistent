//
//  SettingsView.swift
//  app
//
//  Created by Nils Brenkman on 09/12/2023.
//

import SwiftUI

@MainActor
struct SettingsView: View {
    
    @State private var onStart = true //UserDefaults.standard.bool(forKey: Notifications.START_KEY)
    @State private var onStop = true //UserDefaults.standard.bool(forKey: Notifications.STOP_KEY)
    @State private var reminders = true //UserDefaults.standard.bool(forKey: Notifications.REMINDER_KEY)
    @State private var reminderInterval = 1.0 //UserDefaults.standard.double(forKey: Notifications.INTERVAL_KEY)
    
    var body: some View {
        Form {
            Section("Notifications") {
                Toggle("On session start", isOn: $onStart)
                    .onChange(of: onStart) { _onStart in
                        UserDefaults.standard.set(_onStart, forKey: Notifications.START_KEY)
                    }

                Toggle("On session end", isOn: $onStop)
                    .onChange(of: onStop) { _onStop in
                        UserDefaults.standard.set(_onStop, forKey: Notifications.STOP_KEY)
                    }
                Toggle("Reminders", isOn: $reminders)
                    .onChange(of: reminders) { _reminders in
                        UserDefaults.standard.set(_reminders, forKey: Notifications.REMINDER_KEY)
                    }
                HStack {
                    Slider(value: $reminderInterval, in: 0...5, step: Double.Stride(1.0)) { editing in
                        if !editing {
                            UserDefaults.standard.setValue(reminderInterval, forKey: Notifications.INTERVAL_KEY)
                        }
                    }.disabled(!reminders)
                    
                    Text(Notifications.INTERVAL_LABELS[Int(reminderInterval)])
                        .foregroundStyle(reminders ? Color.ui.bw0 : Color.ui.bw30)
                        .frame(width: 50)
                }
            }
        }
        .onAppear {
            onStart = UserDefaults.standard.bool(forKey: Notifications.START_KEY)
            onStop = UserDefaults.standard.bool(forKey: Notifications.STOP_KEY)
            reminders = UserDefaults.standard.bool(forKey: Notifications.REMINDER_KEY)
            reminderInterval = UserDefaults.standard.double(forKey: Notifications.INTERVAL_KEY)
        }
    }

}

#Preview {
    SettingsView()
}
