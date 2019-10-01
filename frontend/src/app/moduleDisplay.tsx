import React, { Component } from 'react';
import { Level, LevelItem } from '@patternfly/react-core';
import {
    DataList, DataListItem, DataListItemRow, DataListItemCells,
    DataListCell, Card, Text, TextContent, TextVariants
} from '@patternfly/react-core';
import { Revisions } from '@app/revisions';

class ModuleDisplay extends Component<any, any, any> {

    constructor(props) {
        super(props)
        this.state = {
            draftPath: '',
            draftUpdateDate: '',
            modulePath: '',
            moduleTitle: "",
            productValue: "",
            releasePath: '',
            releaseUpdateDate: '',
            resourceType: '',
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
                            <a href='http://access.redhat.com'>View on Customer Portal</a>
                        </div>
                        <div>
                            <DataList aria-label="single action data list">
                                <DataListItem aria-labelledby="simple-item1">
                                    <DataListItemRow id="data-rows-header" >
                                        <DataListItemCells
                                            dataListCells={[
                                                <DataListCell width={2} key="products">
                                                    <span className="sp-prop-nosort" id="span-source-type-products">Products</span>
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
                                                <DataListCell width={2} key="products">
                                                    <span>{this.state.productValue + ' ' + this.state.versionValue}</span>
                                                </DataListCell>,
                                                <DataListCell key="published">
                                                    <span>
                                                        {this.state.releaseUpdateDate.trim() !== ""
                                                            && this.state.releaseUpdateDate.length >= 15 ?
                                                            this.state.releaseUpdateDate.substring(4, 15) : "-"}
                                                    </span>
                                                </DataListCell>,
                                                <DataListCell key="updated">
                                                    <span>
                                                        {this.state.draftUpdateDate.trim() !== ""
                                                            && this.state.draftUpdateDate.length >= 15 ?
                                                            this.state.draftUpdateDate.substring(4, 15) : "-"}
                                                    </span>
                                                </DataListCell>,
                                                <DataListCell key="module_type">
                                                    <span>{this.state.resourceType}</span>
                                                </DataListCell>,
                                            ]}
                                        />
                                    </DataListItemRow>
                                    ))
                                </DataListItem>
                            </DataList>
                        </div>
                        <div>
                            <Card>
                                <Revisions
                                    modulePath={this.state.modulePath}
                                    revisionModulePath={this.state.moduleTitle}
                                    draftUpdateDate={this.updateDate}
                                    releaseUpdateDate={this.updateDate}
                                    onGetProduct={this.getProduct}
                                    onGetVersion={this.getVersion}
                                />
                            </Card>
                        </div>
                    </div>
            </React.Fragment>
        );
    }

    private updateDate = (date, type, path) => {
        if (type === "draft") {
            this.setState({
                draftUpdateDate: date,
                // tslint:disable-next-line: object-literal-sort-keys
                draftPath: path
            });
        }
        else {
            this.setState({
                releaseUpdateDate: date,
                // tslint:disable-next-line: object-literal-sort-keys
                releasePath: path
            });
        }

    };

    private fetchModuleDetails = (data) => {
        this.setState({ modulePath: data.location.pathname })

        fetch(data.location.pathname + '.4.json')
            .then(response => response.json())
            .then(responseJSON => {
                // console.log('fetch results:',responseJSON["en_US"])
                this.setState({
                    moduleTitle: responseJSON.en_US["1"].metadata["jcr:title"],
                    resourceType: responseJSON["sling:resourceType"],
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
}

export { ModuleDisplay }