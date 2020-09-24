import React, { Component } from "react";
import { Link } from "react-router-dom";

class BuildInfo extends Component {
    public state = {
        buildDate: "",
        commitHash: "",
        commitText: ""
    };

    public componentDidMount() {
        this.getBuildInfo()
    }

    public render() {
        return (
            <React.Fragment>
                <div className="column-view">
                    {!this.state.commitHash.includes("OPENSHIFT_BUILD_COMMIT") && <Link to=""
                        onClick={this.commitRedirect}>
                        {this.state.buildDate}
                    </Link>}
                    {this.state.commitHash.includes("OPENSHIFT_BUILD_COMMIT") && this.state.buildDate}
                </div>
            </React.Fragment>
        );
    }

    public getBuildInfo() {
        const backend = "/pantheon/builddate.json"
        if (this.state.buildDate === "") {
            fetch(backend)
                .then(response => response.json())
                .then(responseJSON => this.setState({ buildDate: "Build Date: " + responseJSON.buildDate, commitHash: responseJSON.commitHash }))
        }
    };

    private commitRedirect = () => {
        window.location.href = "https://github.com/redhataccess/pantheon/commit/" + this.state.commitHash
    }
}

export { BuildInfo }