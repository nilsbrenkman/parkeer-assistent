//
//  WheelSelector.swift
//  parkeerassistent
//
//  Created by Nils Brenkman on 29/06/2021.
//

import SwiftUI

struct WheelSelector: View {
    
    var config: Config
    var onChange: (Int) -> Void
    var onEnd: () -> Void
    
    @State private var angle: CGFloat = 0.0
    @State private var angleBegin: CGFloat = 0.0
    @State private var anglePrev: CGFloat = 0.0
    
    var body: some View {
        
        ZStack {
            
            Circle()
                .stroke(Color(red: 0.8, green: 0.8, blue: 0.8), style: StrokeStyle(lineWidth: config.size * 2))
                .frame(width: config.radius * 2, height: config.radius * 2)
                .shadow(color: Color.ui.bw0, radius: 2)
            
            Circle()
                .stroke(Color(red: 0.9, green: 0.9, blue: 0.9),
                        style: StrokeStyle(lineWidth: config.size, lineCap: .butt, dash: [3, 4.8539]))
                .frame(width: config.radius * 2, height: config.radius * 2)
                .padding(20)
                .rotationEffect(Angle.degrees(Double(angle)))
                .gesture(DragGesture(minimumDistance: 0)
                            .onEnded({ value in
                                angleBegin = angle
                                anglePrev = 0
                                self.onEnd()
                            })
                            .onChanged({ value in
                                change(value: value)
                            }))
            
            Circle()
                .fill(Color(red: 0.7, green: 0.7, blue: 0.7))
                .frame(width: config.radius * 2, height: config.radius * 2)
                .scaleEffect(0.75)
            
        }
        
    }
    
    private func change(value: DragGesture.Value) {
        
        let angleStart = getDegree(x: value.startLocation.x, y: value.startLocation.y)
        let angleLocation = getDegree(x: value.location.x, y: value.location.y)
        
        var angleDiff = (angleLocation - angleStart - anglePrev).truncatingRemainder(dividingBy: 360)
        if angleDiff > 180 {
            angleDiff = angleDiff - 360
        } else if angleDiff < -180 {
            angleDiff = angleDiff + 360
        }
        
        anglePrev = anglePrev + angleDiff
        angle = angleBegin + anglePrev
        
        let value = Int(anglePrev / 360 * 60)
        self.onChange(value)
        
    }
    
    private func getDegree(x: CGFloat, y: CGFloat) -> CGFloat {
        let a = atan2(y - config.radius, x - config.radius)
        return a * 180 / .pi
    }
}

struct Config {
    var radius: CGFloat
    var size: CGFloat
}

struct WheelSelector_Previews: PreviewProvider {
    static var previews: some View {
        WheelSelector(config: Config(
            radius: 50,
            size: 12
        ), onChange: { value in
            //
        }, onEnd: {
            //
        })
    }
}
