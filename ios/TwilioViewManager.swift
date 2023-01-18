import UIKit
import Foundation
import React
import AVFoundation
import TwilioVideo
import AVFoundation

@objc(TwilioViewManager)
class TwilioViewManager: RCTViewManager {

    override func view() -> (TwilioView) {
        return TwilioView()
    }
    
    @objc override static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    func methodQueue() -> DispatchQueue {
        return bridge.uiManager.methodQueue
    }
    
    @objc
       func switchCamera() {
           view().switchCamera()
       }
    
    @objc
       func mute() {
           view().mute()

       }
    
    @objc
       func closeCamera() {
           view().closeCamera()

       }
    
    @objc
       func endCall() {
           view().endCall()

       }
    
}

class TwilioView : UIView {
    let rootController = TwilioViewController();
    var _src = NSDictionary()
    var rect = CGRectMake(0, 0, 100, 100)
    var previewView:VideoView = VideoView.init()

    override public init(frame: CGRect) {
        super.init(frame: frame)
        addSubview(rootController.view);
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    
    
    @objc var initialize: NSDictionary = [:]  {
        didSet {
            _src=src
        }
    }
    
    
    @objc var src: NSDictionary = [:]  {
        didSet {
            _src=src
        }
    }

    public func switchCamera() {
        rootController.switchCamera()
    
     }
  

    func mute() {
        rootController.mute()
     }
  
  
    func closeCamera() {
        rootController.closeCamera()

     }

    func endCall() {
        rootController.endCall()

     }
    
    
    func videoView(_ videoView: VideoView, didChangeVideoSize size: CGSize) {
        if (self.previewView == videoView) {
            //self.videoSize = size
        }
        self.setNeedsLayout()
    }
    
    override func layoutSubviews() {
    

        rect = CGRect(x: 0, y:0, width: frame.width, height: frame.height)
        rootController.setDataSrc(data: _src,rect: rect,videoView: previewView)
    
    }
    
}


