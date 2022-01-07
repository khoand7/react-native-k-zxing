/**
 * Sample React Native App
 *
 * adapted from App.js generated by the following command:
 *
 * react-native init example
 *
 * https://github.com/facebook/react-native
 */

import React, {Component} from 'react';
import {Text, View, PixelRatio} from 'react-native';
import KBarCodeView from 'react-native-k-zxing';

export default class App extends Component {
  constructor() {
    super();
    this.state = {
      barCode: 'Test',
    };
    this._onReceiveBarCode = this._onReceiveBarCode.bind(this);
  }
  _onReceiveBarCode(barCode) {
    console.log(barCode);
    this.setState({barCode: barCode});
  }
  render() {
    return (
      <View>
        <KBarCodeView
          style={{
            // converts dpi to px, provide desired height
            height: PixelRatio.getPixelSizeForLayoutSize(400),
            // converts dpi to px, provide desired width
            width: PixelRatio.getPixelSizeForLayoutSize(400),
          }}
          onReceiveBarCode={this._onReceiveBarCode}
        />
        <Text>{this.state.barCode}</Text>
      </View>
    );
  }
}
