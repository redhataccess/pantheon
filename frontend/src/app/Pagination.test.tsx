import React from 'react';
import { Paginate } from '@app/Pagination';
import "isomorphic-fetch"

import { mount, shallow } from 'enzyme';
import { LevelItem } from '@patternfly/react-core';

describe('Tests for Pagination', () => {

  test('should render Pagination component', () => {
    const view = shallow(<Paginate 
    handleMoveLeft={move}
    handleMoveRight={move}
    handleMoveToFirst={move}
    pageNumber={1}
    nextPageRecordCount={1}
    noOfRecordsOnPage={1}
/>);
    expect(view).toMatchSnapshot();
  });

  it('should render Badge for displaying page number', () => {
    const wrapper = mount(<Paginate 
      handleMoveLeft={move}
      handleMoveRight={move}
      handleMoveToFirst={move}
      pageNumber={1}
      nextPageRecordCount={1}
      noOfRecordsOnPage={1}
    />);
    const pageNumberDisplay = wrapper.find(LevelItem);
    expect(pageNumberDisplay.exists()).toBe(true)
  });  

  function move(){
    return "R"
  }
});
