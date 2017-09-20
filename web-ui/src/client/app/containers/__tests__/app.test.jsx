import React from 'react';
import { shallow } from 'enzyme';
import { App } from '../app.jsx';

jest.mock('../editor.jsx', () => ({
  Editor: () => {},
}));

describe('App component', () => {
  test('should show UserInfo child component', () => {
    const props = {
      user: {},
      env: { OAUTH_ENABLED: true },
    };
    const component = shallow(<App {...props} />);
    const userInfo = component.find('UserInfo');
    expect(userInfo.length).toEqual(1);
  });

  test('should hide UserInfo child component', () => {
    const props = {
      user: {},
      env: {},
      showUserInfo: false,
    };
    const component = shallow(<App {...props} />);
    const userInfo = component.find('UserInfo');
    expect(userInfo.length).toEqual(0);
  });
});
