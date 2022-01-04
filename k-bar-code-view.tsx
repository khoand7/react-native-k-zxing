import React, { useEffect, useRef } from 'react';
import { UIManager, findNodeHandle, PixelRatio } from 'react-native';
import PropTypes from 'prop-types';

import { KBarCodeViewManager } from './k-bar-code-view-manager';

const createFragment = (viewId) =>
  UIManager.dispatchViewManagerCommand(
    viewId,
    // we are calling the 'create' command
    UIManager.KBarCodeViewManager.Commands.create.toString(),
    [viewId]
  );

export const KBarCodeView = (props) => {
  const ref = useRef(null);

  useEffect(() => {
    const viewId = findNodeHandle(ref.current);
    createFragment(viewId);
  }, []);

  function _onChange(event) {
    if (!props.onReceiveBarCode) {
      return;
    }
    props.onReceiveBarCode(event.nativeEvent.barCode);
  }

  return (
    <KBarCodeViewManager
      style={{
        // converts dpi to px, provide desired height
        height: PixelRatio.getPixelSizeForLayoutSize(200),
        // converts dpi to px, provide desired width
        width: PixelRatio.getPixelSizeForLayoutSize(200)
      }}
      ref={ref}
      onChange={_onChange}
    />
  );
};

KBarCodeView.propTypes = {
  onReceiveBarCode: PropTypes.func,
}

// export default KBarCodeView;