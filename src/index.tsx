import { UIManager, requireNativeComponent, Platform, View, findNodeHandle,
} from "react-native";
import PropTypes from "prop-types";
import React, {Component, createRef} from "react";


const LINKING_ERROR =
  `The package 'react-native-twillio' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const ComponentName = 'TwillioView';
const isConnectedRef = createRef();

const propTypes = {
  ...View.prototype,
  roomName: PropTypes.string,
  accessToken: PropTypes.string,
  trackSid: PropTypes.string,
  onParticipantAddedDataTrack: PropTypes.func,
}
const nativeEvents = {
  connectToRoom: 1,
  disconnect: 2,
  switchCamera: 3,
  toggleVideo: 4,
  toggleSound: 5,
  getStats: 6,
  disableOpenSLES: 7,
  toggleSoundSetup: 8,
  toggleRemoteSound: 9,
  releaseResource: 10,
  toggleBluetoothHeadset: 11,
  sendString: 12,
  publishVideo: 13,
  publishAudio: 14
}

CustomTwilioVideoView.propTypes = propTypes

class CustomTwilioVideoView extends Component {
  connect ({
             roomName,
             accessToken,
             cameraType = 'front',
             enableAudio = true,
             enableVideo = true,
             enableRemoteAudio = true,
             enableNetworkQualityReporting = false,
             dominantSpeakerEnabled = false,
             maintainVideoTrackInBackground = false,
             encodingParameters = {}
           }:any) {
    if(isConnectedRef.current) return;
    isConnectedRef.current = true;
    this.runCommand(nativeEvents.connectToRoom, [
      roomName,
      accessToken,
      enableAudio,
      enableVideo,
      enableRemoteAudio,
      enableNetworkQualityReporting,
      dominantSpeakerEnabled,
      maintainVideoTrackInBackground,
      cameraType,
      encodingParameters
    ])
  }
  buildNativeEventWrappers () {
    return [
      'onParticipantAddedDataTrack',
    ].reduce((wrappedEvents, eventName) => {
      if (!this.props[eventName]) {
        return wrappedEvents
      } else {
        return {
          ...wrappedEvents,
          [eventName]: data => this.props[eventName](data.nativeEvent)
        }
      }
    }, {})
  }

  sendString (message:any) {
    this.runCommand(nativeEvents.sendString, [
      message
    ])
  }

  publishLocalAudio () {
    this.runCommand(nativeEvents.publishAudio, [true])
  }

  publishLocalVideo () {
    this.runCommand(nativeEvents.publishVideo, [true])
  }

  unpublishLocalAudio () {
    this.runCommand(nativeEvents.publishAudio, [false])
  }

  unpublishLocalVideo () {
    this.runCommand(nativeEvents.publishVideo, [false])
  }

  disconnect () {
    isConnectedRef.current = false;
    this.runCommand(nativeEvents.disconnect, [])
  }

  componentWillUnmount () {
    this.runCommand(nativeEvents.releaseResource, [])
  }

  flipCamera () {
    this.runCommand(nativeEvents.switchCamera, [])
  }

  setLocalVideoEnabled (enabled:any) {
    this.runCommand(nativeEvents.toggleVideo, [enabled])
    return Promise.resolve(enabled)
  }

  setLocalAudioEnabled (enabled:any) {
    this.runCommand(nativeEvents.toggleSound, [enabled])
    return Promise.resolve(enabled)
  }

  setRemoteAudioEnabled (enabled:any) {
    this.runCommand(nativeEvents.toggleRemoteSound, [enabled])
    return Promise.resolve(enabled)
  }

  setBluetoothHeadsetConnected (enabled:any) {
    this.runCommand(nativeEvents.toggleBluetoothHeadset, [enabled])
    return Promise.resolve(enabled)
  }

  getStats () {
    this.runCommand(nativeEvents.getStats, [])
  }

  disableOpenSLES () {
    this.runCommand(nativeEvents.disableOpenSLES, [])
  }

  toggleSoundSetup (speaker:any) {
    this.runCommand(nativeEvents.toggleSoundSetup, [speaker])
  }
  runCommand (event:any, args:any) {
    switch (Platform.OS) {
      case 'android':
        UIManager.dispatchViewManagerCommand(
          findNodeHandle(this.refs.videoView),
          event,
          args
        )
        break
      default:
        break
    }
  }
  render () {
    return (<NativeCustomTwilioVideoView ref='videoView' {...this.props} {...this.buildNativeEventWrappers()}
      />
    )
  }
}


const NativeCustomTwilioVideoView = requireNativeComponent(
  ComponentName,
  CustomTwilioVideoView
)

export const TwillioView =
  UIManager.getViewManagerConfig(ComponentName) != null
    ?NativeCustomTwilioVideoView
    : () => {
      throw new Error(LINKING_ERROR);
    };
