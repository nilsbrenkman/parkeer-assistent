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
            guard let response = try? await paymentClient.ideal() else { return }

            amounts = response.amounts
            issuers = response.issuers
            if let previousAmount = UserDefaults.standard.string(forKey: Payment.AMOUNT_KEY) {
                for i in 0 ..< response.amounts.count {
                    if previousAmount == response.amounts[i] {
                        selectedAmount = i
                    }
                }
            }
            if let previousIssuer = UserDefaults.standard.string(forKey: Payment.ISSUER_KEY) {
                for i in 0 ..< response.issuers.count {
                    if previousIssuer == response.issuers[i].issuerId {
                        selectedIssuer = i
                    }
                }
            }
        }
    }
    
    func payment() async throws -> PaymentResponse {
        let amount = amounts![selectedAmount]
        let issuerId = issuers![selectedIssuer].issuerId
        
        UserDefaults.standard.set(amount, forKey: Payment.AMOUNT_KEY)
        UserDefaults.standard.set(issuerId, forKey: Payment.ISSUER_KEY)
        
        let response = try await paymentClient.payment(amount: amount, issuerId: issuerId)
            
        transactionId = response.transactionId
        
        return response
    }
    
    func status() async throws -> StatusResponse? {
        guard let transactionId = transactionId else {
            return nil
        }
        
        if completeData != nil {
            _ = try await paymentClient.complete(transactionId: transactionId,
                                                      data: completeData!)
            completeData = nil
        }
        
        let response = try await paymentClient.status(transactionId)
        
        if response.status == "success" {
            Stats.user.paymentCount += 1
        }
        return response
    }
    
}
