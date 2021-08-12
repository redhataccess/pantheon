import React from "react"
import { BulkOperationPublish } from "./BulkOperationPublish";

import { mount, shallow } from "enzyme"
import { Modal } from "@patternfly/react-core"
import { render } from '@testing-library/react'
import fetchMock from 'fetch-mock';

const anymatch = require("anymatch")
const props = {
    documentsSelected: anymatch,
    contentTypeSelected: "module",
    isBulkPublish: true,
    isBulkUnpublish: false,
    bulkOperationCompleted: true,
    updateIsBulkPublish: (isBulkPublish) => anymatch,
    updateIsBulkUnpublish: (isBulkUnpublish) => anymatch,
    updateBulkOperationCompleted: (bulkOperationCompleted) => anymatch
}

describe("BulkOperationPublish tests", () => {
    const api = "/content/products.harray.1.json"

    const response = {
        body: { __name__: "test product" },
        headers: { "content-type": "application/json" }
    }

    const Button = ({ onClick, children }) => (
        <button onClick={onClick}>{children}</button>
    )

    beforeEach(() => {
        fetchMock.getOnce(api, response)
    })


    afterEach(() => {
        fetchMock.reset();
        fetchMock.restore();
    });

    test("should render BulkOperationPublish component", () => {
        const view = shallow(<BulkOperationPublish {...props} />)
        expect(view).toMatchSnapshot()
    })

    it("should render a Modal component", () => {

        const wrapper = mount(<BulkOperationPublish {...props} />)
        const modal = wrapper.find(Modal)
        expect(modal.exists()).toBe(true)
    })

    it("test handleModalToggle function", () => {
        const { queryAllByText } = render(<BulkOperationPublish {...props} />)
        expect(queryAllByText("The publish process may take a while to update all files")).toBeTruthy()
        expect(queryAllByText("Publish")).toBeTruthy()
    })

})
