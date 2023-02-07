import {
  NativeEventEmitter,
  NativeModules,
  findNodeHandle,
  Platform,
  requireNativeComponent,
  UIManager,
  ViewStyle,
} from 'react-native';

import { forwardRef, useCallback, useImperativeHandle, useRef } from 'react';
import * as React from 'react';

const twilioEmitter =
  Platform.OS === 'ios'
    ? new NativeEventEmitter(NativeModules.TwilioEmitter)
    : new NativeEventEmitter(NativeModules.TwilioModule);

export { RNTwilio, twilioEmitter };

export enum EventType {
  ON_FRAME_DIMENSIONS_CHANGED = 'onFrameDimensionsChanged',
  ON_CAMERA_SWITCHED = 'onCameraSwitched',
  ON_VIDEO_CHANGED = 'onVideoChanged',
  ON_AUDIO_CHANGED = 'onAudioChanged',
  ROOM_NAME = 'roomName',
  ROOM_SID = 'roomSid',
  PARTICIPANT_NAME = 'participantName',
  PARTICIPANT_SID = 'participantSid',
  PARTICIPANT = 'participant',
  PARTICIPANTS = 'participants',
  LOCAL_PARTICIPANT = 'localParticipant',
  TRACK = 'track',
  TRACK_SID = 'trackSid',
  TRACK_NAME = 'trackName',
  ENABLED = 'enabled',
  IDENTITY = 'identity',
  SID = 'sid',
  IS_LOCAL_USER = 'isLocalUser',
  QUALITY = 'quality',
  ERROR = 'error',
  END_CALL = 'endCall',
  ON_CONNECTED = 'onRoomDidConnect',
  ON_RE_CONNECTED = 'onRoomReConnect',
  ON_CONNECT_FAILURE = 'onRoomDidFailToConnect',
  ON_DISCONNECTED = 'onRoomDidDisconnect',
  ON_PARTICIPANT_CONNECTED = 'onRoomParticipantDidConnect',
  ON_PARTICIPANT_RECONNECTED = 'onRoomParticipantReconnect',
  ON_PARTICIPANT_DISCONNECTED = 'onRoomParticipantDidDisconnect',
  ON_DATATRACK_MESSAGE_RECEIVED = 'onDataTrackMessageReceived',
  ON_PARTICIPANT_ADDED_DATA_TRACK = 'onParticipantAddedDataTrack',
  ON_PARTICIPANT_REMOVED_DATA_TRACK = 'onParticipantRemovedDataTrack',
  ON_PARTICIPANT_ADDED_VIDEO_TRACK = 'onParticipantAddedVideoTrack',
  ON_PARTICIPANT_REMOVED_VIDEO_TRACK = 'onParticipantRemovedVideoTrack',
  ON_PARTICIPANT_ADDED_AUDIO_TRACK = 'onParticipantAddedAudioTrack',
  ON_PARTICIPANT_REMOVED_AUDIO_TRACK = 'onParticipantRemovedAudioTrack',
  ON_PARTICIPANT_ENABLED_VIDEO_TRACK = 'onParticipantEnabledVideoTrack',
  ON_PARTICIPANT_DISABLED_VIDEO_TRACK = 'onParticipantDisabledVideoTrack',
  ON_PARTICIPANT_ENABLED_AUDIO_TRACK = 'onParticipantEnabledAudioTrack',
  ON_PARTICIPANT_DISABLED_AUDIO_TRACK = 'onParticipantDisabledAudioTrack',
  ON_STATS_RECEIVED = 'onStatsReceived',
  ON_NETWORK_QUALITY_LEVELS_CHANGED = 'onNetworkQualityLevelsChanged',
  ON_DOMINANT_SPEAKER_CHANGED = 'onDominantSpeakerDidChange',
  ON_LOCAL_PARTICIPANT_SUPPORTED_CODECS = 'onLocalParticipantSupportedCodecs',
  ON_VIDEO_ENABLED = 'onVideoEnabled',
  ON_MUTE = 'onAudioEnabled',
}

const LINKING_ERROR =
  `The package 'react-native-twillio' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

type TwilioProps = {
  style: ViewStyle;
};

const ComponentName = 'TwilioView';
const RNTwilio =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<TwilioProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };

const TwilioView = forwardRef((props: TwilioProps, ref: any): any => {
  const localRef = useRef<any>();
  const runCommandAndroid = useCallback((event: any, args: any) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(localRef.current),
      event,
      args
    );
  }, []);

  const removeTwilioListeners = useCallback(() => {
    twilioEmitter.removeAllListeners(EventType.ON_CONNECTED);
    twilioEmitter.removeAllListeners(EventType.ON_DISCONNECTED);
    twilioEmitter.removeAllListeners(EventType.ON_CONNECT_FAILURE);
    twilioEmitter.removeAllListeners(EventType.ON_RE_CONNECTED);
  }, []);

  const listenTwilio = useCallback(() => {
    removeTwilioListeners();

    const subscriptions = [
      twilioEmitter.addListener(
        EventType.ON_CONNECT_FAILURE,
        ({ error }) => {}
      ),
      twilioEmitter.addListener(EventType.ON_RE_CONNECTED, ({ name }) => {}),
      twilioEmitter.addListener(EventType.ON_CONNECTED, ({ name }) => {}),
      twilioEmitter.addListener(EventType.ON_DISCONNECTED, ({ name }) => {}),
    ];
    return () => {
      subscriptions.map((subscription) => {
        subscription.remove();
      });
    };
  }, []);

  useImperativeHandle(ref, () => ({
    initialize: () => {
      const subscribeRegisterAndroid = listenTwilio();
      return () => {
        subscribeRegisterAndroid();
      };
    },

    connect: (src: any) => {
      if (Platform.OS === 'ios') {
        NativeModules.TwilioViewManager.connect();
      } else {
        runCommandAndroid('connect', [src]);
      }
    },
    flipCamera: () => {
      if (Platform.OS === 'ios') {
        NativeModules.TwilioViewManager.switchCamera();
      } else {
        runCommandAndroid('switchCamera', []);
      }
    },
    mute: () => {
      if (Platform.OS === 'ios') {
        NativeModules.TwilioViewManager.mute();
      } else {
        runCommandAndroid('mute', []);
      }
    },
    closeCamera: () => {
      if (Platform.OS === 'ios') {
        NativeModules.TwilioViewManager.closeCamera();
      } else {
        runCommandAndroid('closeCamera', []);
      }
    },
    endCall: () => {
      if (Platform.OS === 'ios') {
        NativeModules.TwilioViewManager.endCall();
      } else {
        runCommandAndroid('endCall', []);
      }
    },
  }));

  return <RNTwilio ref={localRef} {...props} />;
});
TwilioView.defaultProps = {
  /*  src: {
    roomName: '',
    token: '',
    textPlaceHolder: 'No Preview',
    imgUriPlaceHolder: 'https://picsum.photos/200/300.jpg',
    localTextPlaceHolder: 'No Preview',
  },*/
};
export default TwilioView;
