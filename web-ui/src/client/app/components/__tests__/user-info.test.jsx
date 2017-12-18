import React from 'react';
import { shallow } from 'enzyme';
import { default as UserInfo } from '../user-info.jsx';

describe('UserInfo component', () => {
  let handleLogout, component, userElem, logoutLink;
  const username = 'foo';

  beforeEach(() => {
    handleLogout = jest.fn();
    component = shallow(
      <UserInfo
        username={username}
        authenticated={true}
        onLogout={handleLogout}
      />
    );
    userElem = component.find('.user-info__icon');
    logoutLink = component.find('.user-info__logout');
  });

  test('should render the component', () => {
    expect(userElem).toHaveLength(1);
    expect(userElem.text()).toEqual(username);
    expect(logoutLink).toHaveLength(1);
  });

  test('should render the login button', () => {
    component.setProps({ authenticated: false });
    expect(component.find('.user-info__logout')).toHaveLength(0);
    expect(component.find('.user-info__login')).toHaveLength(1);
  });

  describe('when clicking on logout link', () => {
    test('should handle the action', () => {
      logoutLink.simulate('click', { preventDefault() {} });
      expect(handleLogout).toHaveBeenCalled();
    });
  });
});
