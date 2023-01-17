import * as React from 'react';

import { StyleSheet, View, TouchableOpacity, Text } from 'react-native';
import TwilioView, { EventType, twilioEmitter } from 'react-native-twilio';

export default function App() {
  const token =
    'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImN0eSI6InR3aWxpby1mcGE7dj0xIn0.eyJqdGkiOiJTSzdkNGI0NWZmYzU0OWQ2MjQ3ZmI1OGMwNmM3ZTdiMmU3LTE2NzMzNzg1NjMiLCJpc3MiOiJTSzdkNGI0NWZmYzU0OWQ2MjQ3ZmI1OGMwNmM3ZTdiMmU3Iiwic3ViIjoiQUNjNzc1OTc1ZTA3MDlkNTQ3OGFiN2Q2OTY2YjA0ODZkOCIsImV4cCI6MTY3MzM4MjE2MywiZ3JhbnRzIjp7ImlkZW50aXR5IjoidXNlcjQiLCJ2aWRlbyI6eyJyb29tIjoibXJvb20ifX19.Y2zHAHWO3AaIzaIvnn_wct50-muHNdgqG15IWorNm48';
  TwilioView.initialize(token);
  React.useEffect(() => {
    const subscriptions = [
      twilioEmitter.addListener(EventType.ON_VIDEO_ENABLED, (data) => {
        console.log('ON_VIDEO_ENABLED');
        console.log(data);
      }),
      twilioEmitter.addListener(EventType.ON_MUTE, (data) => {
        console.log('ON_MUTE');
        console.log(data);
      }),
      twilioEmitter.addListener(EventType.ON_CONNECTED, (data) => {
        console.log('ON_CONNECTED');
        console.log(data);
      }),
      twilioEmitter.addListener(EventType.ON_RE_CONNECTED, (data) => {
        console.log('ON_RE_CONNECTED');
        console.log(data);
      }),
      twilioEmitter.addListener(EventType.ON_DISCONNECTED, (data) => {
        console.log('ON_DISCONNECTED');
        console.log(data);
      }),
      twilioEmitter.addListener(EventType.ON_CONNECT_FAILURE, (data) => {
        console.log('ON_CONNECT_FAILURE');
        console.log(data);
      }),

      twilioEmitter.addListener(
        EventType.ON_FRAME_DIMENSIONS_CHANGED,
        (data) => {
          console.log('ON_FRAME_DIMENSIONS_CHANGED');
          console.log(data);
        }
      ),
      twilioEmitter.addListener(EventType.ON_CAMERA_SWITCHED, (data) => {
        console.log('ON_CAMERA_SWITCHED');
        console.log(data);
      }),
      twilioEmitter.addListener(EventType.ON_VIDEO_CHANGED, (data) => {
        console.log('ON_VIDEO_CHANGED');
        console.log(data);
      }),
      twilioEmitter.addListener(EventType.ON_AUDIO_CHANGED, (data) => {
        console.log('ON_AUDIO_CHANGED');
        console.log(data);
      }),

      ////
      twilioEmitter.addListener(EventType.ON_PARTICIPANT_CONNECTED, (data) => {
        console.log('ON_PARTICIPANT_CONNECTED');
        console.log(data);
      }),
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_RECONNECTED,
        (data) => {
          console.log('ON_PARTICIPANT_RECONNECTED');
          console.log(data);
        }
      ),
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_DISCONNECTED,
        (data) => {
          console.log('ON_PARTICIPANT_DISCONNECTED');
          console.log(data);
        }
      ),
      twilioEmitter.addListener(
        EventType.ON_DATATRACK_MESSAGE_RECEIVED,
        (data) => {
          console.log('ON_DATATRACK_MESSAGE_RECEIVED');
          console.log(data);
        }
      ), ///
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_ADDED_DATA_TRACK,
        (data) => {
          console.log('ON_PARTICIPANT_ADDED_DATA_TRACK');
          console.log(data);
        }
      ),
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_REMOVED_DATA_TRACK,
        (data) => {
          console.log('ON_PARTICIPANT_REMOVED_DATA_TRACK');
          console.log(data);
        }
      ),
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_ADDED_VIDEO_TRACK,
        (data) => {
          console.log('ON_PARTICIPANT_ADDED_VIDEO_TRACK');
          console.log(data);
        }
      ),
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_REMOVED_VIDEO_TRACK,
        (data) => {
          console.log('ON_PARTICIPANT_REMOVED_VIDEO_TRACK');
          console.log(data);
        }
      ),

      ////
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_ADDED_AUDIO_TRACK,
        (data) => {
          console.log('ON_PARTICIPANT_ADDED_AUDIO_TRACK');
          console.log(data);
        }
      ),
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_REMOVED_AUDIO_TRACK,
        (data) => {
          console.log('ON_PARTICIPANT_REMOVED_AUDIO_TRACK');
          console.log(data);
        }
      ),
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_ENABLED_VIDEO_TRACK,
        (data) => {
          console.log('ON_PARTICIPANT_ENABLED_VIDEO_TRACK');
          console.log(data);
        }
      ),
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_DISABLED_VIDEO_TRACK,
        (data) => {
          console.log('ON_PARTICIPANT_DISABLED_VIDEO_TRACK');
          console.log(data);
        }
      ), ///
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_ENABLED_AUDIO_TRACK,
        (data) => {
          console.log('ON_PARTICIPANT_ENABLED_AUDIO_TRACK');
          console.log(data);
        }
      ),
      twilioEmitter.addListener(
        EventType.ON_PARTICIPANT_DISABLED_AUDIO_TRACK,
        (data) => {
          console.log('ON_PARTICIPANT_DISABLED_AUDIO_TRACK');
          console.log(data);
        }
      ),
      twilioEmitter.addListener(EventType.ON_STATS_RECEIVED, (data) => {
        console.log('ON_STATS_RECEIVED');
        console.log(data);
      }),
      twilioEmitter.addListener(
        EventType.ON_NETWORK_QUALITY_LEVELS_CHANGED,
        (data) => {
          console.log('ON_NETWORK_QUALITY_LEVELS_CHANGED');
          console.log(data);
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
        src={{ roomName: 'mroom' }}
        //trackSid={null}
        style={styles.box}
      />
      <View style={styles.containerBtns}>
        <TouchableOpacity style={styles.button} onPress={()=>{TwilioView.mute()}}>
          <Text style={styles.text}>mute</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={()=>{TwilioView.flipCamera()}}>
          <Text style={styles.text}>switch camera</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={()=>{TwilioView.closeCamera()}}>
          <Text style={styles.text}>lock camera</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={()=>{TwilioView.endCall()}}>
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
