import React from 'react';
import { BuildInfo }  from './BuildInfo';
import "isomorphic-fetch"

import { shallow } from 'enzyme';

describe('BuildInfo tests', () => {
  test('should render NavLinks component', () => {
    const view = shallow(<BuildInfo />);
    expect(view).toMatchSnapshot();
  });

  test('getBuildInfo call', () => {
    const spy = jest.spyOn(BuildInfo.prototype, 'getBuildInfo');
    shallow(<BuildInfo />);

    expect(spy).toHaveBeenCalled()
  });

});