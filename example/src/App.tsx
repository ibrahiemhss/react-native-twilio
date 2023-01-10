import * as React from 'react';

import { StyleSheet, View } from 'react-native';
import TwilioView from 'react-native-twilio';

export default function App() {
  const token ="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImN0eSI6InR3aWxpby1mcGE7dj0xIn0.eyJqdGkiOiJTSzdkNGI0NWZmYzU0OWQ2MjQ3ZmI1OGMwNmM3ZTdiMmU3LTE2NzMzNjEzMDkiLCJpc3MiOiJTSzdkNGI0NWZmYzU0OWQ2MjQ3ZmI1OGMwNmM3ZTdiMmU3Iiwic3ViIjoiQUNjNzc1OTc1ZTA3MDlkNTQ3OGFiN2Q2OTY2YjA0ODZkOCIsImV4cCI6MTY3MzM2NDkwOSwiZ3JhbnRzIjp7ImlkZW50aXR5IjoidXNlcjIiLCJ2aWRlbyI6eyJyb29tIjoicm9vbSJ9fX0.fNBL748SxqrqC1hAIcTB0FiDTmsco0nnsZO0WfR0aBQ";

  TwilioView.initialize(token);
  return (
    <View style={styles.container}>
      <TwilioView
        src={{ roomName: 'room1' }}
        //trackSid={null}
        style={styles.box}
      />
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
});
