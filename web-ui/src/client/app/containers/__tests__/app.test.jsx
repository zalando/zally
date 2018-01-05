import React from 'react';
import { shallow, mount } from 'enzyme';
import { StaticRouter } from 'react-router-dom';
import { App } from '../app.jsx';

jest.mock('../violations-tab.jsx', () => ({
  ViolationsTab: () => null,
}));
jest.mock('../login.jsx', () => ({
  Login: () => null,
}));

describe('App component', () => {
  test('should show UserInfo child component', () => {
    const props = {
      user: {},
      env: { OAUTH_ENABLED: true },
    };
    const component = shallow(<App {...props} />);
    expect(component.find('UserInfo')).toHaveLength(1);
  });

  test('should hide UserInfo child component', () => {
    const props = {
      user: {},
      env: {},
    };
    const component = shallow(<App {...props} />);
    expect(component.find('UserInfo')).toHaveLength(0);
  });

  test('should render Login route', () => {
    const props = {
      user: {},
      env: {},
    };
    const component = mount(
      <StaticRouter location={{ pathname: '/login' }}>
        <App {...props} />
      </StaticRouter>
    );
    expect(component.find('Login')).toHaveLength(1);
    expect(component.find('ViolationsTab')).toHaveLength(0);
  });

  test('should render the ViolationsTab', () => {
    const props = {
      user: {},
      env: {},
    };
    const component = mount(
      <StaticRouter location={{ pathname: '/' }}>
        <App {...props} />
      </StaticRouter>
    );
    expect(component.find('Login')).toHaveLength(0);
    expect(component.find('ViolationsTab')).toHaveLength(1);
  });
});
