//
//  Payment.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 23/08/2021.
//

import Foundation
import SwiftUI

class Payment: ObservableObject {
    
    @Published var transactionId: String?
    @Published var show: Bool = false
    @Published var inProgress: Bool = false

    @Published var amounts: [String]?
    @Published var issuers: [Issuer]?
    @Published var selectedAmount: Int = -1
    @Published var selectedIssuer: Int = -1

    private static let AMOUNT_KEY    = "paymentAmount"
    private static let ISSUER_KEY    = "paymentIssuer"
    private static let COMPLETED_KEY = "paymentCompleted"

    let paymentClient: PaymentClient

    init() throws {
        paymentClient = try ClientManager.instance.get(PaymentClient.self)
    }
    
    func ideal() {
        if amounts == nil || issuers == nil {
            DispatchQueue.global().async {
                self.paymentClient.ideal { response in
                    DispatchQueue.main.async {
                        self.amounts = response.amounts
                        self.issuers = response.issuers
                        if let previousAmount = UserDefaults.standard.string(forKey: Payment.AMOUNT_KEY) {
                            for i in 0 ..< response.amounts.count {
                                if previousAmount == response.amounts[i] {
                                    self.selectedAmount = i
                                }
                            }
                        }
                        if let previousIssuer = UserDefaults.standard.string(forKey: Payment.ISSUER_KEY) {
                            for i in 0 ..< response.issuers.count {
                                if previousIssuer == response.issuers[i].issuerId {
                                    self.selectedIssuer = i
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    func payment(onComplete: @escaping (PaymentResponse) -> Void) {
        let amount = self.amounts![selectedAmount]
        let issuerId = self.issuers![selectedIssuer].issuerId
        
        UserDefaults.standard.set(amount, forKey: Payment.AMOUNT_KEY)
        UserDefaults.standard.set(issuerId, forKey: Payment.ISSUER_KEY)
        
        DispatchQueue.global().async {
            self.paymentClient.payment(amount: amount, issuerId: issuerId) { response in
                DispatchQueue.main.async {
                    self.transactionId = response.transactionId
                    self.inProgress = true
                    onComplete(response)
                }
            }
        }
    }
    
    func status(onComplete: @escaping (StatusResponse) -> Void) {
        guard let transactionId = self.transactionId else {
            return
        }
        DispatchQueue.global().async {
            self.paymentClient.status(transactionId: transactionId) { response in
                if response.status == "success" {
                    UserDefaults.standard.set(true, forKey: Payment.COMPLETED_KEY)
                }
                DispatchQueue.main.async {
                    onComplete(response)
                }
            }
        }
    }
 
    func showRedirectMessage() -> Bool {
        return !UserDefaults.standard.bool(forKey: Payment.COMPLETED_KEY)
    }
    
}
