import React from 'react';
import { Rules } from '../rules.jsx';
import { shallow } from 'enzyme';
import 'url-search-params-polyfill';

describe('Rules container component', () => {
  let component, props, container, getSupportedRules;

  beforeEach(() => {
    getSupportedRules = jest.fn();
    props = {
      getSupportedRules: getSupportedRules,
      location: { search: '' },
    };
    component = shallow(<Rules {...props} />);
    container = component.instance();
  });

  test('should redirect with no filter', () => {
    expect(component.find('Redirect')).toHaveLength(1);
  });

  test('should not redirect with a filter', () => {
    const rules = [{}];
    getSupportedRules.mockReturnValueOnce(
      Promise.resolve({
        rules: rules,
      })
    );
    component.setProps({ location: { search: '?is_active=true' } });
    expect(component.find('Redirect')).toHaveLength(0);
  });

  test('should redirect with a bad query', () => {
    component.setProps({ location: { search: '?badquery=false' } });
    expect(component.find('Redirect')).toHaveLength(1);
  });

  test('should not fetch rules with no filter', () => {
    container.componentDidMount();
    expect(container.state.filter).toEqual(null);
    expect(getSupportedRules).not.toHaveBeenCalled();
  });

  describe('when call fetchRules', () => {
    test('should handle success', () => {
      const rules = [{}];
      getSupportedRules.mockReturnValueOnce(
        Promise.resolve({
          rules: rules,
        })
      );
      const promise = container.fetchRules();

      expect.assertions(5);
      return promise.then(() => {
        expect(getSupportedRules).toHaveBeenCalled();
        expect(container.state.error).toBe(null);
        expect(container.state.pending).toBe(false);
        expect(container.state.ajaxComplete).toBe(true);
        expect(container.state.rules).toBe(rules);
      });
    });

    test('should handle failure', () => {
      const mockError = { detail: 'error' };
      getSupportedRules.mockReturnValueOnce(Promise.reject(mockError));
      expect.assertions(5);
      return container.fetchRules().catch(() => {
        expect(getSupportedRules).toHaveBeenCalled();
        expect(container.state.error).toBe(mockError.detail);
        expect(container.state.pending).toBe(false);
        expect(container.state.ajaxComplete).toBe(true);
        expect(container.state.rules).toEqual([]);
      });
    });

    test('should handle failure without error detail', () => {
      getSupportedRules.mockReturnValueOnce(Promise.reject({}));
      expect.assertions(5);
      return container.fetchRules().catch(() => {
        expect(getSupportedRules).toHaveBeenCalled();
        expect(container.state.error).toBe(Rules.DEFAULT_ERROR_MESSAGE);
        expect(container.state.pending).toBe(false);
        expect(container.state.ajaxComplete).toBe(true);
        expect(container.state.rules).toEqual([]);
      });
    });
  });

  describe('when call parseFilterValue', () => {
    test('should return null when null passed', () => {
      expect(container.parseFilterValue(null)).toBe(null);
    });
    test('should return null when empty object passed', () => {
      expect(container.parseFilterValue('')).toBe(null);
    });
    test('should return correct object when is_active false', () => {
      expect(container.parseFilterValue('?is_active=false')).toEqual({
        is_active: false,
      });
    });
    test('should return correct object when is_active true', () => {
      expect(container.parseFilterValue('?is_active=true')).toEqual({
        is_active: true,
      });
    });
  });

  describe('when call sameFilter', () => {
    beforeEach(() => {
      getSupportedRules.mockReturnValueOnce(Promise.resolve({ rules: [] }));
    });

    test('should return false with no filter', () => {
      expect(container.sameFilter(false)()).toBeFalsy();
      expect(container.sameFilter(true)()).toBeFalsy();
    });

    test('should return true with filter set to true', () => {
      component.setProps({ location: { search: '?is_active=true' } });
      expect(container.sameFilter(false)()).toBeFalsy();
      expect(container.sameFilter(true)()).toBeTruthy();
    });
    test('should return true with filter set to false', () => {
      component.setProps({ location: { search: '?is_active=false' } });
      expect(container.sameFilter(false)()).toBeTruthy();
      expect(container.sameFilter(true)()).toBeFalsy();
    });
  });
});
