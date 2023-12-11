//
//  PaymentView.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 20/08/2021.
//

import SwiftUI

@MainActor
struct PaymentView: View {
    
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.openURL) var openURL
    
    @EnvironmentObject var payment: Payment
    @EnvironmentObject var user: User
    
    @State private var wait: Bool = false
    @State private var disableStatusCheck: Int = 0
    
    @State private var statusTask: Task<Void, Never>?
    
    var body: some View {
        Form {
            if payment.transactionId == nil {
                Section {
                    Text(Lang.Payment.recipientMsg.localized())
                        .bold()
                        .foregroundColor(Color.ui.danger)
                }
                Section {
                    if let amounts = $payment.amounts.wrappedValue {
                        NavigationLink(destination: InsetPicker(labels: amounts.map{ self.formatAmount($0) }, selected: $payment.selectedAmount)) {
                            HStack {
                                Text(Lang.Payment.amount.localized())
                                Spacer()
                                Text(payment.selectedAmount == -1 ? Lang.Common.select.localized() : formatAmount(amounts[payment.selectedAmount]))
                                    .foregroundColor(Color(UIColor.systemGray))
                            }
                        }
                    }
                    if let issuers = $payment.issuers.wrappedValue {
                        NavigationLink(destination: InsetPicker(labels: issuers.map{ $0.name }, selected: $payment.selectedIssuer)) {
                            HStack {
                                Text(Lang.Payment.bank.localized())
                                Spacer()
                                Text("\(payment.selectedIssuer == -1 ? Lang.Common.select.localized() : issuers[payment.selectedIssuer].name)")
                                    .foregroundColor(Color(UIColor.systemGray))
                            }
                        }
                    }
                }
                Section {
                    Button(action: {
                        Task {
                            self.wait = true
                            guard let response = try? await payment.payment(),
                                  let redirectUrl = URL(string: response.redirectUrl) else {
                                
                                MessageManager.instance.addMessage(Lang.Payment.redirectErrorMsg.localized(), type: Type.ERROR)
                                return
                            }
                            openURL(redirectUrl)
                        }
                    }){
                        Text(Lang.Payment.start.localized())
                            .font(.title3)
                            .bold()
                            .wait($wait)
                    }
                    .style(.success, disabled: payment.selectedAmount < 0 && payment.selectedIssuer < 0)
                    .disabled(payment.selectedAmount < 0 && payment.selectedIssuer < 0)
                }
            } else {
                Section {
                    ZStack {
                        ProgressView()
                    }
                    .frame(height: 200)
                    .centered()
                    .animation(nil, value: 0)
                }
            }
        }
        .onAppear {
            if payment.transactionId != nil {
                startStatusTask()
            } else {
                Task {
                    await self.payment.ideal()
                }
            }
        }
        .onChange(of: scenePhase) { phase in
            if phase == .active && payment.transactionId != nil {
                startStatusTask()
            } else {
                Log.debug("Cancelling status task")
                statusTask?.cancel()
                statusTask = nil
            }
        }
        .pageTitle(payment.transactionId == nil
                       ? Lang.User.addBalance.localized()
                       : Lang.Payment.inProgress.localized(),
                   dismiss: {
            if payment.transactionId != nil {
                cancelInProgress()
            }
            user.page = nil
        })
    }
    
    private func formatAmount(_ amount: String) -> String {
        return "€ \(amount.replacingOccurrences(of: ",", with: "."))"
    }
    
    private func startStatusTask() {
        guard statusTask == nil else {
            return
        }
        statusTask = Task.detached(priority: .background) {
            Log.debug("Starting status task")
            var wait = 5.0
            while !Task.isCancelled {
                if let response = try? await payment.status() {
                    await MainActor.run {
                        self.handleStatusResponse(response)
                    }
                }
                Log.debug("Waiting \(wait, privacy: .public) seconds")
                try? await Task.sleep(nanoseconds: UInt64(wait * 1_000_000_000))
                wait = min(wait * 1.5, 30)
            }
            Log.debug("Exiting status task")
        }
    }
    
    private func cancelInProgress() {
        payment.transactionId = nil
        statusTask?.cancel()
        statusTask = nil
        Task {
            await user.getBalance()
        }
    }
    
    private func handleStatusResponse(_ response: StatusResponse) {
        self.wait = false
        switch response.status {
        case "success":
            MessageManager.instance.addMessage(Lang.Payment.successMsg.localized(), type: Type.SUCCESS) {
                cancelInProgress()
                user.page = nil
            }
            break
        case "pending":
            break
        case "error":
            MessageManager.instance.addMessage(Lang.Payment.errorMsg.localized(), type: Type.ERROR) {
                cancelInProgress()
            }
            break
        default:
            MessageManager.instance.addMessage(Lang.Payment.unknownMsg.localized(), type: Type.WARN) {
                cancelInProgress()
            }
            break
        }
    }
}

struct PaymentView_Previews: PreviewProvider {
    static var previews: some View {
        PaymentView()
    }
}
