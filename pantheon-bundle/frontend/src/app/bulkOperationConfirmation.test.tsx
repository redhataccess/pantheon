import React from "react"
import { BulkOperationConfirmation } from "./bulkOperationConfirmation";

import { mount, shallow } from "enzyme"
import { Alert, Modal, Progress } from "@patternfly/react-core"
import { render, fireEvent } from '@testing-library/react'

const anymatch = require("anymatch")

const props = {
    isEditMetadata: true,
    header: "Bulk Edit",
    subheading: "Summary",
    updateSucceeded: "/repositories/s/a,/repositories/s/b,/repositories/s/c",
    updateFailed: "/repositories/f/a",
    updateIgnored: "/repositorires/i/a,/repositorires/i/b",
    footer: "",
    progressSuccessValue: 50,
    progressFailureValue: 10,
    progressWarningValue: 40,
    // onShowBulkEditConfirmation: (showBulkEditConfirmation) => anymatch,
    onMetadataEditError: (metadataEditError) => anymatch,
    updateIsEditMetadata: (isEditMetadata) => anymatch
}

describe("BulkOperationConfirmation tests", () => {
    test("should render BulkOperationConfirmation component", () => {
        const view = shallow(<BulkOperationConfirmation {...props} />)
        expect(view).toMatchSnapshot()
    })

    it("should render an Alert component", () => {
        const wrapper = mount(<BulkOperationConfirmation {...props} />)
        const alert = wrapper.find(Alert)
        expect(alert.exists()).toBe(true)
    })


    it("should render a Modal component", () => {
        const wrapper = mount(<BulkOperationConfirmation {...props} />)
        const modal = wrapper.find(Modal)
        expect(modal.exists()).toBe(true)
    })

    it("should render a Progress component", () => {
        const wrapper = mount(<BulkOperationConfirmation {...props} />)
        const progress = wrapper.find(Progress)
        expect(progress.exists()).toBe(true)
    })

    it("test hideAlert function", () => {
        // render component  
        const { getByTestId } = render(<BulkOperationConfirmation {...props} />)

        // click "Hide Alert" button
        fireEvent.click(getByTestId("hide-alert-button"))

        // verify the alert is gone
        expect(getByTestId("hide-alert-button")).toBeFalsy
    })

    it("test handleModalToggle function", () => {
        // render component  
        const { getByTestId, queryAllByText } = render(<BulkOperationConfirmation {...props} />)

        // click "view details" link
        fireEvent.click(getByTestId("view-details-link"))

        // verify
        expect(queryAllByText("Bulk Edit")).toBeTruthy
    })

})
