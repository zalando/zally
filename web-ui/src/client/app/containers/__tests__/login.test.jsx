import React from 'react';
import { shallow } from 'enzyme';
import { Login } from '../login.jsx';

describe('Login container component', () => {
  const notAuthenticatedSelector = '[data-test-id="not-authenticated"]';
  const authenticatedSelector = '[data-test-id="authenticated"]';

  test('should show login UI if not authenticated', () => {
    const component = shallow(<Login user={{ authenticated: false }} />);
    expect(component.find(notAuthenticatedSelector)).toHaveLength(1);
    expect(component.find(authenticatedSelector)).toHaveLength(0);
  });

  test('should show already logged-in UI if authenticated', () => {
    const component = shallow(<Login user={{ authenticated: true }} />);
    expect(component.find(authenticatedSelector)).toHaveLength(1);
    expect(component.find(notAuthenticatedSelector)).toHaveLength(0);
  });
});
