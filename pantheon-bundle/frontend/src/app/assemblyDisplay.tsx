import React, { Component } from 'react'
import { CopyIcon } from '@patternfly/react-icons';
import { Level, LevelItem, Breadcrumb, BreadcrumbItem, Button } from '@patternfly/react-core'
import {
    DataList, DataListItem, DataListItemRow, DataListItemCells,
    DataListCell, Card, Text, TextContent, TextVariants
} from '@patternfly/react-core'
import { Versions } from '@app/versions'
import { Fields, PathPrefixes } from '@app/Constants'
import { continueStatement } from '@babel/types';

class AssemblyDisplay extends Component<any, any, any> {

    constructor(props) {
        super(props)
        this.state = {
            copySuccess: '',
            draftPath: '',
            draftUpdateDate: '',
            modulePath: '',
            moduleTitle: "",
            moduleType: '',
            variantUUID: '',
            portalHost: '',
            productValue: "",
            releasePath: '',
            releaseUpdateDate: '',
            releaseVersion: '',
            results: {},
            variant: 'DEFAULT',
            versionValue: ""
        }
    }

    public componentDidMount() {
        // this.getVariantParam()
        this.fetchModuleDetails(this.props)
        this.getVersionUUID(this.props.location.pathname)
        this.getPortalUrl()
    }

    public render() {
        // console.log('Props: ', this.props)
        return (
            <React.Fragment>
                <div>
                    <div className="app-container">
                        <Breadcrumb>
                            <BreadcrumbItem ><a href="#/search">Assemblies</a></BreadcrumbItem>
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
                            && this.state.variantUUID !== ""
                            && this.state.portalHost !== ""
                            && <span><a href={this.state.portalHost + '/topics/en-us/' + this.state.variantUUID} target="_blank">View on Customer Portal  <i className="fa pf-icon-arrow" /></a> </span>
                        }

                        <span>&emsp;&emsp;</span>

                        {this.state.releaseUpdateDate.trim() !== "" && this.state.releaseUpdateDate !== '-'
                            && this.state.variantUUID !== ""
                            && this.state.portalHost !== ""
                            && <span><a id="permanentURL" onClick={this.copyToClipboard} onMouseLeave={this.mouseLeave}>Copy permanent URL  <CopyIcon /></a></span>
                        }

                        <span>&emsp;{this.state.copySuccess !== '' && this.state.copySuccess}</span>
                    </div>
                    <br />
                    <Level gutter="md">
                        <LevelItem>{}</LevelItem>
                        <LevelItem>{}</LevelItem>
                        <LevelItem>
                        <Button variant='primary' onClick={() => this.generateDraftHtml(this.props.location.pathname)}>Generate Draft Html</Button>{'  '}
                        </LevelItem>
                    </Level>
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
                                variant={this.state.variant}
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
    private generateDraftHtml = (pathname: any) => {
        const docPath = '/content' + pathname.substring(PathPrefixes.ASSEBMLY_PATH_PREFIX.length) + '.preview?draft=true&variant=' + this.state.variant

        // console.log('Preview path: ', docPath)
        return window.open(docPath)
    }

    private updateDate = (draftDate, releaseDate, releaseVersion, variantUUID) => {
        this.setState({
            draftUpdateDate: draftDate,
            variantUUID,
            releaseUpdateDate: releaseDate,
            releaseVersion,
        })
    }

    private fetchModuleDetails = async(data) => {
        await this.getVariantParam()
        const path = data.location.pathname.substring(PathPrefixes.ASSEBMLY_PATH_PREFIX.length)
        this.setState({
            modulePath: path,
            releasePath: "/content" + path + ".preview?variant=" + this.state.variant
        })

        fetch(path + '/en_US.harray.4.json')
            .then(response => response.json())
            .then(responseJSON => {
                // console.log('fetch results:', responseJSON)
                // TODO: refactor for loops
                for (const sourceVariant of responseJSON.__children__) {
                    if (!sourceVariant.__children__) {
                        continue
                    }
                    for (const myChild of sourceVariant.__children__) {

                        if (!myChild.__children__) {
                            continue
                        }
                        if (myChild.__name__ === 'draft') {

                            this.setState({ draftUpdateDate: myChild["jcr:created"] })
                        }
                        for (const myGrandchild of myChild.__children__) {
                            if (!myGrandchild.__children__) {
                                continue
                            }

                            for (const offspring of myGrandchild.__children__) {
                                if (offspring.__name__ === 'metadata') {

                                    if (offspring[Fields.JCR_TITLE] !== undefined) {

                                        this.setState({
                                            moduleTitle: offspring[Fields.JCR_TITLE],
                                        })
                                    }
                                    if (offspring[Fields.PANT_MODULE_TYPE] !== undefined) {

                                        this.setState({
                                            moduleType: offspring[Fields.PANT_MODULE_TYPE],

                                        })
                                    }
                                }
                            }

                        }
                    }
                }

            })
    }

    private getProduct = (product) => {
        this.setState({ productValue: product })
    }

    private getVersion = (version) => {
        this.setState({ versionValue: version })
    }

    private getVersionUUID = (path) => {
        // Remove /assembly from path
        path = path.substring(PathPrefixes.ASSEBMLY_PATH_PREFIX.length)
        // path = "/content" + path + "/en_US/1/metadata.json"
        path = "/content" + path + "/en_US.harray.4.json"
        fetch(path)
            .then(response => response.json())
            .then((responseJSON) => {
                for (const locale of responseJSON.__children__) {
                    if (!locale.__children__) {
                        continue
                    }
                    for (const localeChild of locale.__children__) {

                        if (!localeChild.__children__) {
                            continue
                        }
                        for (const variant of localeChild.__children__) {
                            if (!variant.__children__) {
                                continue
                            }

                            for (const offspring of variant.__children__) {
                                if (offspring.__name__ === 'metadata') {

                                    if (offspring[Fields.PANT_PRODUCT_VERSION_REF] !== undefined) {

                                        this.getProductInitialLoad(offspring[Fields.PANT_PRODUCT_VERSION_REF])
                                    }
                                }
                            }

                        }
                    }
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
        if (this.state.variantUUID.trim() !== '') {
            textField.value = this.state.portalHost + '/topics/en-us/' + this.state.variantUUID
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

    private getPortalUrl = () => {
        fetch('/conf/pantheon/pant:portalUrl')
            .then(resp => {
                if (resp.ok) {
                    resp.text().then(text => {
                        this.setState({ portalHost: text })
                        // console.log("set portalHost: " + this.state.portalHost)
                    })
                }
            })
    }

    private async getVariantParam() {
        const query = new URLSearchParams(this.props.location.search);
        const variantParam = query.get('variant')
        // console.log("[moduleDisplay] variantParam => ", variantParam)
        if (variantParam !== 'undefined') {
            this.setState({ variant: variantParam })
        }
    }

}

export { AssemblyDisplay }
