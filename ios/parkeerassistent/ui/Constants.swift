//
//  Constants.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 30/06/2021.
//

//import Foundation
import SwiftUI

struct Constants {
    
    static let sound   = Constants.Sound()
    static let spacing = Constants.Spacing()
    static let padding = Constants.Padding()
    static let radius  = Constants.Radius()
    static let license = Constants.License()

    struct Sound {
        let carHorn = UNNotificationSound(named: UNNotificationSoundName("car-horn.wav"))
    }

    struct Spacing {
        let normal: CGFloat = 20
        let small:  CGFloat = 10
        let xSmall: CGFloat = 5
    }

    struct Padding {
        let normal: CGFloat = 20
    }
    
    struct Radius {
        let normal: CGFloat = 12
        let small:  CGFloat = 6
    }
    
    struct License {
        var width:   CGFloat = 140
        var height:  CGFloat = 36
        var padding: CGFloat = 5
    }

}

enum GenericError: Error {
    case VisitorNotFound
    case InvalidDate
}
