import React, { Component } from 'react';
import {
    BaseSizes, Button, Dropdown, DropdownToggle, DropdownItem, DropdownSeparator, Form,
    FormGroup, Modal, Title, TitleLevel, TextInput
} from '@patternfly/react-core';
import { Breadcrumb, BreadcrumbItem, BreadcrumbHeading } from '@patternfly/react-core';
import { Link } from "react-router-dom";
import { Level, LevelItem } from '@patternfly/react-core';
import { Tooltip } from '@patternfly/react-core';
import {
    DataList, DataListItem, DataListItemRow, DataListItemCells,
    DataListCell, Card, Text, TextContent, TextVariants, Grid, GridItem
} from '@patternfly/react-core';
import { Revisions } from '@app/revisions';
import { HelpIcon, ThIcon, CaretDownIcon } from '@patternfly/react-icons';

export interface IProps {
    moduleName: string
    modulePath: string
    moduleType: string
    updated: string
}

class ModuleDisplay extends Component<IProps> {
    public state = {
        formInvalid: false,
        isDropdownOpen: false,
        isDup: false,
        isModalOpen: false,
        productUrl: ''
    };

    public render() {
        const { isDropdownOpen, isModalOpen, productUrl } = this.state;
        const header = (
            <React.Fragment>
                <Title headingLevel={TitleLevel.h1} size={BaseSizes["2xl"]}>
                    Edit Metadata
              </Title>
                <p className="pf-u-pl-sm">
                    All fields are required.
              </p>
            </React.Fragment>
        );
        const productDropdownItems = [
            <DropdownItem key="link">Link</DropdownItem>,
            <DropdownItem key="action" component="button">
                Action
            </DropdownItem>,
            <DropdownSeparator key="separator" />,
            <DropdownItem key="separated link">Separated Link</DropdownItem>,
            <DropdownItem key="separated action" component="button">
                Separated Action
            </DropdownItem>
        ];

        const versionDropdownItems = [
            <DropdownItem key="link">1.1</DropdownItem>,
            <DropdownItem key="action" component="button">
                1.2
            </DropdownItem>,
            <DropdownSeparator key="separator" />,
            <DropdownItem key="separated link">Separated Link</DropdownItem>,
            <DropdownItem key="separated action" component="button">
                Separated Action
            </DropdownItem>
        ];

        const documentUseCaseItems = [
            <DropdownItem key="link">Case Study</DropdownItem>,
            <DropdownItem key="action" component="button">
                Documentation
            </DropdownItem>,
            <DropdownSeparator key="separator" />,
            <DropdownItem key="separated link">Separated Link</DropdownItem>,
            <DropdownItem key="separated action" component="button">
                Separated Action
            </DropdownItem>
        ];
        return (
            <React.Fragment>
                <div>
                    <Breadcrumb>
                        <BreadcrumbItem to="#">Modules</BreadcrumbItem>
                        <BreadcrumbItem to="#" isActive={true}>
                            {this.props.moduleName}
                        </BreadcrumbItem>
                    </Breadcrumb>
                </div>
                <div>
                    <Level gutter="md">
                        <LevelItem>
                            <TextContent>
                                <Text component={TextVariants.h1}>{this.props.moduleName}{'  '}
                                    <Tooltip
                                        position="right"
                                        content={
                                            <div>Title updated in latest revision</div>
                                        }>
                                        <span><HelpIcon /></span>
                                    </Tooltip>
                                </Text>
                            </TextContent>
                        </LevelItem>
                        <LevelItem />
                        <LevelItem>
                            <Button variant="secondary" onClick={this.handleModalToggle}>Edit metadata</Button>
                        </LevelItem>
                    </Level>
                </div>
                <div>
                    <a href='http://access.redhat.com'>View on Customer Portal</a>
                </div>
                <div>
                    <DataList aria-label="single action data list example ">
                        <DataListItem aria-labelledby="simple-item1">
                            <DataListItemRow id="data-rows-header" >
                                <DataListItemCells
                                    dataListCells={[
                                        <DataListCell width={2} key="products">
                                            <span className="sp-prop-nosort" id="span-source-type">Products</span>
                                        </DataListCell>,
                                        <DataListCell key="published">
                                            <span className="sp-prop-nosort" id="span-source-type">Published</span>
                                        </DataListCell>,
                                        <DataListCell key="updated">
                                            <span className="sp-prop-nosort" id="span-source-type">Updated</span>
                                        </DataListCell>,
                                        <DataListCell key="module_type">
                                            <span className="sp-prop-nosort" id="span-source-name">Module Type</span>
                                        </DataListCell>
                                    ]}
                                />
                            </DataListItemRow>

                            <DataListItemRow>
                                <DataListItemCells
                                    dataListCells={[
                                        <DataListCell width={2} key="products">
                                            <span>Dummy Product Name</span>
                                        </DataListCell>,
                                        <DataListCell key="published">
                                            <span>Dummy Publish</span>
                                        </DataListCell>,
                                        <DataListCell key="updated">
                                            <span>{this.props.updated}</span>
                                        </DataListCell>,
                                        <DataListCell key="module_type">
                                            <span>{this.props.moduleType}</span>
                                        </DataListCell>,
                                    ]}
                                />
                            </DataListItemRow>
                            ))}
                    </DataListItem>
                    </DataList>
                </div>
                <div>
                    <Card>
                        <Revisions
                            modulePath={this.props.modulePath}
                            revisionModulePath={this.props.moduleName}
                        />
                    </Card>
                </div>
                <Modal
                    width={'50%'}
                    title="Edit metadata"
                    isOpen={isModalOpen}
                    header={header}
                    ariaDescribedById="edit-metadata"
                    onClose={this.handleModalToggle}
                    actions={[
                        <Button key="confirm" variant="primary" onClick={this.handleModalToggle}>
                            Save
          </Button>,
                        <Button key="cancel" variant="secondary" onClick={this.handleModalToggle}>
                            Cancel
            </Button>
                    ]}
                >
                    <Form isHorizontal={true}>
                        <FormGroup
                            label="Product Name"
                            isRequired={true}
                            fieldId="product-name"
                        >
                            <Dropdown
                                onSelect={this.onSelect}
                                toggle={<DropdownToggle onToggle={this.onToggle} iconComponent={CaretDownIcon}>Dropdown</DropdownToggle>}
                                isOpen={isDropdownOpen}
                                dropdownItems={productDropdownItems}
                            />
                            <Dropdown
                                onSelect={this.onSelect}
                                toggle={<DropdownToggle onToggle={this.onToggle} iconComponent={CaretDownIcon}>Dropdown</DropdownToggle>}
                                isOpen={isDropdownOpen}
                                dropdownItems={versionDropdownItems}
                            />
                        </FormGroup>
                        <FormGroup
                            label="Document Usecase"
                            isRequired={true}
                            fieldId="document-usecase"
                        >
                            <Dropdown
                                onSelect={this.onSelect}
                                toggle={<DropdownToggle onToggle={this.onToggle} iconComponent={CaretDownIcon}>Dropdown</DropdownToggle>}
                                isOpen={isDropdownOpen}
                                dropdownItems={documentUseCaseItems}
                            />
                        </FormGroup>
                        <FormGroup
                            label="Vanity URL Fragment"
                            isRequired={true}
                            fieldId="url-fragment"
                        >
                            <div><span>/</span><TextInput isRequired={true} id="url-fragment" type="text" placeholder="URL Fragment" value={productUrl} onChange={this.handleURLInput} /></div>
                        </FormGroup>
                    </Form>
                </Modal>
            </React.Fragment>

        );
    }


    private handleModalToggle = () => {
        this.setState({
            isModalOpen: !this.state.isModalOpen
        });
    }
    private onSelect = event => {
        this.setState({
            isDropdownOpen: !this.state.isDropdownOpen
        });
    }

    private onToggle = isDropdownOpen => {
        this.setState({
            isDropdownOpen
        });
    }

    private handleURLInput = productUrl => {
        this.setState({ productUrl });

        // check for duplcated product URL.
        // this.productUrlExist(this.state.productUrl);
        if (this.state.isDup) {
            this.setState({ formInvalid: true });
        }
    }
}

export { ModuleDisplay }