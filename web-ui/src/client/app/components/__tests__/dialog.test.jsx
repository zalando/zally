import React from 'react';
import { shallow } from 'enzyme';
import { Dialog } from '../dialog.jsx';

describe('Dialog component', () => {
  test('should render an hidden dialog', () => {
    const component = shallow(<Dialog />);
    expect(component.find('.dc-dialog').length).toEqual(1);
    expect(component.find('.editor-dialog--show').length).toEqual(0);
    expect(component.find('.editor-dialog__close').length).toEqual(1);
  });

  test('should render an shown dialog', () => {
    const component = shallow(<Dialog show />);
    expect(component.find('.dc-dialog').length).toEqual(1);
    expect(component.find('.editor-dialog--show').length).toEqual(1);
    expect(component.find('.editor-dialog__close').length).toEqual(1);
  });
  test('should render the children', () => {
    const component = shallow(
      <Dialog>
        <div className=".lonely-child" />
      </Dialog>
    );
    expect(component.find('.editor-dialog__body').children().length).toEqual(1);
  });
});
