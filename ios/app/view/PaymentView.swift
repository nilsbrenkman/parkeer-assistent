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

    var body: some View {
        Form {
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
                if payment.transactionId == nil {
                    Button(action: {
                        Task {
                            self.wait = true
                            guard let response = try? await payment.payment(),
                                  let redirectUrl = URL(string: response.redirectUrl) else {
                                
                                MessageManager.instance.addMessage(Lang.Payment.redirectErrorMsg.localized(), type: Type.ERROR)
                                return
                            }
                            if payment.showRedirectMessage() {
                                MessageManager.instance.addMessage(Lang.Payment.redirectMsg.localized(), type: Type.INFO) {
                                    openURL(redirectUrl)
                                }
                            } else {
                                openURL(redirectUrl)
                            }
                        }
                    }){
                        Text(Lang.Payment.start.localized())
                            .font(.title3)
                            .bold()
                            .wait($wait)
                    }
                    .style(.success, disabled: payment.selectedAmount < 0 && payment.selectedIssuer < 0)
                    .disabled(payment.selectedAmount < 0 && payment.selectedIssuer < 0)
                } else {
                    Button(action: {
                        Task {
                            self.wait = true
                            if let response = try? await payment.status() {
                                self.handleStatusResponse(response)
                            } else {
                                self.wait = false
                            }
                        }
                    }){
                        Text(disableStatusCheck > 0 ? "\(disableStatusCheck)" : Lang.Payment.status.localized())
                            .font(.title3)
                            .bold()
                            .wait($wait)
                    }
                    .style(.success, disabled: disableStatusCheck > 0)
                    .disabled(disableStatusCheck > 0)
                }
   
                Button(action: {
                    payment.show = false
                    payment.transactionId = nil
                }) {
                    Text(Lang.Common.cancel.localized())
                        .font(.title3)
                        .bold()
                        .centered()
                }
                .style(.cancel)
            }
        }
        .navigationBarHidden(true)
        .onAppear {
            Task {
                await self.payment.ideal()
            }
        }
        .onChange(of: scenePhase) { phase in
            if phase == .active && payment.inProgress {
                Task {
                    if let response = try? await payment.status() {
                        payment.inProgress = false
                        self.handleStatusResponse(response)
                    }
                }
            }
        }
    }
    
    func formatAmount(_ amount: String) -> String {
        return "€ \(amount.replacingOccurrences(of: ",", with: "."))"
    }

    func handleStatusResponse(_ response: StatusResponse) {
        self.wait = false
        switch response.status {
        case "success":
            MessageManager.instance.addMessage(Lang.Payment.successMsg.localized(), type: Type.SUCCESS) {
                payment.transactionId = nil
                payment.show = false
                Task {
                    await user.getBalance()
                }
            }
            break
        case "pending":
            self.disableStatusCheck = 15
            Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { timer in
                Task {
                    await MainActor.run {
                        self.disableStatusCheck -= 1
                    }
                }
//                if self.disableStatusCheck == 0 {
//                    timer.invalidate()
//                }
            }
            MessageManager.instance.addMessage(Lang.Payment.pendingMsg.localized(), type: Type.INFO)
            break
        case "error":
            MessageManager.instance.addMessage(Lang.Payment.errorMsg.localized(), type: Type.ERROR) {
                payment.transactionId = nil
                Task {
                    await user.getBalance()
                }
            }
            break
        default:
            MessageManager.instance.addMessage(Lang.Payment.unknownMsg.localized(), type: Type.WARN) {
                payment.transactionId = nil
                Task {
                    await user.getBalance()
                }
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
