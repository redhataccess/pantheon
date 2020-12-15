import React from "react";
import { SearchBeta } from "@app/searchBeta";
import { shallow, mount } from "enzyme";
import sinon from "sinon"
import renderer from "react-test-renderer"
import "@app/fetchMock"
import { any } from "prop-types";
import { mockStateUser } from "@app/TestResources"
import { Button, Drawer, Toolbar, ExpandableSection, Divider, SimpleList, Select } from "@patternfly/react-core";
import { HashRouter as Router, Link } from "react-router-dom"


describe("SearchBeta tests", () => {
  test("should render default Search component", () => {
    const view = shallow(<SearchBeta {...mockStateUser} />);
    expect(view).toMatchSnapshot();
  });

  it("should render a Button", () => {
    const wrapper = mount(<Router><SearchBeta {...mockStateUser} /></Router>)
    const button = wrapper.find(Button)
    expect(button.exists()).toBe(true)
  })

  it("should render a Drawer", () => {
    const wrapper = mount(<Router><SearchBeta {...mockStateUser} /></Router>)
    const drawer = wrapper.find(Drawer)
    expect(drawer.exists()).toBe(true)
  })

  it("should render a Toolbar", () => {
    const wrapper = mount(<Router><SearchBeta {...mockStateUser} /></Router>)
    const toolbar = wrapper.find(Toolbar)
    expect(toolbar.exists()).toBe(true)
  })

  it("should render a ExpandableSection", () => {
    const wrapper = mount(<Router><SearchBeta {...mockStateUser} /></Router>)
    const expandableSection = wrapper.find(ExpandableSection)
    expect(expandableSection.exists()).toBe(true)
  })

  it("should render a Divider", () => {
    const wrapper = mount(<Router><SearchBeta {...mockStateUser} /></Router>)
    const divider = wrapper.find(Divider)
    expect(divider.exists()).toBe(true)
  })

  it("should render a SimpleList", () => {
    const wrapper = mount(<Router><SearchBeta {...mockStateUser} /></Router>)
    const simpleList = wrapper.find(SimpleList)
    expect(simpleList.exists()).toBe(true)
  })

  it("should render a Select", () => {
    const wrapper = mount(<Router><SearchBeta {...mockStateUser} /></Router>)
    const select = wrapper.find(Select)
    expect(select.exists()).toBe(true)
  })

});
