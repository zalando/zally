import 'jsdom-global/register';
import React from 'react';
import { shallow, mount } from 'enzyme';
import { StaticRouter } from 'react-router-dom';
import { ViolationsTab } from '../violations-tab.jsx';

jest.mock('../editor.jsx', () => ({
  Editor: () => null,
}));
jest.mock('../url.jsx', () => ({
  URL: () => null,
}));
jest.mock('../rules.jsx', () => ({
  Rules: () => null,
}));

describe('ViolationsTab container component', () => {
  test('should renders tabs', () => {
    const component = shallow(<ViolationsTab authenticated />);
    expect(component.find('.dc-tab').length).toEqual(1);
  });
  test('should redirect', () => {
    const component = shallow(<ViolationsTab authenticated={false} />);
    expect(component.find('.dc-tab').length).toEqual(0);
    expect(component.find('Redirect').length).toEqual(1);
  });
  test('render the URL tab on /', () => {
    const component = mount(
      <StaticRouter location={{ pathname: '/' }}>
        <ViolationsTab authenticated />
      </StaticRouter>
    );
    expect(component.find('URL').length).toEqual(1);
    expect(component.find('Editor').length).toEqual(0);
    expect(component.find('Rules').length).toEqual(0);
  });
  test('render the Editor tab on /editor', () => {
    const component = mount(
      <StaticRouter location={{ pathname: '/editor' }}>
        <ViolationsTab authenticated />
      </StaticRouter>
    );
    expect(component.find('URL').length).toEqual(0);
    expect(component.find('Editor').length).toEqual(1);
    expect(component.find('Rules').length).toEqual(0);
  });
  test('render the Rules tab on /rules', () => {
    const component = mount(
      <StaticRouter location={{ pathname: '/rules' }}>
        <ViolationsTab authenticated />
      </StaticRouter>
    );
    expect(component.find('URL').length).toEqual(0);
    expect(component.find('Editor').length).toEqual(0);
    expect(component.find('Rules').length).toEqual(1);
  });
});
