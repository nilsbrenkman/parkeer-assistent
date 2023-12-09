//
//  SettingsView.swift
//  app
//
//  Created by Nils Brenkman on 09/12/2023.
//

import SwiftUI

@MainActor
struct SettingsView: View {
    
    @Environment(\.presentationMode) var presentationMode: Binding<PresentationMode>
    
    @State private var onStart = true
    @State private var onStop = true
    @State private var reminders = true
    @State private var reminderInterval = 1.0
    
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
                .padding(.vertical, Constants.padding.nano)
            }
            //                Toggle(Lang.Account.autoLogin.localized(), isOn: $autoLogin)
            //                    .onChange(of: autoLogin) { toggle in
            //                        Keychain.autoLogin(enabled: toggle)
            //                    }
            //                    .padding(.vertical, Constants.padding.small)
            //                    .padding(.horizontal)
            //                    .background(RoundedRectangle(cornerRadius: Constants.radius.normal)
            //                                    .fill(Color.system.groupedRowBackground))

        }
        .onAppear {
            onStart = UserDefaults.standard.bool(forKey: Notifications.START_KEY)
            onStop = UserDefaults.standard.bool(forKey: Notifications.STOP_KEY)
            reminders = UserDefaults.standard.bool(forKey: Notifications.REMINDER_KEY)
            reminderInterval = UserDefaults.standard.double(forKey: Notifications.INTERVAL_KEY)
        }
        .navigationBarTitle(Text(Lang.Settings.header.localized()))
        .navigationBarBackButtonHidden(true)
        .navigationBarItems(
            leading: Button(action: { self.presentationMode.wrappedValue.dismiss() }) {
                Image(systemName: "arrow.left")
            }
        )
    }
}

#Preview {
    SettingsView()
}
