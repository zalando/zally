import 'jsdom-global/register';
import React from 'react';
import { mount } from 'enzyme';
import { URLInputForm } from '../url.jsx';

describe('URLInputForm component', () => {
  let handleEditFile, component, urlInput, submitButtom, editButton;

  beforeEach(() => {
    handleEditFile = jest.fn();
    component = mount(
      <URLInputForm inputValue="" onEditFile={handleEditFile} />
    );
    urlInput = component.find('.dc-input');
    submitButtom = component.find('.dc-btn--primary');
    editButton = component.find('.dc-btn--secondary');
  });

  test('should render the input and the buttons', () => {
    expect(urlInput.length).toEqual(1);
    expect(submitButtom.length).toEqual(1);
    expect(urlInput.length).toEqual(1);
  });

  test('should have the buttons disabled', () => {
    expect(editButton.find('.dc-btn--disabled').length).toEqual(1);
    expect(submitButtom.find('.dc-btn--disabled').length).toEqual(1);
  });
  test('should enabled the buttons with text', () => {
    component.setProps({ inputValue: 'value' });
    expect(editButton.find('.dc-btn--disabled').length).toEqual(0);
    expect(submitButtom.find('.dc-btn--disabled').length).toEqual(0);
  });

  describe('when clicking on edit button  ', () => {
    test('should not call the action when disabled', () => {
      editButton.simulate('click');
      expect(handleEditFile).not.toHaveBeenCalled();
    });
    test('should call the action when enabled', () => {
      component.setProps({ inputValue: 'value' });
      editButton.simulate('click');
      expect(handleEditFile).toHaveBeenCalled();
    });
  });
});
