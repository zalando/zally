import React from 'react';
import { shallow } from 'enzyme';
import { Rule, RuleType, RuleLink, RulesTab } from '../rules.jsx';

describe('RuleType component', () => {
  describe('when ruleType is MUST', () => {
    test('should return dc-status--error', () => {
      const component = shallow(<RuleType type="MUST" />);
      const status = component.find('.dc-status--error');

      expect(status).toHaveLength(1);
    });
  });

  describe('when ruleType is SHOULD', () => {
    test('should return dc-status--new', () => {
      const component = shallow(<RuleType type="SHOULD" />);
      const status = component.find('.dc-status--new');

      expect(status).toHaveLength(1);
    });
  });

  describe('when ruleType niether MUST nor SHOULD', () => {
    test('should return dc-status--inactive', () => {
      const component = shallow(<RuleType type="" />);
      const status = component.find('.dc-status--inactive');

      expect(status).toHaveLength(1);
    });
  });
});

describe('RuleLink component', () => {
  test('should return a rule link', () => {
    const component = shallow(<RuleLink url="foo" />);
    const link = component.find('a');
    expect(link).toHaveLength(1);
    expect(link.text()).toEqual('foo');
    expect(link.getElement(0).props.href).toEqual('foo');
  });
});

describe('RuleTab component', () => {
  test('should render a list of rules', () => {
    const rules = [{}, {}, {}];
    const component = shallow(<RulesTab rules={rules} />);
    expect(component.find('Msg')).toHaveLength(0);
    expect(component.find('Rule')).toHaveLength(rules.length);
  });

  test('should render the error', () => {
    const errorText = 'error text';
    const component = shallow(<RulesTab rules={[]} error={errorText} />);
    expect(component.find('Msg')).toHaveLength(1);
    expect(component.find('Msg').props().text).toEqual(errorText);
  });
});

describe('Rule component', () => {
  test('should render a rule with url', () => {
    const rule = {
      name: 'NoUnusedDefinitionsRule',
      title: 'Do not leave unused definitions',
      type: 'SHOULD',
      url: 'someurl',
      code: 'S005',
      is_active: true,
    };

    const component = shallow(<Rule rule={rule} />);
    const RuleLink = component.find('RuleLink');

    expect(component.find('RuleType')).toHaveLength(1);
    expect(RuleLink).toHaveLength(1);
    expect(RuleLink.prop('url')).toEqual('someurl');
    expect(component.find('RuleType')).toHaveLength(1);
  });

  test('should render a rule without url', () => {
    const rule = {
      name: 'NoUnusedDefinitionsRule',
      title: 'Do not leave unused definitions',
      type: 'SHOULD',
      url: null,
      code: 'S005',
    };

    const component = shallow(<Rule rule={rule} />);
    const RuleLink = component.find('RuleLink');

    expect(component.find('RuleType')).toHaveLength(1);
    expect(RuleLink).toHaveLength(0);
    expect(component.find('RuleType')).toHaveLength(1);
  });
});
