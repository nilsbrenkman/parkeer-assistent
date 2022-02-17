//
//  Preview.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 17/02/2022.
//

import SwiftUI

#if DEBUG
extension View {
    
    func setupPreview(loggedIn: Bool = false) -> some View {
        let login = try! Login()
        login.isLoggedIn = loggedIn
        let user = try! User()
        let payment = try! Payment()
        return self
            .environmentObject(login)
            .environmentObject(user)
            .environmentObject(payment)
    }
    
}
#endif
