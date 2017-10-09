import React, { Component } from 'react';
import { debounce } from 'lodash';

export class WindowResizeListener extends Component {
  shouldComponentUpdate(nextProps) {
    return nextProps.onResize !== this.props.onResize;
  }

  componentDidMount() {
    // Defer creating _debouncedResize until it's mounted
    // This allows users to change DEBOUNCE_TIME if they want
    // If there's no listeners, we need to attach the window listener
    if (!WindowResizeListener._listeners.length) {
      WindowResizeListener._debouncedResize = debounce(
        WindowResizeListener._onResize,
        WindowResizeListener.DEBOUNCE_TIME
      );
      window.addEventListener(
        'resize',
        WindowResizeListener._debouncedResize,
        false
      );
    }
    WindowResizeListener._listeners.push(this.props.onResize);
    WindowResizeListener._debouncedResize();
  }

  componentWillUnmount() {
    const idx = WindowResizeListener._listeners.indexOf(this.props.onResize);
    WindowResizeListener._listeners.splice(idx, 1);
    if (!WindowResizeListener._listeners.length) {
      window.removeEventListener(
        'resize',
        WindowResizeListener._debouncedResize,
        false
      );
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.onResize !== this.props.onResize) {
      const idx = WindowResizeListener._listeners.indexOf(this.props.onResize);
      WindowResizeListener._listeners.splice(idx, 1, nextProps.onResize);
    }
  }

  render() {
    return null;
  }
}

WindowResizeListener.DEBOUNCE_TIME = 100;
WindowResizeListener._listeners = [];
WindowResizeListener._onResize = function _onResize() {
  const windowWidth =
    window.innerWidth ||
    document.documentElement.clientWidth ||
    document.body.clientWidth;
  const windowHeight =
    window.innerHeight ||
    document.documentElement.clientHeight ||
    document.body.clientHeight;

  WindowResizeListener._listeners.forEach(listener =>
    listener({
      windowWidth: windowWidth,
      windowHeight: windowHeight,
    })
  );
};

export default class FluidContainer extends Component {
  constructor(props) {
    super(props);
    this.state = {
      childOffset: null,
    };
    this.updateOffSet = this.updateOffSet.bind(this);
  }

  componentDidMount() {
    this.baseSize = parseFloat(getComputedStyle(document.body).fontSize) || 1;
    this.updateOffSet();
  }

  componentDidUpdate(prevProps) {
    if (prevProps.children !== this.props.children) {
      this.updateOffSet();
    }
  }

  updateOffSet() {
    if (this.child) {
      let paddingParent = 0;
      if (this.child.offsetParent !== null) {
        const parentStyle = getComputedStyle(this.child.offsetParent, null);
        paddingParent =
          parseFloat(parentStyle.getPropertyValue('padding-top')) -
          parseFloat(parentStyle.getPropertyValue('padding-bottom'));
      }
      const newOffset = this.child.offsetTop - paddingParent;
      if (newOffset !== this.state.childOffset) {
        this.setState({ childOffset: newOffset });
      }
    }
  }

  render() {
    const style = {};
    if (this.state.childOffset !== null) {
      style.position = 'relative';
      style.height = `calc(100% - ${this.state.childOffset / this.baseSize}em`;
    }
    return (
      <div ref={base => (this.child = base)} style={style}>
        <WindowResizeListener onResize={this.updateOffSet} />
        {this.props.children}
      </div>
    );
  }
}
