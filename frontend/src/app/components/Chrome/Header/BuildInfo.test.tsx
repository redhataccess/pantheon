import React from 'react';
import { BuildInfo }  from './BuildInfo';
import { Link } from "react-router-dom";
import { HashRouter as Router } from 'react-router-dom';
import "isomorphic-fetch"

import { mount, shallow } from 'enzyme';

describe('BuildInfo tests', () => {
  test('should render NavLinks component', () => {
    const view = shallow(<BuildInfo />);
    expect(view).toMatchSnapshot();
  });

});