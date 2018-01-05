import React from 'react';
import { mount } from 'enzyme';
import FluidContainer, { WindowResizeListener } from '../fluid-container.jsx';

function fillInOffsetParent(instance) {
  Object.defineProperty(instance.child, 'offsetParent', {
    writable: true,
    configurable: true,
  });
  const div = document.createElement('div');
  div.style.paddingTop = '10px';
  div.style.paddingBottom = '20px';
  instance.child.offsetParent = div;
}

describe('FluidContainer component', () => {
  test('should initially calculate the childOffset', () => {
    const component = mount(<FluidContainer />);
    expect(component.state('childOffset')).toBe(0);
  });
  test('should update the childOffset on didMount', () => {
    const component = mount(<FluidContainer />);
    fillInOffsetParent(component.instance());
    expect(component.state('childOffset')).toBe(0);
    component.instance().componentDidMount();
    expect(component.state('childOffset')).toBe(10);
  });
  test('should update the childOffset on children change', () => {
    const component = mount(<FluidContainer />);
    fillInOffsetParent(component.instance());
    expect(component.state('childOffset')).toBe(0);
    component.setProps({ children: 'test' });
    expect(component.state('childOffset')).toBe(10);
  });
});

function simulateResizeEvent(windowWidth, windowHeight) {
  window.innerWidth = windowWidth;
  window.innerHeight = windowHeight;
  // Simulate window resize event
  const resizeEvent = document.createEvent('Event');
  resizeEvent.initEvent('resize', true, true);
  window.dispatchEvent(resizeEvent);
}

describe('WindowResizeListener', () => {
  beforeAll(() => {
    jest.useFakeTimers();
  });

  afterAll(() => {
    jest.useRealTimers();
  });

  beforeEach(() => {
    WindowResizeListener._listeners = [];
  });

  test('should add a listener at mounting', () => {
    const spy = jest.fn();
    expect(WindowResizeListener._listeners).toHaveLength(0);
    mount(<WindowResizeListener onResize={spy} />);
    expect(spy).not.toHaveBeenCalled();
    expect(WindowResizeListener._listeners).toHaveLength(1);
  });
  test('should remove the listener at unmounting', () => {
    const spy = jest.fn();
    const component = mount(<WindowResizeListener onResize={spy} />);
    expect(spy).not.toHaveBeenCalled();
    expect(WindowResizeListener._listeners).toHaveLength(1);
    component.unmount();
    expect(WindowResizeListener._listeners).toHaveLength(0);
  });
  test('should switch the listener', () => {
    const spy = jest.fn();
    const component = mount(<WindowResizeListener onResize={spy} />);
    expect(spy).not.toHaveBeenCalled();
    expect(WindowResizeListener._listeners.indexOf(spy)).toBe(0);
    const newSpy = jest.fn();
    component.setProps({ onResize: newSpy });
    expect(WindowResizeListener._listeners.indexOf(newSpy)).toBe(0);
    expect(WindowResizeListener._listeners.indexOf(spy)).toBe(-1);
  });

  test('should call the listener in case of resize event', () => {
    const spy = jest.fn();
    mount(<WindowResizeListener onResize={spy} />);
    expect(spy).not.toHaveBeenCalled();
    const windowWidth = 400;
    const windowHeight = 200;
    simulateResizeEvent(windowWidth, windowHeight);
    // Fast-forward until all timers have been executed
    jest.runAllTimers();
    expect(spy).toHaveBeenCalledWith({ windowWidth, windowHeight });
  });
  test('should not call the listener in case of resize event if the debounce time has not passed', () => {
    const spy = jest.fn();
    mount(<WindowResizeListener onResize={spy} />);
    expect(spy).not.toHaveBeenCalled();
    const windowWidth = 400;
    const windowHeight = 200;
    simulateResizeEvent(windowWidth, windowHeight);
    // Fast-forward until all timers have been executed
    jest.runTimersToTime(WindowResizeListener.DEBOUNCE_TIME - 1);
    expect(spy).not.toHaveBeenCalled();
    jest.runAllTimers();
    expect(spy).toHaveBeenCalledWith({ windowWidth, windowHeight });
  });
});
