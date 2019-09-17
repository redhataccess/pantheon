import React from 'react';
import { BuildInfo }  from './BuildInfo';
import "isomorphic-fetch"
import sinon from "sinon";

import { shallow } from 'enzyme';

describe('BuildInfo tests', () => {
  test('should render NavLinks component', () => {
    const view = shallow(<BuildInfo />);
    expect(view).toMatchSnapshot();
  });

  test('getBuildInfo call', () => {
    const wrapper = shallow(<BuildInfo />);
    const instance = wrapper.instance();
    const spy = sinon.spy(instance, 'getBuildInfo');

    wrapper.setState({buildDate: '',commitHash: '', commitText: ''})
    sinon.assert.calledOnce(spy);
  });

});