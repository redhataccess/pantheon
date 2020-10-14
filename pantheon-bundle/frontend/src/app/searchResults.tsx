import React, { Component } from "react";

import {
    Table,
    TableHeader,
    TableBody,
    headerCol,
} from "@patternfly/react-table";

import "@app/app.css";
import styles from "@patternfly/react-styles/css/components/Table/table";

export interface ISearchState {

    columns: [
        { title: string, cellTransforms: any },
        "Branches",
        { title: string },
        "Workspaces",
        "Last Commit"
    ],
    rows: [
        { cells: string[] },
        {
            cells: string[],
        },
        {
            cells: string[]
        }
    ],
}
class SearchResults extends Component<any, ISearchState> {

    constructor(props) {
        super(props);
        this.state = {
            // states for table
            columns: [
                { title: "Repositories", cellTransforms: [headerCol()] },
                "Branches",
                { title: "Pull requests" },
                "Workspaces",
                "Last Commit"
            ],
            rows: [
                {
                    cells: ["one", "two", "a", "four", "five"]
                },
                {
                    cells: ["a", "two", "k", "four", "five"]

                },
                {
                    cells: ["p", "two", "b", "four", "five"]
                }
            ],

        };
    }

    //   public componentDidMount() {

    //   }


    public render() {
        const { columns, rows } = this.state;

        return (
            <Table aria-label="Simple Table" cells={columns} rows={rows}>
                <TableHeader className={styles.modifiers.nowrap} />
                <TableBody />
            </Table>
        );
    }

    // private methods

}


export { SearchResults }; 