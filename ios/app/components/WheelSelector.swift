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
    
    @State private var valuePrev = 0

    @State private var angle: CGFloat = 0.0
    @State private var angleBegin: CGFloat = 0.0
    @State private var anglePrev: CGFloat = 0.0
    
    @State private var showRotate = true
    @State private var rotate = false
  
    var body: some View {
        
        ZStack {
            
            Circle()
                .stroke(Color.ui.grey80, style: StrokeStyle(lineWidth: config.size * 2))
                .frame(width: config.radius * 2, height: config.radius * 2)
                .shadow(color: .primary, radius: 2)
            
            Circle()
                .stroke(Color.ui.grey90, style: StrokeStyle(lineWidth: config.size, lineCap: .butt, dash: [3, 4.8539]))
                .frame(width: config.radius * 2, height: config.radius * 2)
                .padding(20)
                .rotationEffect(Angle.degrees(Double(angle)))
                .gesture(DragGesture(minimumDistance: 0)
                            .onEnded({ value in
                                angleBegin = angle
                                anglePrev = 0
                                valuePrev = 0
                            })
                            .onChanged({ value in
                                change(value: value)
                            }))
                .accessibility(identifier: "wheel-selector")

            Circle()
                .stroke(Color.ui.grey70, style: StrokeStyle(lineWidth: 2))
                .frame(width: getFrameSize(offset: -1), height: getFrameSize(offset: -1))
            
            Circle()
                .stroke(Color.ui.grey80, style: StrokeStyle(lineWidth: 2))
                .frame(width: getFrameSize(offset: -3), height: getFrameSize(offset: -3))
            
            Circle()
                .fill(Color.ui.grey70)
                .frame(width: getFrameSize(offset: -4), height: getFrameSize(offset: -4))
                .shadow(color: Color.ui.grey50, radius: 1)

            if showRotate {
                Image("Image-rotate")
                    .resizable()
                    .scaledToFit()
                    .rotationEffect(Angle(degrees: rotate ? 360 : 0))
                    .frame(width: config.radius * 0.75, height: config.radius * 0.75)
                    .foregroundColor(Color.ui.grey80)
                    .onAppear {
                        withAnimation(.easeInOut(duration: 1.0).delay(1.0).repeatForever(autoreverses: false)) {
                            self.rotate = true
                        }
                    }
            }
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
        
        let valueNew = Int(anglePrev / 360 * 60)
        if valueNew == valuePrev {
            return
        }
        let valueDiff = valueNew - valuePrev
        valuePrev = valueNew
        let diff = Int(Double(valueDiff) * abs(Double(valueDiff)).squareRoot())
        if showRotate && diff > 0 {
            showRotate = false
        }
        onChange(diff)
    }
    
    private func getDegree(x: CGFloat, y: CGFloat) -> CGFloat {
        let a = atan2(y - config.radius, x - config.radius)
        return a * 180 / .pi
    }
    
    private func getFrameSize(offset: Double) -> CGFloat {
        (config.radius - config.size + CGFloat(offset)) * 2
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
        })
    }
}
