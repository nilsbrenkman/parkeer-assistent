//
//  Keychain.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 03/07/2021.
//

import Foundation

class Keychain {
    
    static let store = Keychain()
    
    static let account = "{USER}"
    static let service = "ParkeerAssistentAmsterdam"
    
    private init() {
        //
    }
    
    func storeCredentials(username: String, password: String) throws {
        let credentials = Credentials(username: username, password: password)
        guard let secureData = try? JSONEncoder().encode(credentials).base64EncodedData() else {
            throw KeychainError.Encoding
        }
        
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: Keychain.account,
            kSecAttrService as String: Keychain.service,
            kSecValueData as String: secureData
        ]
        let status = SecItemAdd(query as CFDictionary, nil)
        
        if status == errSecDuplicateItem {
            try updateCredentials(secureData: secureData)
            return
        }
        guard status == errSecSuccess else {
            throw KeychainError.Save
        }
    }
    
    func updateCredentials(secureData: Data) throws {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: Keychain.account,
            kSecAttrService as String: Keychain.service
        ]
        let attributes: [String: Any] = [
            kSecValueData as String: secureData
        ]
        let status = SecItemUpdate(query as CFDictionary, attributes as CFDictionary)
        guard status == errSecSuccess else {
            throw KeychainError.Update
        }
    }
    
    func retrieveCredentials() -> Credentials? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: Keychain.account,
            kSecAttrService as String: Keychain.service,
            kSecMatchLimit as String: kSecMatchLimitOne,
            kSecReturnAttributes as String: true,
            kSecReturnData as String: true
        ]
        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)
        guard status == errSecSuccess else {
            return nil
        }
        guard let existingItem = item as? [String: Any],
              let dataBase64 = existingItem[kSecValueData as String] as? Data,
              let data = Data(base64Encoded: dataBase64),
              let credentials = try? JSONDecoder().decode(Credentials.self, from: data) else {
            return nil
        }
        return credentials
    }
    
}

struct Credentials: Codable {
    var username: String
    var password: String
}

enum KeychainError: Error {
    case Encoding
    case Save
    case Update
}
