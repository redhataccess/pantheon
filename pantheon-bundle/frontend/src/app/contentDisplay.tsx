import React, { Component } from "react"
import { CopyIcon, InfoCircleIcon } from "@patternfly/react-icons";
import {
    Card, Text, TextContent, TextVariants, Level, LevelItem, Button, Divider, Title, Tooltip
} from "@patternfly/react-core"

import { Versions } from "@app/versions"
import { Fields, PathPrefixes, PantheonContentTypes } from "@app/Constants"

export interface IContentDisplayState {
    attributesFilePath: string
    copySuccess: string
    draftPath: string
    modulePath: string
    moduleTitle: string
    moduleType: string
    portalUrl: string
    portalUrlType: string
    productValue: string
    releasePath: string
    releaseVersion: string
    results: any
    variant: string
    variantUUID: string
    versionValue: string
    portalHostUrl: string
    productUrlFragment: string
    versionUrlFragment: string
    locale: string
    assemblyData: any
    firstPublishDate: string
    lastPublishDate: string
    regeneratePortalUrl: boolean
}

export interface IModuleDisplayState extends IContentDisplayState {
    assemblyTitle: string
    assemblyPath: string
}

export interface IAssemblyDisplayState extends IContentDisplayState { }

class ContentDisplay extends Component<any, IModuleDisplayState | IAssemblyDisplayState> {
    isAssembly: any;

    constructor(props) {
        super(props)
        this.state = {
            attributesFilePath: "",
            copySuccess: "",
            draftPath: "",
            modulePath: "",
            moduleTitle: "",
            moduleType: "",
            portalUrl: "",
            portalUrlType: "",
            productValue: "",
            releasePath: "",
            releaseVersion: "",
            results: {},
            variant: "DEFAULT",
            variantUUID: "",
            versionValue: "",
            assemblyData: [],
            assemblyTitle: "",
            assemblyPath: "",
            portalHostUrl: "",
            productUrlFragment: "",
            versionUrlFragment: "",
            locale: "",
            firstPublishDate: "",
            lastPublishDate: "",
            regeneratePortalUrl: false
        }
    }

    public componentDidMount() {
        //set var to determine if this component will render assembly or module details
        this.isAssembly = this.props.match.path.includes('assembly')
        this.fetchModuleDetails(this.props)
        this.getVersionUUID(this.props.location.pathname)
        this.fetchAttributesFilePath(this.props)
        this.getPortalHostUrl()
        this.getLocale(this.props.location.pathname)

    }

    public componentDidUpdate(prevProps, prevState) {
        if (prevState.regeneratePortalUrl !== this.state.regeneratePortalUrl) {
            this.getPortalUrl(this.state.modulePath, this.state.variant)
            if(this.state.regeneratePortalUrl === true){
                this.setState({regeneratePortalUrl: false})
            }
        }
    }

    public render() {
        return (
            <React.Fragment>
                <Level>
                    <LevelItem>
                        <Title headingLevel="h1" size="xl">{this.state.moduleTitle}</Title>
                    </LevelItem>
                    <LevelItem />
                </Level>
                <Level>
                    <LevelItem>
                        <TextContent>
                            <Text component={TextVariants.small}>{this.isAssembly ? 'Assembly' : 'Module'}</Text>
                        </TextContent>
                    </LevelItem>
                    <LevelItem />
                </Level>
                <Level>
                    <LevelItem>
                        <TextContent>
                            <Text component={TextVariants.pre}>{this.props.location.pathname.substring(this.isAssembly ? PathPrefixes.ASSEBMLY_PATH_PREFIX.length : PathPrefixes.MODULE_PATH_PREFIX.length)}</Text>
                        </TextContent>
                    </LevelItem>
                    <LevelItem />
                    <LevelItem>
                        {this.state.variantUUID !== ""
                            && this.state.portalUrl !== ""
                            && this.state.portalUrlType === "LIVE"
                            && <span><a href={this.state.portalUrl} target="_blank">View on Customer Portal  <i className="fa pf-icon-arrow" /></a> </span>
                        }
                        {this.state.variantUUID !== ""
                            && this.state.portalUrl !== ""
                            && this.state.portalUrlType === "PRELIVE"
                            && <span><a href={this.state.portalUrl} target="_blank">Pre-live Customer Portal URL</a> </span>
                        }
                    </LevelItem>
                    <LevelItem>
                        {this.state.variantUUID !== ""
                            && this.state.portalUrl !== ""
                            && this.state.portalUrlType === "LIVE"
                            && <span><a id="permanentURL" onClick={this.copyToClipboard} onMouseLeave={this.mouseLeave}>Copy permanent URL  <CopyIcon /></a></span>
                        }
                        {this.state.variantUUID !== ""
                            && this.state.portalUrl !== ""
                            && this.state.portalUrlType === "PRELIVE"
                            && <span><a id="permanentURL" onClick={this.copyToClipboard} onMouseLeave={this.mouseLeave}>Copy pre-live URL  <CopyIcon /></a></span>
                        }
                        {this.state.variantUUID !== ""
                            && this.state.portalUrl !== ""
                            && this.state.portalUrlType === "ERROR"
                            && <span><div><Tooltip position="top" content={<div>NO URL; {this.state.portalUrl}</div>}><InfoCircleIcon /></Tooltip></div></span>
                        }

                        <span>&emsp;{this.state.copySuccess !== "" && this.state.copySuccess}</span>

                    </LevelItem>
                </Level>
                <br />
                <Level>
                    <LevelItem>
                        <TextContent>
                            <Text>
                                <strong><span id="span-source-type-product">Product</span></strong>
                                <br />
                                <span>{this.state.productValue + " " + this.state.versionValue}</span>
                            </Text>
                        </TextContent>
                    </LevelItem>
                    <LevelItem>{ }</LevelItem>

                    {!this.isAssembly && <LevelItem>
                        <TextContent>
                            <Text>
                                <strong><span id="span-source-name-module-type">Module type</span></strong>
                                <br />
                                <span>
                                    {this.state.moduleType.trim() !== "" ?
                                        this.state.moduleType : ""}
                                </span>
                            </Text>
                        </TextContent>
                    </LevelItem>}
                    <LevelItem>
                        <TextContent>
                            <Text>
                                <strong><span id="span-source-type-firstpublished">First Published Date</span></strong>
                                <br />
                                <span>
                                    {this.state.firstPublishDate !== undefined && this.state.firstPublishDate.trim() !== ""
                                        && this.state.firstPublishDate.length >= 15 ?
                                        new Intl.DateTimeFormat("en-GB", { year: "numeric", month: "long", day: "numeric" }).format(new Date(this.state.firstPublishDate)) : "--"}
                                </span>
                            </Text>
                        </TextContent>
                    </LevelItem>
                    <LevelItem>
                        <TextContent>
                            <Text>
                                <strong><span id="span-source-type-lastpublished">Last Published Date</span></strong>
                                <br />
                                <span>
                                    {this.state.lastPublishDate != undefined && this.state.lastPublishDate.trim() !== ""
                                        && this.state.lastPublishDate.length >= 15 ?
                                        new Intl.DateTimeFormat("en-GB", { year: "numeric", month: "long", day: "numeric" }).format(new Date(this.state.lastPublishDate)) : "--"}
                                </span>
                            </Text>
                        </TextContent>
                    </LevelItem>
                </Level>

                <br />
                <Level>
                    <LevelItem>{ }</LevelItem>
                    <LevelItem>{ }</LevelItem>
                    <LevelItem>
                        <Button variant="secondary" onClick={() => this.generateDraftHtml(this.props.location.pathname)}>Generate Draft Html</Button>{"  "}
                    </LevelItem>
                </Level>
                <br />

                <Divider />
                <br />

                <Card>
                    <Versions
                        contentType={this.isAssembly ? PantheonContentTypes.ASSEMBLY : PantheonContentTypes.MODULE}
                        modulePath={this.state.modulePath}
                        productInfo={this.state.productValue}
                        versionModulePath={this.state.moduleTitle}
                        variant={this.state.variant}
                        variantUUID={this.state.variantUUID}
                        attributesFilePath={this.state.attributesFilePath}
                        assemblies={this.state.assemblyData}
                        updateDate={this.updateDate}
                        onGetUrl={this.onGetUrl}
                        onGetProduct={this.getProduct}
                        onGetVersion={this.getVersion}
                        canRegeneratePortalUrl={this.canRegeneratePortalUrl}
                    />
                </Card>


            </React.Fragment>
        )
    }
    private generateDraftHtml = (pathname: any) => {
        const docPath = "/pantheon/preview/latest/" + this.state.variantUUID + "?rerender=true"

        return window.open(docPath)
    }

    private updateDate = (releaseVersion, variantUUID) => {
        this.setState({
            variantUUID,
            releaseVersion,
        })
    }


    private fetchModuleDetails = async (data) => {
        await this.getVariantParam()
        const path = data.location.pathname.substring(this.isAssembly ? PathPrefixes.ASSEBMLY_PATH_PREFIX.length : PathPrefixes.MODULE_PATH_PREFIX.length)
        this.setState({
            modulePath: path,
            releasePath: "/content" + path + ".preview?variant=" + this.state.variant
        })

        this.getPortalUrl(path, this.state.variant)

        fetch("/content" + path + "/en_US.harray.4.json")
            .then(response => response.json())
            .then(responseJSON => {
                // TODO: refactor for loops
                for (const sourceVariant of responseJSON.__children__) {
                    if (!sourceVariant.__children__) {
                        continue
                    }
                    for (const myChild of sourceVariant.__children__) {

                        if (!myChild.__children__) {
                            continue
                        }

                        for (const myGrandchild of myChild.__children__) {
                            if (!myGrandchild.__children__) {
                                continue
                            }

                            for (const offspring of myGrandchild.__children__) {
                                if (offspring.__name__ === "metadata") {
                                    if (myGrandchild.__name__ === "released") {
                                        this.setState({
                                            firstPublishDate: offspring["pant:dateFirstPublished"],
                                            lastPublishDate: offspring["pant:datePublished"]
                                        })
                                    }

                                    if (offspring[Fields.JCR_TITLE] !== undefined) {

                                        this.setState({
                                            moduleTitle: offspring[Fields.JCR_TITLE],
                                        })
                                    }
                                    if (!this.isAssembly && offspring["pant:moduleType"] !== undefined) {

                                        this.setState({
                                            moduleType: offspring["pant:moduleType"],

                                        })
                                    }
                                    if (this.isAssembly && offspring[Fields.PANT_MODULE_TYPE] !== undefined) {

                                        this.setState({
                                            moduleType: offspring[Fields.PANT_MODULE_TYPE],

                                        })
                                    }
                                }
                            }

                        }
                    }

                }
                if (!this.isAssembly) {
                    // get the variant UUID
                    for (const variants of responseJSON.__children__) {
                        if (variants.__name__ === "variants") {
                            for (const variant of variants.__children__) {
                                this.fetchIncludedInAssembliesDetails(variant[Fields.JCR_UUID])
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

    private onGetUrl = (url) => {
        console.log("Received url=" + url)
        if (url !== "") {
            this.setState({ portalUrl: url })
        }
    }

    private getLocale = (path) => {
        // remove /module from path
        path = path.substring(this.isAssembly ? PathPrefixes.ASSEBMLY_PATH_PREFIX.length : PathPrefixes.MODULE_PATH_PREFIX.length)
        // path = "/content" + path + "/en_US/1/metadata.json"
        path = "/content" + path + ".harray.1.json"
        fetch(path)
            .then((response) => {
                if (response.ok) {
                    return response.json()
                } else {
                    throw new Error(response.statusText)
                }
            })
            .then(responseJSON => {
                const locale = responseJSON.__children__[0].__name__
                const localeFinal = locale.replace("_", "-")
                this.setState({ locale: localeFinal })
            }

            )
    }
    private getVersionUUID = (path) => {
        // remove /module from path
        path = path.substring(this.isAssembly ? PathPrefixes.ASSEBMLY_PATH_PREFIX.length : PathPrefixes.MODULE_PATH_PREFIX.length)
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
                                if (offspring.__name__ === "metadata") {

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
        const path = "/content/products.harray.3.json"
        fetch(path)
            .then(response => response.json())
            .then(responseJSON => {
                for (const product of responseJSON.__children__) {
                    if (!product.__children__) {
                        continue
                    }
                    for (const productChild of product.__children__) {
                        if (productChild.__name__ !== "versions") {
                            continue
                        }
                        if (productChild.__children__) {
                            for (const productVersion of productChild.__children__) {
                                if (productVersion[Fields.JCR_UUID] === uuid) {
                                    this.setState({ productValue: product.name, versionValue: productVersion.name, productUrlFragment: product.urlFragment, versionUrlFragment: productVersion.urlFragment })
                                }
                            }
                        }
                    }
                }
            })

    }

    private copyToClipboard = () => {
        const textField = document.createElement("textarea")
        if (this.state.variantUUID.trim() !== "") {
            textField.value = this.state.portalUrl
            document.body.appendChild(textField)
            textField.select()
            document.execCommand("copy")
            textField.remove()
            this.setState({ copySuccess: "Copied!" })
        }
    }

    private mouseLeave = () => {
        this.setState({ copySuccess: "" })
    }

    private getPortalUrl = (path, variant) => {
        const variantPath = "/content" + path + "/en_US/variants/" + variant + ".url.json"
        fetch(variantPath)
            .then(resp => {
                if (resp.ok) {
                    resp.json().then(json => {
                        this.setState({ portalUrl: json.url, portalUrlType: json.type})
                    })
                }
            })
    }

    private async getVariantParam() {
        const query = new URLSearchParams(this.props.location.search);
        const variantParam = query.get("variant")
        // console.log("[moduleDisplay] variantParam => "", variantParam)
        if (variantParam !== "undefined" && variantParam !== null) {
            this.setState({ variant: variantParam })
        }
    }

    private fetchAttributesFilePath = async (data) => {
        await this.getVariantParam()
        const path = data.location.pathname.substring(this.isAssembly ? PathPrefixes.ASSEBMLY_PATH_PREFIX.length : PathPrefixes.MODULE_PATH_PREFIX.length)
        // console.log("[fetchAttributesFilePath] path =>", path)
        let repo = ""
        const group = path.split("/")
        repo = group[2]
        fetch("/content/repositories/" + repo + "/module_variants/" + this.state.variant + ".harray.json")
            .then((response) => {
                if (response.ok) {
                    return response.json()
                } else {
                    throw new Error(response.statusText)
                }
            })
            .then(responseJSON => {
                if (responseJSON["pant:attributesFilePath"] !== undefined) {
                    this.setState({ attributesFilePath: responseJSON["pant:attributesFilePath"] })
                }
            })
            .catch((error) => {
                console.log(error)
            })
    }

    private fetchIncludedInAssembliesDetails = (data) => {
        fetch("/module/assemblies.json/" + data)
            .then((response) => {
                if (response.ok) {
                    return response.json()
                } else {
                    throw new Error(response.statusText)
                }
            })
            .then(responseJSON => {
                this.setState({ assemblyData: responseJSON.assemblies })
            }

            )
    }
    private getPortalHostUrl = () => {
        fetch('/conf/pantheon/pant:portalUrl')
            .then(resp => {
                if (resp.ok) {
                    resp.text().then(text => {
                        this.setState({ portalHostUrl: text })
                        // console.log("set portalHost: " + this.state.portalHost)
                    })
                }
            })
    }

    private canRegeneratePortalUrl = (regeneratePortalUrl: boolean) => {
        this.setState({regeneratePortalUrl})
    }

}

export { ContentDisplay }