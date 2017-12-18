import React from 'react';
import { shallow } from 'enzyme';
import { Root } from '../root.jsx';
import { RestService } from '../../services/rest.js';

jest.mock('../app.jsx', () => ({
  App: () => {},
}));

describe('Root component', () => {
  test('render the app', () => {
    const component = shallow(<Root env={{}} RestService={RestService} />);
    expect(component.find('App')).toHaveLength(1);
  });
});
