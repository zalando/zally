import React from 'react';
import { shallow } from 'enzyme';
import { URL } from '../url.jsx';

describe('URL container component', () => {
  let MockStorage;

  beforeEach(() => {
    MockStorage = {
      setItem: jest.fn(),
      getItem: jest.fn(),
    };
  });

  test('should set expected state values when instantiated', () => {
    const value = 'http://github.com/petstore.json';
    MockStorage.getItem.mockReturnValueOnce(value);
    const component = shallow(<URL Storage={MockStorage} />);
    expect(component.state().inputValue).toEqual(value);
  });

  test('on input value change save new value in the Storage and update the state accordingly', () => {
    const value = 'http://github.com/petstore.json';
    const newValue = 'http://github.com/swagger.json';
    MockStorage.getItem.mockReturnValueOnce(value);
    const component = shallow(<URL Storage={MockStorage} />);

    component.instance().handleOnInputValueChange({
      target: { value: newValue },
    });

    expect(MockStorage.setItem).toHaveBeenCalledWith('url-value', newValue);

    expect(component.state().inputValue).toEqual(newValue);
  });

  test("should use an empty string as input value if Storage doesn't contain an item", () => {
    const component = shallow(<URL Storage={MockStorage} />);
    expect(component.state().inputValue).toEqual('');
  });

  describe('edit file', () => {
    test('should set pending flag and call method', () => {
      const value = 'http://github.com/swagger.json';
      MockStorage.getItem.mockReturnValueOnce(value);
      const mockGetFile = jest.fn().mockReturnValue(Promise.resolve());
      const component = shallow(
        <URL Storage={MockStorage} getFile={mockGetFile} />
      );
      component.instance().handleOnEditFile();
      expect(component.state().pending).toBeTruthy();
      expect(mockGetFile).toHaveBeenCalledWith(value);
    });

    test('should fetch file, save file to storage and redirect', () => {
      const value = 'http://github.com/swagger.json';
      const mockFile = 'file';
      MockStorage.getItem.mockReturnValueOnce(value);
      const mockGetFile = jest.fn().mockReturnValue(Promise.resolve(mockFile));
      const component = shallow(
        <URL Storage={MockStorage} getFile={mockGetFile} />
      );
      expect.assertions(5);
      expect(component.find('Redirect')).toHaveLength(0);
      return component
        .instance()
        .handleOnEditFile()
        .then(() => {
          expect(component.state().pending).toBeFalsy();
          expect(component.state().goToEditor).toBeTruthy();
          expect(MockStorage.setItem).toHaveBeenCalledWith(
            'editor-value',
            mockFile
          );
          component.update();
          expect(component.find('Redirect')).toHaveLength(1);
        });
    });

    test('should handle error when fetching file', () => {
      const value = 'http://github.com/swagger.json';
      const mockError = 'file';
      MockStorage.getItem.mockReturnValueOnce(value);
      const mockGetFile = jest.fn().mockReturnValue(Promise.reject(mockError));
      const component = shallow(
        <URL Storage={MockStorage} getFile={mockGetFile} />
      );
      expect.assertions();
      return component
        .instance()
        .handleOnEditFile()
        .then(() => {
          expect(component.state().pending).toBeFalsy();
          expect(component.state().ajaxComplete).toBeTruthy();
          expect(component.state().error).toEqual(mockError);
        });
    });
  });
});
