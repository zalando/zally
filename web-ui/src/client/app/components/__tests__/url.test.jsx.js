import React from 'react';
import { mount } from 'enzyme';
import { URLInputForm } from '../url.jsx';

describe('URLInputForm component', () => {
  let handleEditFile, component;

  beforeEach(() => {
    handleEditFile = jest.fn();
    component = mount(<URLInputForm value="" onEditFile={handleEditFile} />);
  });

  test('should render the input and the buttons', () => {
    expect(component.find('.dc-input')).toHaveLength(1);
    expect(component.find('.dc-btn--primary')).toHaveLength(1);
    expect(component.find('.dc-btn--secondary')).toHaveLength(1);
  });

  test('should have the buttons disabled', () => {
    expect(
      component.find('.dc-btn--secondary').find('.dc-btn--disabled')
    ).toHaveLength(1);
    expect(
      component.find('.dc-btn--primary').find('.dc-btn--disabled')
    ).toHaveLength(1);
  });
  test('should enabled the buttons with text', () => {
    component.setProps({ value: 'value' });
    expect(
      component.find('.dc-btn--secondary').find('.dc-btn--disabled')
    ).toHaveLength(0);
    expect(
      component.find('.dc-btn--primary').find('.dc-btn--disabled')
    ).toHaveLength(0);
  });

  describe('when clicking on edit button  ', () => {
    test('should not call the action when disabled', () => {
      component.find('.dc-btn--secondary').simulate('click');
      expect(handleEditFile).not.toHaveBeenCalled();
    });
    test('should call the action when enabled', () => {
      component.setProps({ value: 'value' });
      component.find('.dc-btn--secondary').simulate('click');
      expect(handleEditFile).toHaveBeenCalled();
    });
  });
});
