//
//  License.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 27/06/2021.
//

import Foundation

class License {
    
    static func formatLicense(_ license: String) -> String {
        let chars = normalise(license)

        for format in formats {
            if format.matches(chars) {
                return format.format(chars)
            }
        }

        return String(chars)
    }

    static func normalise(_ s: String) -> [Character] {
        var chars: [Character] = []
        for c in s.uppercased() {
            if "0"..."9" ~= c || "A"..."Z" ~= c {
                chars.append(c)
            }
        }
        return chars
    }

    static func isGroup(chars: Array<Character>.SubSequence) -> Bool {
        let group = Array(chars)
        if group.count == 1 {
            return true
        }
        for c in group[1..<group.count] {
            if !isSame(a: group[0], b: c) {
                return false
            }
        }
        return true
    }

    static func isSame(a: Character, b: Character) -> Bool {
        return (isDigit(c: a) && isDigit(c: b)) || (!isDigit(c: a) && !isDigit(c: b))
        
    }
    
    static func isDigit(c: Character) -> Bool {
        return "0"..."9" ~= c
    }

    static let formats = [
        // 2-2-2
        Format(
            matches: {chars in
                return chars.count == 6 && isGroup(chars: chars[0...1]) && isGroup(chars: chars[2...3]) && isGroup(chars: chars[4...5])
            },
            format: {chars in
                var c = chars
                c.insert("-", at: 2)
                c.insert("-", at: 5)
                return String(c)
            }
        ),
        // 2-3-1
        Format(
            matches: {chars in
                return chars.count == 6 && isGroup(chars: chars[0...1]) && isGroup(chars: chars[2...4])
            },
            format: {chars in
                var c = chars
                c.insert("-", at: 2)
                c.insert("-", at: 6)
                return String(c)
            }
        ),
        // 1-3-2
        Format(
            matches: {chars in
                return chars.count == 6 && isGroup(chars: chars[1...3]) && isGroup(chars: chars[4...5])
            },
            format: {chars in
                var c = chars
                c.insert("-", at: 1)
                c.insert("-", at: 5)
                return String(c)
            }
        ),
        // 3-2-1
        Format(
            matches: {chars in
                return chars.count == 6 && isGroup(chars: chars[0...2]) && isGroup(chars: chars[3...4])
            },
            format: {chars in
                var c = chars
                c.insert("-", at: 3)
                c.insert("-", at: 6)
                return String(c)
            }
        ),
        // 1-2-3
        Format(
            matches: {chars in
                return chars.count == 6 && isGroup(chars: chars[1...2]) && isGroup(chars: chars[3...5])
            },
            format: {chars in
                var c = chars
                c.insert("-", at: 1)
                c.insert("-", at: 4)
                return String(c)
            }
        )
    ]

}

struct Format {
    let matches: ([Character]) -> Bool
    let format: ([Character]) -> String
}


