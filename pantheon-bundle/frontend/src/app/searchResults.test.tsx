import React from "react";
import { SearchResults } from "@app/searchResults";
import { shallow, mount } from "enzyme";
import sinon from "sinon"
import renderer from "react-test-renderer"
import "@app/fetchMock"
import { any } from "prop-types";
// import { props } from "@app/TestResources"
import { Button, Drawer, Toolbar, ExpandableSection, Divider, SimpleList, Select, EmptyState, Title } from "@patternfly/react-core";
import { HashRouter as Router, Link } from "react-router-dom"
import { Pagination } from "@app/Pagination"
import { Table } from "@patternfly/react-table"

const anymatch = require("anymatch")
const props = {
    contentType: "assemblies",
    keyWord: "test",
    filters: { ctype: ["PROCEDURE"], status: ["draft"] },
    productsSelected: [],
    repositoriesSelected: ["test"],
    userAuthenticated: true,
    onGetdocumentsSelected: (documentsSelected) => anymatch,
    onSelectContentType: (contentType) => anymatch,
    currentBulkOperation: "",
    disabledClassname: ""
}

const propsEmptyState = {
    contentType: "",
    keyWord: "",
    filters: { ctype: [], status: [] },
    productsSelected: [],
    repositoriesSelected: [],
    userAuthenticated: true,
    onGetdocumentsSelected: (documentsSelected) => anymatch,
    onSelectContentType: (contentType) => anymatch,
    currentBulkOperation: "",
    disabledClassname: ""

}

describe("SearchResults tests", () => {
    test("should render default Search component", () => {
        const view = shallow(<SearchResults {...props} />);
        expect(view).toMatchSnapshot();
    });

    it("should render a Table component", () => {
        const wrapper = mount(<Router><SearchResults {...props} /></Router>)
        wrapper.setState({isEmptyResults: false})
        const table = wrapper.find(Table)
        expect(table.exists()).toBe(true)
    })

    it("should render an EmptyState component", () => {
        const wrapper = mount(<Router><SearchResults {...propsEmptyState} /></Router>)
        const emptyState = wrapper.find(EmptyState)
        expect(emptyState.exists()).toBe(true)
    })

    it("should render a Title component", () => {
        const wrapper = mount(<Router><SearchResults {...propsEmptyState} /></Router>)
        const title = wrapper.find(Title)
        expect(title.exists()).toBe(true)
    })

    it("should render a Divider", () => {
        const wrapper = mount(<Router><SearchResults {...props} /></Router>)
        const divider = wrapper.find(Divider)
        expect(divider.exists()).toBe(true)
    })

    // it("should render a Pagination component", () => {
    //     const wrapper = mount(<Router><SearchResults {...props} /></Router>)
    //     wrapper.setState({isEmptyResults: false})
    //     const pagination = wrapper.find(Pagination)
    //     expect(pagination.exists()).toBe(true)
    // })

});
