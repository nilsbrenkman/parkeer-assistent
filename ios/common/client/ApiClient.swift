//
//  ApiClient.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 06/07/2021.
//

import Foundation
import SwiftUI

class ApiClient {
      
    static let client = ApiClient()

    static let COOKIE_KEY = "Cookies"

    public let baseUrl: String
    
    private let session: URLSession
    private let url: URL
    private var cookies: SessionCookies

    weak var errorHandler: ErrorHandler?
    
    private init() {
        baseUrl = Util.getSetting("ServerBaseURL")
        session = URLSession(configuration: .default)
        url = URL(string: baseUrl)!
        cookies = SessionCookies()

        if let json = UserDefaults.standard.string(forKey: ApiClient.COOKIE_KEY) {
            setCookies(json)
        }
    }

    func registerErrorHandler(_ errorHandler: ErrorHandler) {
        self.errorHandler = errorHandler
    }
    
    func throwError(_ error: ClientError) {
        self.errorHandler?.handleError(error)
    }
    
    func call<RESPONSE: Decodable>(_ result: RESPONSE.Type, path: String, method: Method, onComplete: @escaping (RESPONSE) -> Void) {
        let body: Response? = nil
        call(result, path: path, method: method, body: body) { response in
            onComplete(response)
        }
    }
    
    func call<REQUEST: Encodable, RESPONSE: Decodable>(_ result: RESPONSE.Type, path: String, method: Method, body: REQUEST? = nil, onComplete: @escaping (RESPONSE) -> Void) {
        
        guard let url = URL(string: baseUrl + path) else {
            return self.throwError(.InvalidPath)
        }
        
        var headers = HTTPCookie.requestHeaderFields(with: getCookies())
        addAnalyticHeaders(&headers)

        var request = URLRequest(url: url)
        request.allHTTPHeaderFields = headers
        request.httpMethod = method.rawValue
        
        if body != nil {
            guard let json = try? JSONEncoder().encode(body) else {
                return self.throwError(.RequestSerialization)
            }
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.httpBody = json
        }
        
        session.dataTask(with: request) { data, response, error in
            
            guard let httpResponse = response as? HTTPURLResponse else {
                return self.throwError(.NoHttpResponse)
            }

            self.updateCookies(httpResponse)

            if httpResponse.statusCode / 100 != 2 {
                switch httpResponse.statusCode {
                    case 401, 403:
                        return self.throwError(.Unauthorized)
                    default:
                        return self.throwError(.ServerError)
                }
            }
            
            guard let data = data else {
                return self.throwError(.EmptyResponse)
            }

            guard let result = try? JSONDecoder().decode(RESPONSE.self, from: data) else {
                return self.throwError(.ResponseSerialization)
            }
            
            onComplete(result)

        }.resume()

    }
    
    private func persistCookies() {
        var persistCookies: [PersistCookie] = []
        for cookie in getCookies() {
            persistCookies.append(PersistCookie(name: cookie.name, value: cookie.value, domain: cookie.domain, path: cookie.path))
        }

        guard let data = try? JSONEncoder().encode(PersistCookies(cookies: persistCookies)),
              let json = String(data: data, encoding: .utf8) else {
            print("Unable to serialize cookies")
            return
        }
        UserDefaults.standard.set(json, forKey: ApiClient.COOKIE_KEY)
    }

    public func setCookies(_ cookies: String) {
        if let data = cookies.data(using: .utf8),
           let persistCookies = try? JSONDecoder().decode(PersistCookies.self, from: data) {
            
            for persistCookie in persistCookies.cookies {
                if let cookie = HTTPCookie(properties: [HTTPCookiePropertyKey.name:   persistCookie.name,
                                                        HTTPCookiePropertyKey.value:  persistCookie.value,
                                                        HTTPCookiePropertyKey.domain: persistCookie.domain,
                                                        HTTPCookiePropertyKey.path:   persistCookie.path]) {
                    _ = setCookie(cookie)
                }
            }
        }
    }
    
    private func setCookie(_ cookie: HTTPCookie) -> Bool {
        switch cookie.name {
        case "session":
            if cookies.session == nil || cookies.session!.value != cookie.value {
                cookies.session = cookie
                return true
            }
            return false
        case "customerid":
            if cookies.customerId == nil || cookies.customerId!.value != cookie.value{
                cookies.customerId = cookie
                return true
            }
            return false
        case "permitid":
            if cookies.permitId == nil || cookies.permitId!.value != cookie.value {
                cookies.permitId = cookie
                return true
            }
            return false
        default:
            print("Ignoring cookie \(cookie.name)")
        }
        return false
    }
    
    private func getCookies() -> [HTTPCookie] {
        var list: [HTTPCookie] = []
        if let session = cookies.session {
            list.append(session)
        }
        if let customerId = cookies.customerId {
            list.append(customerId)
        }
        if let permitId = cookies.permitId {
            list.append(permitId)
        }
        return list
    }
    
    private func updateCookies(_ httpResponse: HTTPURLResponse) {
        var updated = false
        if let responseHeaderFields = httpResponse.allHeaderFields as? [String : String] {
            let responseCookies = HTTPCookie.cookies(withResponseHeaderFields: responseHeaderFields, for: url)
            
            for responseCookie in responseCookies {
                if setCookie(responseCookie) {
                    updated = true
                }
            }
        }
        if updated {
            persistCookies()
        }
    }
    
    private func addAnalyticHeaders(_ headers: inout [String : String]) {
        if let uuid = UIDevice.current.identifierForVendor?.uuidString,
           let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String,
           let build = Bundle.main.infoDictionary?["CFBundleVersion"] as? String {
            
            let os = ProcessInfo.processInfo.operatingSystemVersion
            
            headers["PA-UserId"] = uuid
            headers["PA-OS"] = "iOS"
            headers["PA-SDK"] = String(os.majorVersion) + "." + String(os.minorVersion) + "." + String(os.patchVersion)
            headers["PA-Version"] = version
            headers["PA-Build"] = build
        }
    }
    
}

enum Method: String {
    case GET
    case POST
    case DELETE
}

protocol ErrorHandler: AnyObject {
    func handleError(_ error: ClientError)
}

enum ClientError: Error {
    case InvalidPath
    case RequestSerialization
    case ResponseSerialization
    case NoHttpResponse
    case Unauthorized
    case ServerError
    case EmptyResponse
}

struct SessionCookies {
    var session: HTTPCookie?
    var customerId: HTTPCookie?
    var permitId: HTTPCookie?
}

struct PersistCookies: Codable {
    var cookies: [PersistCookie] = []
}

struct PersistCookie: Codable {
    var name: String
    var value: String
    var domain: String
    var path: String
}
