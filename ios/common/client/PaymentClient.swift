//
//  PaymentClient.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 20/08/2021.
//

import os
import Foundation

protocol PaymentClient {
    func ideal() async throws -> IdealResponse
    func payment(amount: String, issuerId: String) async throws -> PaymentResponse
    func complete(transactionId: String, data: String) async throws -> Response
    func status(_ transactionId: String) async throws -> StatusResponse
}

class PaymentClientApi: PaymentClient {
    
    static let client = PaymentClientApi()

    private init() {
        //
    }
    
    func ideal() async throws -> IdealResponse {
        return try await ApiClient.client.call(IdealResponse.self, path: "payment", method: Method.GET)
    }
    
    func payment(amount: String, issuerId: String) async throws -> PaymentResponse {
        let body = PaymentRequest(amount: amount, issuerId: issuerId)
        return try await ApiClient.client.call(PaymentResponse.self, path: "payment", method: Method.POST, body: body)
    }
    
    func complete(transactionId: String, data: String) async throws -> Response {
        let body = CompleteRequest(transactionId: transactionId, data: data)
        return try await ApiClient.client.call(Response.self, path: "payment/complete", method: Method.POST, body: body)
    }
    
    func status(_ transactionId: String) async throws -> StatusResponse {
        return try await ApiClient.client.call(StatusResponse.self, path: "payment/\(transactionId)", method: Method.GET)
    }
    
}

class PaymentClientMock: PaymentClient {
    
    static let client = PaymentClientMock()
    
    private var nextId = 10000001
    public var transactions: [String:MockTransaction] = [:]

    private init() {
        //
    }
 
    func ideal() async throws -> IdealResponse {
        guard MockClient.client.authorized() else { throw ClientError.Unauthorized }
        
        return IdealResponse(amounts: ["5,00",
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
                                       Issuer(issuerId: "FVLBNL22", name: "Van Lanschot")])
    }
    
    func payment(amount: String, issuerId: String) async throws -> PaymentResponse {
        guard MockClient.client.authorized() else { throw ClientError.Unauthorized }
        
        let transactionId = String(nextId)
        nextId += 1
        let t = MockTransaction(transactionId: transactionId, amount: amount, issuerId: issuerId, creationDate: Date.now())
        transactions[t.transactionId] = t
        return PaymentResponse(redirectUrl: ApiClient.client.baseUrl + "open", transactionId: transactionId)
    }

    func complete(transactionId: String, data: String) async throws -> Response {
        return Response(success: true)
    }
    
    func status(_ transactionId: String) async throws -> StatusResponse {
        guard MockClient.client.authorized() else { throw ClientError.Unauthorized }
        
        guard let transaction = transactions[transactionId] else {
            return StatusResponse(status: "error")
        }
        
        return StatusResponse(status: PaymentClientMock.getTransactionStatus(transaction))
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
