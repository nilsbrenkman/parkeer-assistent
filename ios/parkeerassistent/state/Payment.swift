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
                        if let previousAmount = UserDefaults.standard.string(forKey: PaymentView.amountKey) {
                            for i in 0 ..< response.amounts.count {
                                if previousAmount == response.amounts[i] {
                                    self.selectedAmount = i
                                }
                            }
                        }
                        if let previousIssuer = UserDefaults.standard.string(forKey: PaymentView.issuerKey) {
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
        
        UserDefaults.standard.set(amount, forKey: PaymentView.amountKey)
        UserDefaults.standard.set(issuerId, forKey: PaymentView.issuerKey)
        
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
                DispatchQueue.main.async {
                    onComplete(response)
                }
            }
        }
    }
 
}
