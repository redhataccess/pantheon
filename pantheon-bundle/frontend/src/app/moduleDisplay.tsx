import React, { Component } from 'react'
import { Level, LevelItem, Breadcrumb, BreadcrumbItem } from '@patternfly/react-core'
import {
    DataList, DataListItem, DataListItemRow, DataListItemCells,
    DataListCell, Card, Text, TextContent, TextVariants
} from '@patternfly/react-core'
import { Versions } from '@app/versions'
import { Fields } from '@app/Constants'
import CopyImage from '@app/images/copy.png'

class ModuleDisplay extends Component<any, any, any> {

    constructor(props) {
        super(props)
        this.state = {
            copySuccess: '',
            draftPath: '',
            draftUpdateDate: '',
            modulePath: '',
            moduleTitle: "",
            moduleType: '',
            moduleUUID: '',
            productValue: "",
            releasePath: '',
            releaseUpdateDate: '',
            releaseVersion: '',
            results: {},
            versionValue: ""
        }
    }

    public componentDidMount() {
        this.fetchModuleDetails(this.props)
        this.getVersionUUID(this.props.location.pathname)
    }

    public render() {
        // console.log('Props: ', this.props)
        return (
            <React.Fragment>
                <div>
                    <div className="app-container">
                        <Breadcrumb>
                            <BreadcrumbItem ><a href="#/search">Modules</a></BreadcrumbItem>
                            <BreadcrumbItem to="#" isActive={true}>{this.state.moduleTitle}</BreadcrumbItem>
                        </Breadcrumb>
                    </div>
                    <br />
                    <div>
                        <Level gutter="md">
                            <LevelItem>
                                <TextContent>
                                    <Text component={TextVariants.h1}>{this.state.moduleTitle}</Text>
                                </TextContent>
                            </LevelItem>
                            <LevelItem />
                        </Level>
                    </div>
                    <div>
                        {this.state.releaseUpdateDate.trim() !== "" && this.state.releaseUpdateDate !== '-'
                            && this.state.moduleUUID !== ""
                            && <span><a href={'https://access.redhat.com/topics/en-us/' + this.state.moduleUUID} target="_blank">View on Customer Portal  <i className="fa pf-icon-arrow" /></a> </span>
                        }

                        <span>&emsp;&emsp;</span>

                        {this.state.releaseUpdateDate.trim() !== "" && this.state.releaseUpdateDate !== '-'
                            && this.state.moduleUUID !== ""
                            && <span><a id="permanentURL" onClick={this.copyToClipboard} onMouseLeave={this.mouseLeave}>Copy permanent URL  <img src={CopyImage} width="16px" height="16px" /></a></span>
                        }

                        <span>&emsp;{this.state.copySuccess !== '' && this.state.copySuccess}</span>
                    </div>
                    <br />
                    <div>
                        <DataList aria-label="single action data list">
                            <DataListItem aria-labelledby="simple-item1">
                                <DataListItemRow id="data-rows-header" >
                                    <DataListItemCells
                                        dataListCells={[
                                            <DataListCell width={2} key="product">
                                                <span className="sp-prop-nosort" id="span-source-type-product">Product</span>
                                            </DataListCell>,
                                            <DataListCell key="published">
                                                <span className="sp-prop-nosort" id="span-source-type-published">Published</span>
                                            </DataListCell>,
                                            <DataListCell key="updated">
                                                <span className="sp-prop-nosort" id="span-source-type-draft-uploaded">Draft Uploaded</span>
                                            </DataListCell>,
                                            <DataListCell key="module_type">
                                                <span className="sp-prop-nosort" id="span-source-name-module-type">Module Type</span>
                                            </DataListCell>
                                        ]}
                                    />
                                </DataListItemRow>
                                <DataListItemRow>
                                    <DataListItemCells
                                        dataListCells={[
                                            <DataListCell width={2} key="product">
                                                <span>{this.state.productValue + ' ' + this.state.versionValue}</span>
                                            </DataListCell>,
                                            <DataListCell key="published">
                                                <span>
                                                    {this.state.releaseUpdateDate.trim() !== ""
                                                        && this.state.releaseUpdateDate.length >= 15 ?
                                                        this.state.releaseUpdateDate : "-"} <br />
                                                    {this.state.releaseUpdateDate.trim() !== "" &&
                                                        <a href={this.state.releasePath} target="_blank">{this.state.releaseVersion}</a>}
                                                </span>
                                            </DataListCell>,
                                            <DataListCell key="updated">
                                                <span>
                                                    {this.state.draftUpdateDate.trim() !== ""
                                                        && this.state.draftUpdateDate.length >= 15 ?
                                                        this.state.draftUpdateDate : "-"}
                                                </span>
                                            </DataListCell>,
                                            <DataListCell key="module_type">
                                                <span>{this.state.moduleType !== undefined ? this.state.moduleType : "-"}</span>
                                            </DataListCell>,
                                        ]}
                                    />
                                </DataListItemRow>
                            </DataListItem>
                        </DataList>
                    </div>
                    <div>
                        <Card>
                            <Versions
                                modulePath={this.state.modulePath}
                                productInfo={this.state.productValue}
                                versionModulePath={this.state.moduleTitle}
                                updateDate={this.updateDate}
                                onGetProduct={this.getProduct}
                                onGetVersion={this.getVersion}
                            />
                        </Card>
                    </div>
                </div>
            </React.Fragment>
        )
    }

    private updateDate = (draftDate, releaseDate, releaseVersion, moduleUUID) => {
        this.setState({
            draftUpdateDate: draftDate,
            moduleUUID,
            releaseUpdateDate: releaseDate,
            releaseVersion,
        })
    }

    private fetchModuleDetails = (data) => {
        this.setState({
            modulePath: data.location.pathname,
            releasePath: "/content" + data.location.pathname + ".preview"
        })

        fetch(data.location.pathname + '.4.json')
            .then(response => response.json())
            .then(responseJSON => {
                // console.log('fetch results:', responseJSON)
                this.setState({
                    moduleTitle: responseJSON.en_US["1"].metadata["jcr:title"],
                    moduleType: responseJSON.en_US["1"].metadata["pant:moduleType"],

                })
            })
    }

    private getProduct = (product) => {
        this.setState({ productValue: product })
    }

    private getVersion = (version) => {
        this.setState({ versionValue: version })
    }

    private getVersionUUID = (path) => {
        path = "/content" + path + "/en_US/1/metadata.json"
        fetch(path)
            .then(response => response.json())
            .then((responseJSON) => {
                if (responseJSON.productVersion !== undefined) {
                    this.getProductInitialLoad(responseJSON.productVersion)
                }
            })
    }

    private getProductInitialLoad = (uuid) => {
        const path = '/content/products.harray.3.json'
        fetch(path)
            .then(response => response.json())
            .then(responseJSON => {
                for (const product of responseJSON.__children__) {
                    if (!product.__children__) {
                        continue
                    }
                    for (const productChild of product.__children__) {
                        if (productChild.__name__ !== 'versions') {
                            continue
                        }
                        for (const productVersion of productChild.__children__) {
                            if (productVersion[Fields.JCR_UUID] === uuid) {
                                this.setState({ productValue: product.name, versionValue: productVersion.name })
                                break
                            }
                        }
                    }
                }
            })
    }

    private copyToClipboard = () => {
        const textField = document.createElement('textarea')
        if (this.state.moduleUUID.trim() !== '') {
            textField.value = 'https://access.redhat.com/topics/en-us/' + this.state.moduleUUID
            document.body.appendChild(textField)
            textField.select()
            document.execCommand('copy')
            textField.remove()
            this.setState({ copySuccess: 'Copied!' })
        }
    }

    private mouseLeave = () => {
        this.setState({ copySuccess: '' })
    }
}

export { ModuleDisplay }