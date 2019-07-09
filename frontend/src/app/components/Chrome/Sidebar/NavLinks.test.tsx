import React from 'react';
import { NavLinks }  from './NavLinks';
import "isomorphic-fetch"

import { shallow } from 'enzyme';

describe('NavLinks tests', () => {
  test('should render NavLinks component', () => {
    const view = shallow(<NavLinks />);
    expect(view).toMatchSnapshot();
  });

});
