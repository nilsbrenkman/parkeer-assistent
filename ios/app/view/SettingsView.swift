//
//  SettingsView.swift
//  app
//
//  Created by Nils Brenkman on 09/12/2023.
//

import SwiftUI

@MainActor
struct SettingsView: View {
    
    @EnvironmentObject var user: User
    
    @State private var onStart = true
    @State private var onStop = true
    @State private var reminders = true
    @State private var reminderInterval = 1.0
    @State private var autoLogin = true

    var body: some View {
        Form {
            Section(header: SectionHeader(Lang.Settings.notifications.localized())) {
                Toggle(Lang.Settings.onStart.localized(), isOn: $onStart)
                    .padding(.vertical, Constants.padding.nano)
                    .onChange(of: onStart) { _onStart in
                        UserDefaults.standard.set(_onStart, forKey: Notifications.START_KEY)
                    }
                Toggle(Lang.Settings.onStop.localized(), isOn: $onStop)
                    .padding(.vertical, Constants.padding.nano)
                    .onChange(of: onStop) { _onStop in
                        UserDefaults.standard.set(_onStop, forKey: Notifications.STOP_KEY)
                    }
                VStack {
                    Toggle(Lang.Settings.reminders.localized(), isOn: $reminders)
                        .padding(.vertical, Constants.padding.nano)
                        .onChange(of: reminders) { _reminders in
                            UserDefaults.standard.set(_reminders, forKey: Notifications.REMINDER_KEY)
                        }
                    HStack {
                        Slider(value: $reminderInterval, in: 0...5, step: Double.Stride(1.0)) { editing in
                            if !editing {
                                UserDefaults.standard.setValue(reminderInterval, forKey: Notifications.INTERVAL_KEY)
                            }
                        }
                        .disabled(!reminders)
                        
                        Text(Notifications.INTERVAL_LABELS[Int(reminderInterval)])
                            .foregroundStyle(reminders ? Color.ui.bw0 : Color.ui.bw30)
                            .frame(width: 50)
                    }
                    
                }
                .padding(.vertical, Constants.padding.nano)
            }
            
            Section(header: SectionHeader(Lang.Login.login.localized())) {
                Toggle(Lang.Account.autoLogin.localized(), isOn: $autoLogin)
                    .padding(.vertical, Constants.padding.nano)
                    .onChange(of: autoLogin) { toggle in
                        Keychain.autoLogin(enabled: toggle)
                    }
            }
        }
        .onAppear {
            onStart = UserDefaults.standard.bool(forKey: Notifications.START_KEY)
            onStop = UserDefaults.standard.bool(forKey: Notifications.STOP_KEY)
            reminders = UserDefaults.standard.bool(forKey: Notifications.REMINDER_KEY)
            reminderInterval = UserDefaults.standard.double(forKey: Notifications.INTERVAL_KEY)
            autoLogin = Keychain.autoLogin()
        }
        .pageTitle(Lang.Settings.header.localized(), dismiss: { user.page = nil })
    }
}

#Preview {
    SettingsView()
}
