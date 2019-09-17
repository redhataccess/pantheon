import React from 'react';
import { Revisions } from '@app/revisions';
import "isomorphic-fetch"

import { mount, shallow } from 'enzyme';
import { Button, Card, DataList, DataListItem, DataListItemCells, DataListItemRow, Form, FormGroup, FormSelect, FormSelectOption, InputGroup, InputGroupText, Modal, TextInput } from '@patternfly/react-core';
import renderer from 'react-test-renderer';
import sinon from 'sinon'

const anymatch = require('anymatch');

const props = {
    draftUpdateDate: anymatch,
    modulePath: "/modules/test",
    releaseUpdateDate: anymatch,
    revisionModulePath: "/modules/test"
}

describe('Revisions tests', () => {
    test('should render Revisions component', () => {
        const view = shallow(<Revisions {...props} />);
        expect(view).toMatchSnapshot();
    });

    it('should render a Button', () => {
        const wrapper = mount(<Revisions {...props} />);
        const button = wrapper.find(Button);
        expect(button.exists()).toBe(true)
    });

    it('should render a Card', () => {
        const wrapper = mount(<Revisions {...props} />);
        const card = wrapper.find(Card);
        expect(card.exists()).toBe(true)
    });

    it('should render a Data List', () => {
        const wrapper = mount(<Revisions {...props} />);
        const dataList = wrapper.find(DataList);
        expect(dataList.exists()).toBe(true)
    });

    it('should render a DataListItem', () => {
        const wrapper = mount(<Revisions {...props} />);
        const dataListItem = wrapper.find(DataListItem);
        expect(dataListItem.exists()).toBe(true)
    });

    it('should render a DataListItemCells Element', () => {
        const wrapper = mount(<Revisions {...props} />);
        const dataListItemCells = wrapper.find(DataListItemCells);
        expect(dataListItemCells.exists()).toBe(true)
    });

    it('should render a DataListItemRow element', () => {
        const wrapper = mount(<Revisions {...props} />);
        const dataListItemRow = wrapper.find(DataListItemRow);
        expect(dataListItemRow.exists()).toBe(true)
    });

    it('should render a Modal', () => {
        const wrapper = mount(<Revisions {...props} />);
        const modal = wrapper.find(Modal);
        expect(modal.exists()).toBe(true)
    });

    it('should render a Form', () => {
        const wrapper = shallow(<Revisions {...props} />);
        wrapper.setState({ 'login': true })
        wrapper.setState({ 'isModalOpen': true })
        const form = wrapper.find(Form);
        expect(form.exists()).toBe(true)
    });

    it('should render a FormGroup', () => {
        const wrapper = shallow(<Revisions {...props} />);
        wrapper.setState({ 'login': true })
        wrapper.setState({ 'isModalOpen': true })
        const formGroup = wrapper.find(FormGroup);
        expect(formGroup.exists()).toBe(true)
    });

    it('should render a FormSelect', () => {
        const wrapper = shallow(<Revisions {...props} />);
        wrapper.setState({ 'login': true })
        wrapper.setState({ 'isModalOpen': true })
        const formSelect = wrapper.find(FormSelect);
        expect(formSelect.exists()).toBe(true)
    });

    it('should render a FormSelectOption', () => {
        const wrapper = shallow(<Revisions {...props} />);
        wrapper.setState({ 'login': true })
        wrapper.setState({ 'isModalOpen': true })
        const formSelectOption = wrapper.find(FormSelectOption);
        expect(formSelectOption.exists()).toBe(true)
    });

    it('should render a InputGroup', () => {
        const wrapper = shallow(<Revisions {...props} />);
        wrapper.setState({ 'login': true })
        wrapper.setState({ 'isModalOpen': true })
        const inputGroup = wrapper.find(InputGroup);
        expect(inputGroup.exists()).toBe(true)
    });

    it('test fetchRevisions function', () => {
        const wrapper = renderer.create(<Revisions {...props} />);
        const inst = wrapper.getInstance();
        expect(inst.fetchRevisions()).toMatchSnapshot();
    });

    it('test changePublishState function', () => {
        const wrapper = renderer.create(<Revisions {...props} />);
        const inst = wrapper.getInstance();
        expect(inst.changePublishState("Publish")).toMatchSnapshot();
    });

    it('test onArchiveSelect function', () => {
        const wrapper = renderer.create(<Revisions {...props} />);
        const inst = wrapper.getInstance();
        expect(inst.onArchiveSelect()).toMatchSnapshot();
    });

    it('test onHeadingToggle function', () => {
        const wrapper = renderer.create(<Revisions {...props} />);
        const inst = wrapper.getInstance();
        expect(inst.onHeadingToggle()).toMatchSnapshot();
    });

    it('test getMetadata function', () => {
        const wrapper = shallow(<Revisions {...props} />);
        const instance = wrapper.instance();
        wrapper.setState({ 'login': true })
        wrapper.setState({ 'isModalOpen': true })
        // Assuming metadata exists
        const spy = sinon.spy(instance, 'saveMetadata');
        const urlFragment = wrapper.find('input');
        expect(urlFragment.exists()).toBe(true)

        const useCaseValue = wrapper.find('[aria-label="FormSelect Usecase"]').simulate('change', { target: { value: 'Administer' } });
        expect(useCaseValue.exists()).toBe(true)

        wrapper.setState({ 'moduleUrl': urlFragment })
        expect(wrapper.state('moduleUrl')).toBeDefined()

        wrapper.setState({ 'usecaseValue': useCaseValue })
        expect(instance.state['usecases'][0]).toEqual('Administer')
    });

});