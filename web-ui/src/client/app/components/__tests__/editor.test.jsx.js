import React from 'react';
import { shallow, mount } from 'enzyme';
import { EditorInputForm, Editor } from '../editor.jsx';

jest.mock('brace', () => ({}));
jest.mock('brace/mode/yaml', () => ({}));
jest.mock('brace/mode/json', () => ({}));
jest.mock('brace/theme/chrome', () => ({}));
jest.mock('brace/ext/searchbox', () => ({}));
jest.mock('brace/ext/language_tools', () => ({}));
jest.mock('brace/snippets/yaml', () => ({}));
jest.mock('brace/snippets/json', () => ({}));
jest.mock(
  'react-ace',
  () =>
    // Named like this for the test selector to find it
    function AceEditor() {
      return null;
    }
);

describe('EditorInputForm component', () => {
  let component;

  beforeEach(() => {
    component = mount(<EditorInputForm value="" />);
  });

  test('should render the editor and the buttons', () => {
    expect(component.find('Editor')).toHaveLength(1);
    expect(component.find('.editor-input-form__button')).toHaveLength(1);
  });

  test('should have the buttons disabled', () => {
    expect(
      component.find('.editor-input-form__button').find('.dc-btn--disabled')
    ).toHaveLength(1);
  });
  test('should enabled the buttons with text', () => {
    component.setProps({ value: 'value' });
    expect(
      component.find('.editor-input-form__button').find('.dc-btn--disabled')
    ).toHaveLength(0);
  });
});

describe('Editor component', () => {
  let component;

  beforeEach(() => {
    component = shallow(<Editor value="" />);
  });

  test('should render the editor', () => {
    expect(component.find('AceEditor')).toHaveLength(1);
  });
  test('should set the mode to yaml', () => {
    component.setProps({ value: 'value' });
    expect(component.find('AceEditor').props().mode).toEqual('yaml');
  });

  test('should set the mode to json', () => {
    component.setProps({ value: '{"value": "test"}' });
    expect(component.find('AceEditor').props().mode).toEqual('json');
  });
});
