import React from 'react';
import { shallow } from 'enzyme';
import { Editor } from '../editor.jsx';

jest.mock('../../components/editor.jsx', () => ({
  EditorInputForm: () => {},
}));

describe('Editor container component', () => {
  let MockStorage;

  beforeEach(() => {
    MockStorage = {
      setItem: jest.fn(),
      getItem: jest.fn(),
    };
  });

  test('should set expected state values when instantiated', () => {
    const editorValue = 'prop: foo';
    MockStorage.getItem.mockReturnValueOnce(editorValue);
    const component = shallow(<Editor Storage={MockStorage} />);
    expect(component.state().editorValue).toBe(editorValue);
  });

  test('should set expected state values when componentDidMount', () => {
    const editorValue = 'prop: foo';
    MockStorage.getItem.mockReturnValueOnce(editorValue);
    const component = shallow(<Editor Storage={MockStorage} />);
    component.instance().componentDidMount();
    expect(component.state().inputValue).toEqual(editorValue);
  });

  test('on input value change save new value in the Storage and update the state accordingly', () => {
    const editorValue = 'prop: foo';
    const newEditorValue = 'foo: prop';
    MockStorage.getItem.mockReturnValueOnce(editorValue);
    const component = shallow(<Editor Storage={MockStorage} />);

    component.instance().handleOnInputValueChange(newEditorValue);

    expect(MockStorage.setItem).toHaveBeenCalledWith(
      'editor-value',
      newEditorValue
    );

    expect(component.state().editorValue).toBe(newEditorValue);
    expect(component.state().inputValue).toEqual(newEditorValue);
  });

  test("should use an empty string as editor value and input value if Storage doesn't contain an item", () => {
    const component = shallow(<Editor Storage={MockStorage} />);

    expect(component.state().editorValue).toBe('');
    expect(component.state().inputValue).toBe('');
  });
});
