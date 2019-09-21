import React from 'react';
import { ModuleDisplay } from '@app/moduleDisplay';
import "isomorphic-fetch"

import { mount, shallow } from 'enzyme';
import { Breadcrumb, Button, Card, DataList, DataListItem, DataListItemCells, DataListItemRow, DataListCell, TextContent, Level, LevelItem } from '@patternfly/react-core';
import renderer from 'react-test-renderer';
import sinon from 'sinon'
import { Revisions } from './revisions';

const props = {
    location: { pathname: "module/test" }
}

describe('ModuleDisplay tests', () => {
    test('should render ModuleDisplay component', () => {
        const view = shallow(<ModuleDisplay {...props} />);
        expect(view).toMatchSnapshot();
    });

    it('should render a Button', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const button = wrapper.find(Button);
        expect(button.exists()).toBe(true)
    });

    it('should render a Card', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const card = wrapper.find(Card);
        expect(card.exists()).toBe(true)
    });

    it('should render a Data List', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const dataList = wrapper.find(DataList);
        expect(dataList.exists()).toBe(true)
    });

    it('should render a DataListItem', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const dataListItem = wrapper.find(DataListItem);
        expect(dataListItem.exists()).toBe(true)
    });

    it('should render a DataListItemCells Element', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const dataListItemCells = wrapper.find(DataListItemCells);
        expect(dataListItemCells.exists()).toBe(true)
    });

    it('should render a DataListItemRow element', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const dataListItemRow = wrapper.find(DataListItemRow);
        expect(dataListItemRow.exists()).toBe(true)
    });

    it('should render a DataListCell', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const dataListCell = wrapper.find(DataListCell);
        expect(dataListCell.exists()).toBe(true)
        // console.log("[DataListCell] length", dataListCell.length)
        expect(dataListCell.at(0).contains("Products")).toBe(true)
    });

    it('should render a TextContent Element', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const textContent = wrapper.find(TextContent);
        expect(textContent.exists()).toBe(true)
    });

    it('should render a Level Element', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const level = wrapper.find(Level);
        expect(level.exists()).toBe(true)
    });

    it('should render a LevelItem', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const levelItem = wrapper.find(LevelItem);
        expect(levelItem.exists()).toBe(true)
    });

    it('should render a Revisions Element', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const revisions = wrapper.find(Revisions);
        expect(revisions.exists()).toBe(true)
    });

    it('test fetchModuleDetails function', () => {
        const wrapper = renderer.create(<ModuleDisplay {...props} />);
        const inst = wrapper.getInstance();
        expect(inst.fetchModuleDetails(props)).toMatchSnapshot();
    });

    it('test getProduct function', () => {
        const wrapper = renderer.create(<ModuleDisplay {...props} />);
        const inst = wrapper.getInstance();
        expect(inst.getProduct()).toMatchSnapshot();
    });

    it('test getVersion function', () => {
        const wrapper = renderer.create(<ModuleDisplay {...props} />);
        const inst = wrapper.getInstance();
        expect(inst.getVersion()).toMatchSnapshot();
    });

    it('test getVersionUUID function', () => {
        const wrapper = renderer.create(<ModuleDisplay {...props} />);
        const inst = wrapper.getInstance();
        expect(inst.getVersionUUID("/modules/test")).toMatchSnapshot();
    });

    it('test getProductInitialLoad function', () => {
        const wrapper = renderer.create(<ModuleDisplay {...props} />);
        const inst = wrapper.getInstance();
        expect(inst.getProductInitialLoad()).toMatchSnapshot();
    });

    it('should handle state changes for initialLoad', () => {
        const wrapper = shallow(<ModuleDisplay {...props} />)

        expect(wrapper.state('initialLoad')).toBe(true)
        wrapper.setState({ 'initialLoad': false })
        expect(wrapper.state('initialLoad')).toBe(false)
    });

    it('has a props', () => {
        const moduleDisplay = mount(<ModuleDisplay {...props} />).matchesElement
        expect(moduleDisplay.length === 1)
    });

    // Value testing with Enzyme.
    it('renders Products heading', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const sourceTypeText = wrapper.find('#span-source-type-products').first().text();

        // ensure it matches what is expected
        expect(sourceTypeText).toEqual("Products");
    });

    it('renders Published heading', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const sourceTypeText = wrapper.find('#span-source-type-published').first().text();

        // ensure it matches what is expected
        expect(sourceTypeText).toEqual("Published");
    });

    it('renders Draft Uploaded heading', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const sourceTypeText = wrapper.find('#span-source-type-draft-uploaded').first().text();

        // ensure it matches what is expected
        expect(sourceTypeText).toEqual("Draft Uploaded");
    });

    it('renders Module Type heading', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const sourceTypeText = wrapper.find('#span-source-name-module-type').first().text();

        // ensure it matches what is expected
        expect(sourceTypeText).toEqual("Module Type");
    });

    it('renders View on Customer Portal hotlink', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const sourceTypeText = wrapper.find('a').first().text();

        // ensure it matches what is expected
        expect(sourceTypeText).toEqual("View on Customer Portal");
    });
});