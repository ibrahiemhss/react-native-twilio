import * as React from 'react';

import { StyleSheet, View } from 'react-native';
import { TwilioView } from 'react-native-twilio';

export default function App() {
  const accessToken =
    'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImN0eSI6InR3aWxpby1mcGE7dj0xIn0.eyJqdGkiOiJTS2M5MmRiYzYzNTliYjk0NzU4ZDdkZmUyNTNiYThkNjhmLTE2NzA1MTc2MzgiLCJpc3MiOiJTS2M5MmRiYzYzNTliYjk0NzU4ZDdkZmUyNTNiYThkNjhmIiwic3ViIjoiQUNhYzUzNWZlOTczMmYwNTVhOWJiOTY4N2U4OTdkYjk1ZiIsImV4cCI6MTY3MDUyMTIzOCwiZ3JhbnRzIjp7ImlkZW50aXR5IjoidXNlcl9hNSIsInZpZGVvIjp7InJvb20iOiJnb29kX3Jvb20ifX19.rt4z1-gfTBAAtzIGX-h_ZXPrT1BwSpfkw82MGhAKY10';

  return (
    <View style={styles.container}>
      <TwilioView
        roomName="name"
        accessToken={accessToken}
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
    width: "100%",
    height:"100%",
    marginVertical: 20,
  },
});
