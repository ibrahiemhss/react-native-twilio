import * as React from 'react';

import { StyleSheet, View } from 'react-native';
import { TwillioView } from 'react-native-twillio';

export default function App() {
  return (
    <View style={styles.container}>
      <TwillioView roomName="name" accessToken="" trackSid="" color="#32a852" style={styles.box} />
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
    width: 400,
    height: 700,
  },
});
