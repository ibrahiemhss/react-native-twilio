import * as React from 'react';

import {
  StyleSheet,
  View,
  TouchableOpacity,
  Text,
  Platform,
} from 'react-native';
import TwilioView, { EventType, twilioEmitter } from 'react-native-twilio';

export default function App() {
  const imgUri =
    'https://develop.watchbeem.com/profile_avatars/c5f9c093-3166-47e9-b83e-84f6d72c7151/avatar.jpg?1669199370556.498';

  const token1 =
    'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImN0eSI6InR3aWxpby1mcGE7dj0xIn0.eyJqdGkiOiJTSzdkNGI0NWZmYzU0OWQ2MjQ3ZmI1OGMwNmM3ZTdiMmU3LTE2NzQ1MTEyMzgiLCJpc3MiOiJTSzdkNGI0NWZmYzU0OWQ2MjQ3ZmI1OGMwNmM3ZTdiMmU3Iiwic3ViIjoiQUNjNzc1OTc1ZTA3MDlkNTQ3OGFiN2Q2OTY2YjA0ODZkOCIsImV4cCI6MTY3NDUxNDgzOCwiZ3JhbnRzIjp7ImlkZW50aXR5IjoidXNlcjkiLCJ2aWRlbyI6eyJyb29tIjoicm9vbTEifX19.gO_eU-hRglY5FPbRIKjY2oKL550KNCrSWxe8RAz-ZMU';
  const token2 =
    'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImN0eSI6InR3aWxpby1mcGE7dj0xIn0.eyJqdGkiOiJTSzdkNGI0NWZmYzU0OWQ2MjQ3ZmI1OGMwNmM3ZTdiMmU3LTE2NzQ1MTEyNTUiLCJpc3MiOiJTSzdkNGI0NWZmYzU0OWQ2MjQ3ZmI1OGMwNmM3ZTdiMmU3Iiwic3ViIjoiQUNjNzc1OTc1ZTA3MDlkNTQ3OGFiN2Q2OTY2YjA0ODZkOCIsImV4cCI6MTY3NDUxNDg1NSwiZ3JhbnRzIjp7ImlkZW50aXR5IjoidXNlcjEwIiwidmlkZW8iOnsicm9vbSI6InJvb20xIn19fQ.4XtlfzMthV1RgJZDoM3fzahIm2dPV1RdT12EPsMQatw';
  TwilioView.initialize(token1);
  React.useEffect(() => {
    const subscriptions = [
      twilioEmitter.addListener(EventType.ON_VIDEO_ENABLED, (data) => {
        console.log(
          `ON_VIDEO_ENABLED:  OS=${Platform.OS} data=${
            data === undefined ? 'NO EVENT' : JSON.stringify(data)
          }`
        );
      }),
      twilioEmitter.addListener(EventType.ON_MUTE, (data) => {
        console.log(
          `ON_MUTE: OS=${Platform.OS} data=${
            data === undefined ? 'NO EVENT' : JSON.stringify(data)
          }`
        );
      }),
      twilioEmitter.addListener(EventType.ON_CONNECTED, (data) => {
        console.log(
          `ON_CONNECTED: OS=${Platform.OS} data=${
            data === undefined ? 'NO EVENT' : JSON.stringify(data)
          }`
        );
      }),
      twilioEmitter.addListener(EventType.ON_RE_CONNECTED, (data) => {
        console.log(
          `ON_RE_CONNECTED: OS=${Platform.OS} data=${
            data === undefined ? 'NO EVENT' : JSON.stringify(data)
          }`
        );
      }),
      twilioEmitter.addListener(EventType.ON_DISCONNECTED, (data) => {
        console.log(
          `ON_DISCONNECTED: OS=${Platform.OS} data=${
            data === undefined ? 'NO EVENT' : JSON.stringify(data)
          }`
        );
      }),
      twilioEmitter.addListener(EventType.ON_CONNECT_FAILURE, (data) => {
        console.log(
          `ON_CONNECT_FAILURE: OS=${Platform.OS} data=${
            data === undefined ? 'NO EVENT' : JSON.stringify(data)
          }`
        );
      }),

      twilioEmitter.addListener(
        EventType.ON_FRAME_DIMENSIONS_CHANGED,
        (data) => {
          console.log(
            `ON_FRAME_DIMENSIONS_CHANGED: OS=${Platform.OS} data=${data}`
          );
        }
      ),
      twilioEmitter.addListener(EventType.ON_CAMERA_SWITCHED, (data) => {
        console.log(
          `ON_CAMERA_SWITCHED: OS=${Platform.OS} data=${
            data === undefined ? 'NO EVENT' : JSON.stringify(data)
          }`
        );
      }),
      twilioEmitter.addListener(EventType.ON_VIDEO_CHANGED, (data) => {
        console.log(
          `ON_VIDEO_CHANGED: OS=${Platform.OS} data=${
            data === undefined ? 'NO EVENT' : JSON.stringify(data)
          }`
        );
      }),
      twilioEmitter.addListener(EventType.ON_AUDIO_CHANGED, (data) => {
        console.log(
          `ON_AUDIO_CHANGED: OS=${Platform.OS} data=${
            data === undefined ? 'NO EVENT' : JSON.stringify(data)
          }`
        );
      }),

      ////
      twilioEmitter.addListener(EventType.ON_PARTICIPANT_CONNECTED, (data) => {
        console.log(
          `ON_PARTICIPANT_CONNECTED: OS=${Platform.OS} data=${
            data === undefined ? 'NO EVENT' : JSON.stringify(data)
          }`
        );
      }),
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_RECONNECTED,
        (data) => {
          console.log(
            `ON_PARTICIPANT_RECONNECTED: OS=${Platform.OS} data=${
              data === undefined ? 'NO EVENT' : JSON.stringify(data)
            }`
          );
        }
      ),
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_DISCONNECTED,
        (data) => {
          console.log(
            `ON_PARTICIPANT_DISCONNECTED: OS=${Platform.OS}data=${
              data === undefined ? 'NO EVENT' : JSON.stringify(data)
            }`
          );
        }
      ),
      twilioEmitter.addListener(
        EventType.ON_DATATRACK_MESSAGE_RECEIVED,
        (data) => {
          console.log(
            `ON_DATATRACK_MESSAGE_RECEIVED: OS=${Platform.OS} data=${
              data === undefined ? 'NO EVENT' : JSON.stringify(data)
            }`
          );
        }
      ), ///
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_ADDED_DATA_TRACK,
        (data) => {
          console.log(
            `ON_PARTICIPANT_ADDED_DATA_TRACK: OS=${Platform.OS} data=${
              data === undefined ? 'NO EVENT' : JSON.stringify(data)
            }`
          );
        }
      ),
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_REMOVED_DATA_TRACK,
        (data) => {
          console.log(
            `ON_PARTICIPANT_REMOVED_DATA_TRACK: OS=${Platform.OS} data=${
              data === undefined ? 'NO EVENT' : JSON.stringify(data)
            }`
          );
        }
      ),
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_ADDED_VIDEO_TRACK,
        (data) => {
          console.log(
            `ON_PARTICIPANT_ADDED_VIDEO_TRACK: OS=${Platform.OS} data=${
              data === undefined ? 'NO EVENT' : JSON.stringify(data)
            }`
          );
        }
      ),
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_REMOVED_VIDEO_TRACK,
        (data) => {
          console.log(
            `ON_PARTICIPANT_REMOVED_VIDEO_TRACK: OS=${Platform.OS} data=${
              data === undefined ? 'NO EVENT' : JSON.stringify(data)
            }`
          );
        }
      ),

      ////
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_ADDED_AUDIO_TRACK,
        (data) => {
          console.log(
            `ON_PARTICIPANT_ADDED_AUDIO_TRACK: OS=${Platform.OS} data=${
              data === undefined ? 'NO EVENT' : JSON.stringify(data)
            }`
          );
        }
      ),
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_REMOVED_AUDIO_TRACK,
        (data) => {
          console.log(
            `ON_PARTICIPANT_REMOVED_AUDIO_TRACK: OS=${Platform.OS} data=${
              data === undefined ? 'NO EVENT' : JSON.stringify(data)
            }`
          );
        }
      ),
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_ENABLED_VIDEO_TRACK,
        (data) => {
          console.log(
            `ON_PARTICIPANT_ENABLED_VIDEO_TRACK: OS=${Platform.OS} data=${
              data === undefined ? 'NO EVENT' : JSON.stringify(data)
            }`
          );
        }
      ),
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_DISABLED_VIDEO_TRACK,
        (data) => {
          console.log(
            `ON_PARTICIPANT_DISABLED_VIDEO_TRACK: OS=${Platform.OS} data=${
              data === undefined ? 'NO EVENT' : JSON.stringify(data)
            }`
          );
        }
      ), ///
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_ENABLED_AUDIO_TRACK,
        (data) => {
          console.log(
            `ON_PARTICIPANT_ENABLED_AUDIO_TRACK: OS=${Platform.OS} data=${
              data === undefined ? 'NO EVENT' : JSON.stringify(data)
            }`
          );
        }
      ),
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_DISABLED_AUDIO_TRACK,
        (data) => {
          console.log(
            `ON_PARTICIPANT_DISABLED_AUDIO_TRACK: OS=${Platform.OS} data=${
              data === undefined ? 'NO EVENT' : JSON.stringify(data)
            }`
          );
        }
      ),
      twilioEmitter.addListener(EventType.ON_STATS_RECEIVED, (data) => {
        console.log(
          `ON_STATS_RECEIVED: OS=${Platform.OS} data=${
            data === undefined ? 'NO EVENT' : JSON.stringify(data)
          }`
        );
      }),
      twilioEmitter.addListener(
        EventType.ON_NETWORK_QUALITY_LEVELS_CHANGED,
        (data) => {
          console.log(
            `ON_NETWORK_QUALITY_LEVELS_CHANGED: OS=${Platform.OS} data=${
              data === undefined ? 'NO EVENT' : JSON.stringify(data)
            }`
          );
        }
      ),
    ];

    return () => {
      subscriptions.map((subscription) => {
        subscription.remove();
      });
    };
  }, []);

  return (
    <View style={styles.container}>
      <TwilioView
        src={{
          token: Platform.OS === 'ios' ? token2 : token1,
          roomName: 'room1',
          imgUriPlaceHolder: imgUri,
          textPlaceHolder: 'No Preview',
        }}
        //trackSid={null}
        style={styles.box}
      />
      <View style={styles.containerBtns}>
        <TouchableOpacity
          style={styles.button}
          onPress={() => {
            TwilioView.mute();
          }}
        >
          <Text style={styles.text}>mute</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.button}
          onPress={() => {
            TwilioView.flipCamera();
          }}
        >
          <Text style={styles.text}>switch camera</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.button}
          onPress={() => {
            TwilioView.closeCamera();
          }}
        >
          <Text style={styles.text}>lock camera</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.button}
          onPress={() => {
            TwilioView.endCall();
          }}
        >
          <Text style={styles.text}>end call</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: '100%',
    height: '100%',
    marginVertical: 20,
  },
  containerBtns: {
    position: 'absolute',
    bottom: 5,
    justifyContent: 'space-evenly',
    flexDirection: 'row',
  },
  button: {
    justifyContent: 'center',
    textAlign: 'center',
    width: 70,
    height: 'auto',
    backgroundColor: 'green',
    borderRadius: 10,
    margin: 20,
  },
  text: {
    color: 'white',
    width: 50,
    textAlign: 'center',
    margin: 10,
  },
});
