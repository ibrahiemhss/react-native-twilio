//
//  TwilioEmitter.swift
//  react-native-twilio
//
//  Created by ibrahim on 18/01/2023.
//
import React
import Foundation
@objc(TwilioEmitter)
open class TwilioEmitter: RCTEventEmitter {
    var hasListeners = false
    public static var emitter: RCTEventEmitter!

    override init() {
        super.init()
        TwilioEmitter.emitter = self
    }

    open  override func supportedEvents() -> [String]! {
        return [
            "onFrameDimensionsChanged",
            "onCameraSwitched",
            "onVideoChanged",
            "onAudioChanged",
            "roomName",
            "roomSid",
            "participantName",
            "participantSid",
            "participant",
            "participants",
            "localParticipant",
            "track",
            "trackSid",
            "trackName",
            "enabled",
            "identity",
            "sid",
            "isLocalUser",
            "quality",
            "error",
            "endCall",
            "onRoomDidConnect",
            "onRoomReConnect",
            "onRoomDidFailToConnect",
            "onRoomDidDisconnect",
            "onRoomParticipantDidConnect",
            "onRoomParticipantReconnect",
            "onRoomParticipantDidDisconnect",
            "onDataTrackMessageReceived",
            "onParticipantAddedDataTrack",
            "onParticipantRemovedDataTrack",
            "onParticipantAddedVideoTrack",
            "onParticipantRemovedVideoTrack",
            "onParticipantAddedAudioTrack",
            "onParticipantRemovedAudioTrack",
            "onParticipantEnabledVideoTrack",
            "onParticipantDisabledVideoTrack",
            "onParticipantEnabledAudioTrack",
            "onParticipantDisabledAudioTrack",
            "onStatsReceived",
            "onNetworkQualityLevelsChanged",
            "onDominantSpeakerDidChange",
            "onLocalParticipantSupportedCodecs",
            "onVideoEnabled",
            "onAudioEnabled",
        ]
    }

    open  override func startObserving() {
        hasListeners = true
    }

    open  override func stopObserving() {
        hasListeners = false
    }
}
