//
//  Keychain.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 03/07/2021.
//

import Foundation

class Keychain {
    
    static let store = Keychain()
    
    private static let RECENT_USED_KEY = "credentialsRecentlyUsed"
    private static let AUTO_LOGIN_DISABLED_KEY = "autoLoginDisabled"
    
    private static let account = "{USER}"
    private static let service = "ParkeerAssistentAmsterdam"
    
    private init() {
        //
    }
    
    static func storeCredentials(username: String, password: String) throws {
        var credentials = retrieveCredentials()
        if let i = credentials.firstIndex(where: {$0.username == username}) {
            // Credentials with this username found
            if credentials[i].password != password {
                // Password is different, so update
                credentials[i] = Credentials(username: username, password: password)
                try storeCredentials(credentials: credentials)
            }
            return
        }
        credentials.append(Credentials(username: username, password: password))
        try storeCredentials(credentials: credentials)
        return
    }
    
    static func updateCredentials(_ account: Credentials, username: String, password: String, alias: String?) throws {
        var credentials = retrieveCredentials()
        if let i = credentials.firstIndex(where: {$0.username == account.username}) {
            // Credentials with this username found
            let old = credentials[i]
            if old.username != username || old.password != password || old.alias != alias {
                // account is different, so update
                credentials[i] = Credentials(alias: alias, username: username, password: password)
                try storeCredentials(credentials: credentials)
            }
        }
    }
    
    static func deleteCredentials(account: Credentials) throws {
        var credentials = retrieveCredentials()
        if let i = credentials.firstIndex(where: {$0.username == account.username}) {
            credentials.remove(at: i)
            try storeCredentials(credentials: credentials)
        }
    }
    
    static func storeCredentials(credentials: [Credentials]) throws {
        guard let secureData = try? JSONEncoder().encode(CredentialsList(list: credentials)).base64EncodedData() else {
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
    
    static func updateCredentials(secureData: Data) throws {
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
    
    static func retrieveCredentials() -> [Credentials] {
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
            return []
        }
        guard let existingItem = item as? [String: Any],
              let dataBase64 = existingItem[kSecValueData as String] as? Data,
              let data = Data(base64Encoded: dataBase64) else {
            return []
        }
        if let credentials = try? JSONDecoder().decode(CredentialsList.self, from: data) {
            return credentials.list
        }
        if let credentials = try? JSONDecoder().decode(Credentials.self, from: data) {
            return [credentials]
        }
        return []
    }
    
    static func getRecent(_ accounts: [Credentials]) -> Credentials? {
        if let recent = UserDefaults.standard.string(forKey: Keychain.RECENT_USED_KEY) {
            return accounts.first(where: {$0.username == recent})
        }
        return nil
    }

    static func setRecent(_ username: String?) {
        UserDefaults.standard.set(username, forKey: Keychain.RECENT_USED_KEY)
    }
    
    static func autoLogin() -> Bool {
        return !UserDefaults.standard.bool(forKey: Keychain.AUTO_LOGIN_DISABLED_KEY)
    }
  
    static func autoLogin(enabled: Bool) {
        UserDefaults.standard.set(!enabled, forKey: Keychain.AUTO_LOGIN_DISABLED_KEY)
    }
    
}

struct CredentialsList: Codable {
    var list: [Credentials]
}

struct Credentials: Codable {
    var alias: String?
    var username: String
    var password: String
}

enum KeychainError: Error {
    case Encoding
    case Save
    case Update
}
