import React from 'react';
import { Pagination } from '@app/Pagination';
import "isomorphic-fetch"

import { mount, shallow } from 'enzyme';
import { Button, Label } from '@patternfly/react-core';

describe('Tests for Pagination', () => {

  test('should render Pagination component', () => {
    const view = shallow(<Pagination 
    handleMoveLeft={move}
    handleMoveRight={move}
    pageNumber={1}
    nextPageRecordCount={1}
    noOfRecordsOnPage={1}
/>);
    expect(view).toMatchSnapshot();
  });

  it('should render a Previous/Next Button', () => {
    const wrapper = mount(<Pagination 
      handleMoveLeft={move}
      handleMoveRight={move}
      pageNumber={1}
      nextPageRecordCount={1}
      noOfRecordsOnPage={1}
    />);
    const NavigatePageButton = wrapper.find(Button);
    expect(NavigatePageButton.exists()).toBe(true)
  });

  it('should render Badge for displaying page number', () => {
    const wrapper = mount(<Pagination 
      handleMoveLeft={move}
      handleMoveRight={move}
      pageNumber={1}
      nextPageRecordCount={1}
      noOfRecordsOnPage={1}
    />);
    const pageNumberDisplay = wrapper.find(Label);
    expect(pageNumberDisplay.exists()).toBe(true)
  });  

  function move(){
    return "R"
  }
});
