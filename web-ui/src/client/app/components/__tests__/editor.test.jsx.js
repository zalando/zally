import 'jsdom-global/register';
import React from 'react';
import { mount } from 'enzyme';
import { EditorInputForm } from '../editor.jsx';

jest.mock('brace', () => ({}));
jest.mock('brace/ext/searchbox', () => ({}));
jest.mock('brace/mode/yaml', () => ({}));
jest.mock('brace/mode/json', () => ({}));
jest.mock('brace/theme/github', () => ({}));
jest.mock('react-ace', () => () => null);

describe('EditorInputForm component', () => {
  let component, editor, submitButtom;

  beforeEach(() => {
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
