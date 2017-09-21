import 'jsdom-global/register';
import React from 'react';
import { mount } from 'enzyme';
import { ValidateButton, Editor, EditorInputForm } from '../editor.jsx';

describe('EditorInputForm component', () => {
  let handleEditFile, component, editor, submitButtom;

  beforeEach(() => {
    handleEditFile = jest.fn();
    component = mount(<EditorInputForm value="" />);
    editor = component.find('Editor');
    submitButtom = component.find('.editor-input-form__button');
  });

  test('should render the editor and the buttons', () => {
    expect(editor.length).toEqual(1);
    expect(submitButtom.length).toEqual(2);
  });

  test('should have the buttons disabled', () => {
    expect(submitButtom.find('.dc-btn--disabled').length).toEqual(2);
  });
  test('should enabled the buttons with text', () => {
    component.setProps({ value: 'value' });
    expect(submitButtom.find('.dc-btn--disabled').length).toEqual(0);
  });
});
