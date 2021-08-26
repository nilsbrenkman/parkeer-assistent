//
//  PaymentClient.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 20/08/2021.
//

import Foundation

protocol PaymentClient {
    func ideal(onComplete: @escaping (IdealResponse) -> Void)
    func payment(amount: String, issuerId: String, onComplete: @escaping (PaymentResponse) -> Void)
    func status(transactionId: String, onComplete: @escaping (StatusResponse) -> Void)
}

class PaymentClientApi: PaymentClient {
    
    static let client = PaymentClientApi()

    private init() {
        //
    }
    
    func ideal(onComplete: @escaping (IdealResponse) -> Void) {
        do {
            try ApiClient.client.call(IdealResponse.self, path: "payment", method: Method.GET, onComplete: onComplete)
        } catch {
            print("Error: \(error)")
        }
    }
    
    func payment(amount: String, issuerId: String, onComplete: @escaping (PaymentResponse) -> Void) {
        do {
            let body = PaymentRequest(amount: amount, issuerId: issuerId)
            try ApiClient.client.call(PaymentResponse.self, path: "payment", method: Method.POST, body: body, onComplete: onComplete)
        } catch {
            print("Error: \(error)")
        }
    }
    
    func status(transactionId: String, onComplete: @escaping (StatusResponse) -> Void) {
        do {
            try ApiClient.client.call(StatusResponse.self, path: "payment/\(transactionId)", method: Method.GET, onComplete: onComplete)
        } catch {
            print("Error: \(error)")
        }
    }
    
}

class PaymentClientMock: PaymentClient {
    
    static let client = PaymentClientMock()
    
    private var nextId = 10000001
    public var transactions: [String:MockTransaction] = [:]

    private init() {
        //
    }
 
    func ideal(onComplete: @escaping (IdealResponse) -> Void) {
        MockClient.mockDelay()
        onComplete(IdealResponse(amounts: ["5,00",
                                           "10,00",
                                           "15,00",
                                           "20,00",
                                           "30,00",
                                           "40,00",
                                           "50,00",
                                           "100,00"],
                                 issuers: [Issuer(issuerId: "ABNANL2A", name: "ABN AMRO - success"),
                                           Issuer(issuerId: "ASNBNL21", name: "ASN Bank - pending"),
                                           Issuer(issuerId: "BUNQNL2A", name: "bunq - pending 10s"),
                                           Issuer(issuerId: "INGBNL2A", name: "ING - error"),
                                           Issuer(issuerId: "KNABNL2H", name: "Knab"),
                                           Issuer(issuerId: "RABONL2U", name: "Rabobank"),
                                           Issuer(issuerId: "RBRBNL21", name: "RegioBank"),
                                           Issuer(issuerId: "REVOLT21", name: "Revolut"),
                                           Issuer(issuerId: "SNSBNL2A", name: "SNS"),
                                           Issuer(issuerId: "TRIONL2U", name: "Triodos Bank"),
                                           Issuer(issuerId: "FVLBNL22", name: "Van Lanschot")]))
    }
    
    func payment(amount: String, issuerId: String, onComplete: @escaping (PaymentResponse) -> Void) {
        MockClient.mockDelay()
        let transactionId = String(nextId)
        nextId += 1
        let t = MockTransaction(transactionId: transactionId, amount: amount, issuerId: issuerId, creationDate: Date.now())
        transactions[t.transactionId] = t
        onComplete(PaymentResponse(redirectUrl: ApiClient.client.baseUrl + "open", transactionId: transactionId))
    }

    func status(transactionId: String, onComplete: @escaping (StatusResponse) -> Void) {
        MockClient.mockDelay()
        
        guard let transaction = transactions[transactionId] else {
            onComplete(StatusResponse(status: "error"))
            return
        }
        
        onComplete(StatusResponse(status: PaymentClientMock.getTransactionStatus(transaction)))
    }
    
    static func getTransactionStatus(_ transaction: MockTransaction) -> String {
        switch transaction.issuerId {
        case "ABNANL2A":
            return "success"
        case "ASNBNL21":
            return "pending"
        case "BUNQNL2A":
            if transaction.creationDate.addingTimeInterval(15) < Date.now() {
                return "success"
            }
            return "pending"
        case "INGBNL2A":
            return "error"
        default:
            return "unknown"
        }
    }
    
}

struct MockTransaction {
    let transactionId: String
    let amount: String
    let issuerId: String
    let creationDate: Date
}
