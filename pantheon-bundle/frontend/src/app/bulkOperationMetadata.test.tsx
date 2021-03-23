import React from "react"
import { BulkOperationMetadata } from "./bulkOperationMetadata";

import { mount, shallow } from "enzyme"
import { Alert, Form, FormAlert, FormGroup, FormSelectOption, InputGroup, Modal, Progress } from "@patternfly/react-core"
import { render, fireEvent, queryAllByLabelText, getByText, screen } from '@testing-library/react'
import fetchMock from 'fetch-mock';
// import 'react-testing-library/extend-expect';
// interface ExtendedMatchers extends jest.Matchers<void> {
//   toHaveTextContent: (htmlElement: string) => object;
//   toBeInTheDOM: () => void;
// }

const anymatch = require("anymatch")
const props = {
    documentsSelected: anymatch,
    contentTypeSelected: "module",
    isEditMetadata: true,
    updateIsEditMetadata: (isEditMetadata) => anymatch,
}
describe("BulkOperationMetadata tests", () => {
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

    test("should render BulkOperationMetadata component", () => {
        const view = shallow(<BulkOperationMetadata {...props} />)
        expect(view).toMatchSnapshot()
    })

    it("should render an Alert component", () => {

        const wrapper = mount(<BulkOperationMetadata {...props} />)
        const alert = wrapper.find(Alert)
        expect(alert.exists()).toBe(true)
    })

    it("should render an Button component", () => {

        const wrapper = mount(<BulkOperationMetadata {...props} />)
        const button = wrapper.find(Alert)
        expect(button.exists()).toBe(true)
    })

    it("should render a Modal component", () => {

        const wrapper = mount(<BulkOperationMetadata {...props} />)
        const modal = wrapper.find(Modal)
        expect(modal.exists()).toBe(true)
    })

    it("should render a Form component", () => {

        const wrapper = mount(<BulkOperationMetadata {...props} />)
        const form = wrapper.find(Form)
        expect(form.exists()).toBe(true)
    })

    it("should render a FormGroup component", () => {

        const wrapper = mount(<BulkOperationMetadata {...props} />)
        const formGroup = wrapper.find(FormGroup)
        expect(formGroup.exists()).toBe(true)
    })

    it("should render a FormAlert component", () => {

        const wrapper = mount(<BulkOperationMetadata {...props} />)
        wrapper.setState({ "isMissingFields": true })
        const formAlert = wrapper.find(FormAlert)
        expect(formAlert.exists()).toBe(true)
    })

    it("should render a FormSelectionOption component", () => {

        const wrapper = mount(<BulkOperationMetadata {...props} />)
        const formSelectionOption = wrapper.find(FormSelectOption)
        expect(formSelectionOption.exists()).toBe(true)
    })

    it("should render an InputGroup component", () => {
        const wrapper = mount(<BulkOperationMetadata {...props} />)
        const inputGroup = wrapper.find(InputGroup)
        expect(inputGroup.exists()).toBe(true)
    })

    it("test handleModalToggle function", () => {
        // render component  
        const { getByTestId, getAllByTestId, getByText, queryAllByText } = render(<BulkOperationMetadata {...props} />)

        // click "Cancel" button
        fireEvent.click(screen.getByRole('button', { name: "Cancel" }))

        // verify
        expect(queryAllByText("Bulk Edit")).toBeFalsy
        expect(queryAllByText("Changes made apply to all selected docs.")).toBeFalsy
    })

})
