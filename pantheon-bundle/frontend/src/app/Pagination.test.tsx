import React from "react"
import { Pagination } from "@app/Pagination"
import "@app/fetchMock"

import { mount, shallow } from "enzyme"
import { LevelItem } from "@patternfly/react-core"

describe("Tests for Pagination", () => {

  test("should render Pagination component", () => {
    const view = shallow(<Pagination 
    handleMoveLeft={move}
    handleMoveRight={move}
    handleMoveToFirst={move}
    pageNumber={1}
    nextPageRecordCount={1}
    handlePerPageLimit={move}
    handleItemsPerPage={move}
    perPageLimit={1}
    showDropdownOptions={true}
    bottom={true}
    className={"test"}
    currentBulkOperation={""}
/>)
    expect(view).toMatchSnapshot()
  })

  it("should render Badge for displaying page number", () => {
    const wrapper = mount(<Pagination 
      handleMoveLeft={move}
      handleMoveRight={move}
      handleMoveToFirst={move}
      pageNumber={1}
      nextPageRecordCount={1}
      handlePerPageLimit={move}
      handleItemsPerPage={move}
      perPageLimit={1}
      showDropdownOptions={true}
      bottom={true}
      className={"test"}
      currentBulkOperation={""}
    />)
    const pageNumberDisplay = wrapper.find(LevelItem)
    expect(pageNumberDisplay.exists()).toBe(true)
  })  

  function move(){
    return "R"
  }
})
