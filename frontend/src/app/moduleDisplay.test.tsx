import React from 'react';
import { ModuleDisplay } from '@app/moduleDisplay';
import "isomorphic-fetch"

import { mount, shallow } from 'enzyme';
import { Breadcrumb, Button, Card, DataList, DataListItem, DataListItemCells, DataListItemRow, FormGroup, FormSelect, Modal, TextInput } from '@patternfly/react-core';
import renderer from 'react-test-renderer';
import sinon from 'sinon'

const props = {
    location: {pathname: "module/test"}
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
 });