import React, { Component } from "react";
import {
    Drawer,
    DrawerPanelContent,
    DrawerContent,
    DrawerContentBody,

    DrawerHead,
    DrawerActions,
    DrawerCloseButton,
    Button
} from "@patternfly/react-core";
class SearchFilters extends Component<any, any> {
    private drawerRef: React.RefObject<HTMLInputElement>;


    constructor(props) {
        super(props);
        this.state = {
            filterLabel: "repo",
            isExpanded: true,
            repositories: [{ name: "", uuid: "" }],
        };
        this.drawerRef = React.createRef();
    }


    public componentDidMount() {
        this.getRepositories();
    }


    public render() {
        const { isExpanded } = this.state;
        const panelContent = (
            <DrawerPanelContent>
                <DrawerHead>
                    <span tabIndex={isExpanded ? 0 : -1} ref={this.drawerRef}>drawer-panel</span>
                    <DrawerActions>
                        <DrawerCloseButton onClick={this.onCloseClick} />
                    </DrawerActions>
                </DrawerHead>
            </DrawerPanelContent>
        );
        const drawerContent = "";

        return (
            <React.Fragment>
                <Button aria-expanded={isExpanded} onClick={this.onClick}>
                    Toggle Drawer
        </Button>
                <Drawer isExpanded={isExpanded} isInline={true} position="left" onExpand={this.onExpand}>
                    <DrawerContent panelContent={panelContent}>
                        <DrawerContentBody>{drawerContent}</DrawerContentBody>
                    </DrawerContent>
                </Drawer>
            </React.Fragment>

        );
    }

    // New

    private getRepositories = () => {
        const path = "/content/repositories.harray.1.json";
        const repos = new Array();
        fetch(path)
            .then((response) => {
                if (response.ok) {
                    return response.json();
                }
                else {
                    throw new Error(response.statusText);
                }
            })
            .then(responseJSON => {
                for (const repository of responseJSON.__children__) {
                    repos.push({ name: repository.__name__, uuid: repository["jcr:uuid"] });
                }
                this.setState({
                    repositories: repos
                });
            })
            .catch((error) => {
                console.log(error);
            });

    };


    private onExpand = () => {
        this.drawerRef.current && this.drawerRef.current.focus();
    };


    private onClick = () => {
        const isExpanded = !this.state.isExpanded;
        this.setState({
            isExpanded
        });
    };


    private onCloseClick = () => {
        this.setState({
            isExpanded: false
        });
    };
}
