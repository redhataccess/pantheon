import React from 'react';
import { Sidebar }  from './Sidebar';
import "isomorphic-fetch"

import { shallow } from 'enzyme';

describe('Sidebar tests', () => {
  test('should render Sidebar component', () => {
    const view = shallow(<Sidebar 
      isNavOpen={true}/>);
    expect(view).toMatchSnapshot();
  });

});
