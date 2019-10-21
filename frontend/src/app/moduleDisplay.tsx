import React, { Component } from 'react';
import { Level, LevelItem, Breadcrumb, BreadcrumbItem } from '@patternfly/react-core';
import {
    DataList, DataListItem, DataListItemRow, DataListItemCells,
    DataListCell, Card, Text, TextContent, TextVariants
} from '@patternfly/react-core';
import { Versions } from '@app/versions';

class ModuleDisplay extends Component<any, any, any> {

    constructor(props) {
        super(props)
        this.state = {
            copySuccess: '',
            draftPath: '',
            draftUpdateDate: '',
            modulePath: '',
            moduleTitle: "",
            moduleUUID: '',
            productValue: "",
            releasePath: '',
            releaseUpdateDate: '',
            releaseVersion: '',
            moduleType: '',
            results: {},
            versionUUID: "",
            versionValue: ""
        };
    }

    public componentDidMount() {
        this.fetchModuleDetails(this.props)
        this.getVersionUUID(this.props.location.pathname)
    }

    public render() {
        // console.log('Props: ', this.props);
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
                            && <span><a id="permanentURL" onClick={this.copyToClipboard}>Copy permanent URL  <i className="fa pf-icon-folder-close" /></a></span>
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
                                versionModulePath={this.state.moduleTitle}
                                updateDate={this.updateDate}
                                onGetProduct={this.getProduct}
                                onGetVersion={this.getVersion}
                            />
                        </Card>
                    </div>
                </div>
            </React.Fragment>
        );
    }

    private updateDate = (draftDate, releaseDate, releaseVersion, moduleUUID) => {
        this.setState({
            draftUpdateDate: draftDate,
            moduleUUID,
            releaseUpdateDate: releaseDate,
            releaseVersion,
        });
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
        // console.log("[getProduct] ", product)
        this.setState({ productValue: product })
    }

    private getVersion = (version) => {
        // console.log("[getVersion] ", version)
        this.setState({ versionValue: version })
    }

    private getVersionUUID = (path) => {
        // console.log("[getVersionUUID] path", path)
        path = "/content" + path + "/en_US/1/metadata.json"
        fetch(path)
            .then(response => response.json())
            .then((responseJSON) => {
                // console.log("[responseJSON]", responseJSON)
                if (responseJSON.productVersion !== undefined) {
                    // console.log("[allproducts inside fetch call] ", this.state.allProducts)

                    // const versionUUID = responseJSON["productVersion"]
                    this.setState({ versionUUID: responseJSON.productVersion }, () => {
                        this.getProductInitialLoad()
                    })
                }
            })

    }

    private getProductInitialLoad = () => {
        const path = '/content/products.3.json'
        let key
        fetch(path)
            .then(response => response.json())
            .then(responseJSON => {
                // console.log('fetch results:',responseJSON)
                // tslint:disable-next-line: prefer-for-of
                for (let i = 0; i < Object.keys(responseJSON).length; i++) {
                    key = Object.keys(responseJSON)[i];
                    const versionKey = "versions"
                    if ((key !== 'jcr:primaryType')) {
                        if (responseJSON[key].name !== undefined) {
                            const pName = responseJSON[key].name
                            const versionObj = responseJSON[key][versionKey]
                            if (versionObj) {
                                // console.log("[getProductFromUUID] versionObj ", versionObj)
                                let vKey;
                                const versions = new Array();
                                // tslint:disable-next-line: no-shadowed-variable
                                for (const item in Object.keys(versionObj)) {
                                    if (Object.keys(versionObj)[item] !== undefined) {
                                        vKey = Object.keys(versionObj)[item]
                                        if (vKey !== 'jcr:primaryType') {
                                            if (versionObj[vKey].name) {
                                                if (versionObj[vKey]["jcr:uuid"] === this.state.versionUUID) {
                                                    // process productValue and versionValue on initial load
                                                    this.setState({ productValue: pName, versionValue: versionObj[vKey].name }, () => {
                                                        // console.log("[getProductFromUUID] item/productvalue", pName)
                                                        // console.log("[getProductFromUUID] versionValue", versionObj[vKey][nameKey])
                                                    })
                                                    break;
                                                }

                                            }
                                        }
                                    }
                                }

                            }

                        }
                    }
                }

            })
    }

    private copyToClipboard = () => {
        const textField = document.createElement('textarea')
        if (window.location.href !== undefined) {
            const targetHref = window.location.href
            if (window.location.pathname !== undefined) {
                textField.innerText = targetHref.split(window.location.pathname)[0] + this.state.releasePath
                document.body.appendChild(textField)
                textField.select()
                document.execCommand('copy')
                textField.remove()
                this.setState({ copySuccess: 'Copied!' });
            }
        }
    };
}

export { ModuleDisplay }