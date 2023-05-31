//
//  Payment.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 23/08/2021.
//

import Foundation
import SwiftUI

@MainActor
class Payment: ObservableObject {
    
    @Published var transactionId: String?
    @Published var show: Bool = false
    @Published var inProgress: Bool = false
    @Published var completeData: String?

    @Published var amounts: [String]?
    @Published var issuers: [Issuer]?
    @Published var selectedAmount: Int = -1
    @Published var selectedIssuer: Int = -1

    private static let AMOUNT_KEY = "paymentAmount"
    private static let ISSUER_KEY = "paymentIssuer"

    let paymentClient: PaymentClient

    init() throws {
        paymentClient = try ClientManager.instance.get(PaymentClient.self)
    }
    
    func ideal() async {
        if amounts == nil || issuers == nil {
            guard let response = try? await self.paymentClient.ideal() else { return }

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
    
    func payment() async throws -> PaymentResponse {
        let amount = self.amounts![selectedAmount]
        let issuerId = self.issuers![selectedIssuer].issuerId
        
        UserDefaults.standard.set(amount, forKey: Payment.AMOUNT_KEY)
        UserDefaults.standard.set(issuerId, forKey: Payment.ISSUER_KEY)
        
        let response = try await self.paymentClient.payment(amount: amount, issuerId: issuerId)
            
        self.transactionId = response.transactionId
        self.inProgress = true
        
        return response
    }
    
    func status() async throws -> StatusResponse? {
        guard let transactionId = self.transactionId else {
            return nil
        }
        
        if self.completeData != nil {
            _ = try await self.paymentClient.complete(transactionId: transactionId,
                                                      data: self.completeData!)
            self.completeData = nil
        }
        
        let response = try await self.paymentClient.status(transactionId)
        
        if response.status == "success" {
            Stats.user.paymentCount += 1
        }
        return response
    }
    
}
