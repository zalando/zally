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
    expect(component.find('.dc-tab')).toHaveLength(1);
  });
  test('should redirect', () => {
    const component = shallow(<ViolationsTab authenticated={false} />);
    expect(component.find('.dc-tab')).toHaveLength(0);
    expect(component.find('Redirect')).toHaveLength(1);
  });
  test('render the URL tab on /', () => {
    const component = mount(
      <StaticRouter location={{ pathname: '/' }}>
        <ViolationsTab authenticated />
      </StaticRouter>
    );
    expect(component.find('URL')).toHaveLength(1);
    expect(component.find('Editor')).toHaveLength(0);
    expect(component.find('Rules')).toHaveLength(0);
  });
  test('render the Editor tab on /editor', () => {
    const component = mount(
      <StaticRouter location={{ pathname: '/editor' }}>
        <ViolationsTab authenticated />
      </StaticRouter>
    );
    expect(component.find('URL')).toHaveLength(0);
    expect(component.find('Editor')).toHaveLength(1);
    expect(component.find('Rules')).toHaveLength(0);
  });
  test('render the Editor tab on /editor/:externalId', () => {
    const component = mount(
      <StaticRouter location={{ pathname: '/editor/:externalId' }}>
        <ViolationsTab authenticated />
      </StaticRouter>
    );
    expect(component.find('URL')).toHaveLength(0);
    expect(component.find('Editor')).toHaveLength(1);
    expect(component.find('Rules')).toHaveLength(0);
  });
  test('render the Rules tab on /rules', () => {
    const component = mount(
      <StaticRouter location={{ pathname: '/rules' }}>
        <ViolationsTab authenticated />
      </StaticRouter>
    );
    expect(component.find('URL')).toHaveLength(0);
    expect(component.find('Editor')).toHaveLength(0);
    expect(component.find('Rules')).toHaveLength(1);
  });
});
