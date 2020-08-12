import React, { Component } from 'react'
import { CopyIcon } from '@patternfly/react-icons';
import { Level, LevelItem, Button, Divider, Title, TextList, TextListItem, TextListVariants, TextListItemVariants } from '@patternfly/react-core'
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
                    <Level>
                        <LevelItem>
                            <Title headingLevel="h1" size="xl">{this.state.moduleTitle}</Title>
                        </LevelItem>
                        <LevelItem />
                    </Level>
                    <Level>
                        <LevelItem>
                            <TextContent>
                                <Text component={TextVariants.small}>Assembly</Text>
                            </TextContent>
                        </LevelItem>
                        <LevelItem />
                    </Level>

                    <Level>
                        <LevelItem>
                            <TextContent>
                                <Text component="a">{this.props.location.pathname.substring(PathPrefixes.ASSEBMLY_PATH_PREFIX.length)}</Text>
                            </TextContent>
                        </LevelItem>
                        <LevelItem />
                        <LevelItem>
                            {this.state.releaseUpdateDate.trim() !== "" && this.state.releaseUpdateDate !== '-'
                                && this.state.variantUUID !== ""
                                && this.state.portalHost !== ""
                                && <span><a href={this.state.portalHost + '/topics/en-us/' + this.state.variantUUID} target="_blank">View on Customer Portal  <i className="fa pf-icon-arrow" /></a> </span>
                            }
                        </LevelItem>
                        <LevelItem>
                            {this.state.releaseUpdateDate.trim() !== "" && this.state.releaseUpdateDate !== '-'
                                && this.state.variantUUID !== ""
                                && this.state.portalHost !== ""
                                && <span><a id="permanentURL" onClick={this.copyToClipboard} onMouseLeave={this.mouseLeave}>Copy permanent URL  <CopyIcon /></a></span>
                            }

                            <span>&emsp;{this.state.copySuccess !== '' && this.state.copySuccess}</span>

                        </LevelItem>
                    </Level>
                    <br />
                    <Level>
                        <LevelItem>
                            <TextContent>
                                <Text><strong>Product</strong></Text>
                            </TextContent>
                        </LevelItem>
                        <LevelItem>{}</LevelItem>
                        <LevelItem>
                            <TextContent>
                                <Text><strong>Draft Uploaded</strong></Text>
                            </TextContent>
                        </LevelItem>
                        <LevelItem>
                            <TextContent>
                                <Text><strong>Published</strong></Text>
                            </TextContent>
                        </LevelItem>
                    </Level>

                    <Level>
                        <LevelItem><span>{this.state.productValue + ' ' + this.state.versionValue}</span></LevelItem>
                        <LevelItem>{}</LevelItem>
                        <LevelItem className="pf-u-text-align-left">
                            <span>
                                {this.state.draftUpdateDate.trim() !== ""
                                    && this.state.draftUpdateDate.length >= 15 ?
                                    new Intl.DateTimeFormat('en-GB', { year: 'numeric', month: 'long', day: 'numeric' }).format(new Date(this.state.draftUpdateDate)) : "--"}
                            </span>
                        </LevelItem>
                        <LevelItem className="pf-u-text-align-left"><span>
                            {this.state.releaseUpdateDate.trim() !== ""
                                && this.state.releaseUpdateDate.length >= 15 ?
                                new Intl.DateTimeFormat('en-GB', { year: 'numeric', month: 'long', day: 'numeric' }).format(new Date(this.state.releaseUpdateDate)) : "--"}
                            {this.state.releaseUpdateDate.trim() !== ""
                                && this.state.releaseUpdateDate.length >= 15 ?
                                <a href={this.state.releasePath} target="_blank">{this.state.releaseVersion}</a> : ""}
                        </span>
                        </LevelItem>
                    </Level>

                    <br />
                    <Level>
                        <LevelItem>{}</LevelItem>
                        <LevelItem>{}</LevelItem>
                        <LevelItem>
                            <Button variant='primary' onClick={() => this.generateDraftHtml(this.props.location.pathname)}>Generate Draft Html</Button>{'  '}
                        </LevelItem>
                    </Level>
                    <br />

                    <Divider />
                    <br />
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
        const docPath = '/content' + pathname.substring(PathPrefixes.ASSEBMLY_PATH_PREFIX.length) + '.preview?draft=true&rerender=true&variant=' + this.state.variant

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

    private fetchModuleDetails = async (data) => {
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
