import React from 'react';
import PropTypes from 'prop-types';
import { ActivityIndicator, StyleSheet, Text } from 'react-native'
import { View } from 'react-native-animatable'

import TouchableView from './TouchableView'

const CustomButton = ({ onPress, icon, isEnabled, isLoading, text, isNew, buttonStyle, textStyle, ...otherProps }) => {
  const onButtonPress = isEnabled && !isLoading ? onPress : () => null

  return (
    <View {...otherProps}>
      <TouchableView onPress={onButtonPress} style={[styles.button, buttonStyle]}>
        {icon ? icon : null}
        {(isLoading) && <ActivityIndicator style={styles.spinner} color={'grey'} />}
        {(!isLoading) && <Text style={[styles.text, textStyle]}>{text}</Text>}
        {(!isLoading) && isNew && <Text style={[styles.text, styles.newText]}>NEW</Text>}
      </TouchableView>
    </View>
  )
}

CustomButton.propTypes = {
  onPress: PropTypes.func,
  isEnabled: PropTypes.bool,
  isLoading: PropTypes.bool,
  text: PropTypes.string,
  buttonStyle: PropTypes.any,
  textStyle: PropTypes.any
}

CustomButton.defaultProps = {
  onPress: () => null,
  isEnabled: true,
  isLoading: false
}

const styles = StyleSheet.create({
  button: {
    height: 42,
    borderWidth: 1,
    borderRadius: 3,
    alignSelf: 'stretch',
    justifyContent: 'center',
    alignItems: 'center',
    borderColor: 'rgba(0, 0, 0, 0.1)',
    flexDirection: 'row',
  },
  spinner: {
    height: 26
  },
  text: {
    textAlign: 'center',
    fontWeight: '400',
    color: 'white'
  },
  newText: {
    fontWeight: '600',
    color: 'red'
  }
})

export default CustomButton
