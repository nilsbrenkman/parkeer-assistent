//
//  Constants.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 30/06/2021.
//

import SwiftUI

struct Constants {
    
    static let sound   = Constants.Sound()
    static let spacing = Constants.Spacing()
    static let padding = Constants.Padding()
    static let radius  = Constants.Radius()
    static let license = Constants.License()

    struct Sound {
        let carHorn = "car-horn.wav"
    }

    struct Spacing {
        let xLarge: CGFloat = 36
        let large:  CGFloat = 30
        let normal: CGFloat = 20
        let small:  CGFloat = 10
        let xSmall: CGFloat = 5
    }

    struct Padding {
        let normal: CGFloat = 20
        let small:  CGFloat = 10
        let xSmall: CGFloat = 5
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

enum AuthenticationError: Error {
    case Unavailable
    case Failed
}
