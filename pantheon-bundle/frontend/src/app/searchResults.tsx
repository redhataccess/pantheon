import React, { Component } from "react";

import {
  Table,
  TableHeader,
  TableBody,
  headerCol,
} from "@patternfly/react-table";
import { Checkbox } from '@patternfly/react-core';
import { Tooltip } from '@patternfly/react-core';
import "@app/app.css";
import styles from "@patternfly/react-styles/css/components/Table/table";
import { Pagination } from "@app/Pagination"
import {
  Divider,
  EmptyState,
  EmptyStateBody,
  Title,
  EmptyStateIcon,
  EmptyStateVariant
} from "@patternfly/react-core";
import { SearchIcon, ExclamationTriangleIcon, ExclamationCircleIcon } from "@patternfly/react-icons";
import CheckCircleIcon from "@patternfly/react-icons/dist/js/icons/check-circle-icon"
import { SlingTypesPrefixes } from "./Constants";

export interface IProps {
  contentType: string
  keyWord: string
  filters: { ctype: any, status: any }
  productsSelected: string[]
  repositoriesSelected: string[]
  userAuthenticated: boolean
  onGetdocumentsSelected: (documentsSelected) => any
  onSelectContentType: (contentType) => any
  currentBulkOperation: string
  disabledClassname: string
  bulkOperationCompleted: boolean
}
export interface ISearchState {

  columns: [
    { title: string },
    { title: string, cellTransforms: any },
    { title: string },
    { title: string },
    { title: string }
  ],
  displayLoadIcon: boolean
  // filterQuery: string
  isEmptyResults: boolean
  isSearchException: boolean
  // states for pagination
  nextPageRowCount: number
  page: number
  pageLimit: number
  itemsPerPage: number
  results: any
  rows: any
  canSelectAll: boolean
  showDropdownOptions: boolean
  bottom: boolean
}
class SearchResults extends Component<IProps, ISearchState> {

  constructor(props) {
    super(props);
    this.state = {
      // states for table
      columns: [
        { title: "" },
        { title: "Title", cellTransforms: [headerCol()] },
        { title: "Repository" },
        { title: "Upload Date" },
        { title: "Last Published Date" }
      ],
      displayLoadIcon: true,
      // filterQuery: "",
      isEmptyResults: false,
      isSearchException: false,
      // states for pagination
      nextPageRowCount: 1,
      page: 1,
      pageLimit: 5,
      itemsPerPage: 5,
      results: [
        {
          "pant:transientPath": "",
          "pant:dateUploaded": "",
          "name": "",
          "jcr:title": "",
          "jcr:description": "",
          "productVersion": "",
          "sling:transientSource": "",
          "pant:transientSourceName": "",
          "checkedItem": false,
          "publishedDate": "-",
          "pant:moduleType": "-",
          "variant": "",
          "validations": ""
        }
      ],
      // states for table
      rows: [
        {
          cells: ["", "", "", ""]
        }
      ],
      canSelectAll: true,
      showDropdownOptions: true,
      bottom: true,
    };

    this.onSelect = this.onSelect.bind(this);
    this.toggleSelect = this.toggleSelect.bind(this);
  }

  public componentDidMount() {
    this.doSearch()
  }

  public componentDidUpdate(prevProps) {
    if (this.props.repositoriesSelected !== prevProps.repositoriesSelected
      || this.props.productsSelected !== prevProps.productsSelected
      || this.props.keyWord !== prevProps.keyWord
      || this.props.filters !== prevProps.filters
      || this.props.onGetdocumentsSelected !== prevProps.onGetdocumentsSelected
      || (this.props.bulkOperationCompleted !== prevProps.bulkOperationCompleted
        && this.props.currentBulkOperation.length === 0
      )
    ) {
      this.doSearch()
    }
  }

  public render() {
    const { columns, rows, canSelectAll, results } = this.state;

    return (
      <React.Fragment>
        {!this.state.isEmptyResults &&
          <div className={this.props.disabledClassname}>
            <Checkbox
              label="Can select all"
              className="pf-u-mb-lg"
              isChecked={canSelectAll}
              onChange={this.toggleSelect}
              aria-label="toggle select all checkbox"
              id={"toggle-select-all-" + this.props.contentType}
              name={"toggle-select-all-" + this.props.contentType}
            />
            <Table
              onSelect={this.onSelect}
              canSelectAll={canSelectAll}
              aria-label={"Selectable Table " + this.props.contentType}
              cells={columns}
              rows={rows}
            >
              <TableHeader className={styles.modifiers.nowrap} />
              <TableBody className="results__table-body" />
            </Table></div>}

        {!this.state.isEmptyResults && <Pagination
          handleMoveLeft={this.updatePageCounter("L")}
          handleMoveRight={this.updatePageCounter("R")}
          handleMoveToFirst={this.updatePageCounter("F")}
          pageNumber={this.state.page}
          nextPageRecordCount={this.state.nextPageRowCount}
          handlePerPageLimit={this.changePerPageLimit}
          handleItemsPerPage={this.changePerPageLimit}
          perPageLimit={this.state.pageLimit}
          showDropdownOptions={this.state.showDropdownOptions}
          bottom={this.state.bottom}
          className="results__pagination"
          currentBulkOperation={this.props.currentBulkOperation}
          
        />}

        {this.state.isEmptyResults && <EmptyState variant={EmptyStateVariant.small} className="search-results--empty">
          <EmptyStateIcon icon={SearchIcon} />
          <Title headingLevel="h2" size="lg">
            No results found
        </Title>
          <EmptyStateBody>
            No results match the filter criteria. Select repo filter to show results.
        </EmptyStateBody>

        </EmptyState>
        }
        <Divider />
      </React.Fragment>
    );
  }

  // private methods
  private buildSearchQuery() {
    if ((this.props.repositoriesSelected && this.props.repositoriesSelected.length > 0)
      || (this.props.productsSelected && this.props.productsSelected.length > 0)) {
      let backend = "/pantheon/internal/modules.json?"

      if (this.props.repositoriesSelected) {
        this.props.repositoriesSelected.map(repo => {
          if (backend.endsWith("?") || backend.endsWith("&")) {
            backend += "repo=" + repo
          } else {
            backend += "&repo=" + repo
          }
        })
      }

      //TODO: enable product filter in the query
      // if (this.props.productsSelected) {
      //   console.log("[productsSelected]", this.props.productsSelected)
      //   this.props.productsSelected.map(product => {
      //     backend += "product=" + product + "&"
      //   })
      // }

      if (this.props.contentType) {
        backend += "&ctype=" + this.props.contentType
      }

      if (this.props.keyWord) {
        backend += "&search=" + this.props.keyWord.trim()
      }

      if (this.props.filters) {
        if (this.props.filters.ctype) {
          this.props.filters.ctype.map(type => {
            backend += "&type=" + type
          })
        }
        if (this.props.filters.status) {
          this.props.filters.status.map(stat => {
            backend += "&status=" + stat
          })
        }
      }

      backend += "&offset=" + ((this.state.page - 1) * this.state.itemsPerPage) + "&limit=" + this.state.itemsPerPage

      if (!backend.includes("Updated") && !backend.includes("direction")) {
        backend += "&key=Updated&direction=desc"
      }

      return backend
    } else {
      this.setState({ isEmptyResults: true })
      return ""
    }
  }

  // private setFilterQuery = (filterQuery: string) => {
  //   this.setState({ filterQuery })
  // };

  // methods for pagination
  private updatePageCounter = (direction: string) => () => {
    if (direction === "L" && this.state.page > 1) {
      this.setState({ page: this.state.page - 1 }, () => {
        this.doSearch()
      })
    } else if (direction === "R") {
      this.setState({ page: this.state.page + 1 }, () => {
        this.doSearch()
      })
    } else if (direction === "F") {
      this.setState({ page: 1 }, () => {
        this.doSearch()
      })
    }
  }

  // Handle gateway timeout on slow connections.
  private doSearch = () => {
    this.setState({ displayLoadIcon: true })
    if (this.buildSearchQuery()) {
      fetch(this.buildSearchQuery())
        .then(response => response.json())
        .then(responseJSON => {
          this.setState({ results: responseJSON.results, nextPageRowCount: responseJSON.hasNextPage ? 1 : 0 })
          const data = new Array()

          responseJSON.results.map((item, key) => {
            const lastUpdateDate = item["pant:publishedDate"]

            let docIcon = lastUpdateDate !== "-" ? <div><Tooltip position="top" content={<div>Published successfully</div>}><CheckCircleIcon className="p2-search__check-circle-icon" /></Tooltip></div> : null
            if (docIcon === null) {
              const productVersion = item["productVersion"] != undefined ? item["productVersion"] : "-"

              docIcon = productVersion == "-" ? <div><Tooltip position="top" content={<div>Metadata missing</div>}><ExclamationTriangleIcon color="#f0ab00" /></Tooltip></div> : null
            }
            if (item.validations !== undefined && item.validations.length > 1) {
              let vIcon = <div><Tooltip position="top" content={<div>{item.validations}</div>}><ExclamationCircleIcon color="#c9190b" /></Tooltip></div>
              docIcon = <span>{docIcon}{vIcon}</span>
            }
            const cellItem = new Array()
            cellItem.push(docIcon)
            if (this.props.userAuthenticated) {
              cellItem.push({ title: <a href={"/pantheon/#" + item["sling:resourceType"].substring(SlingTypesPrefixes.PANTHEON.length) + "/" + item['pant:transientPath'] + "?variant=" + item.variant}> {item["jcr:title"] !== "-" ? item["jcr:title"] : item["pant:transientPath"]} </a> })
            } else {
              let docTitle = item["jcr:title"] !== "-" ? item["jcr:title"] : item["pant:transientPath"]
              cellItem.push(docTitle)
            }
            { title: "Last Published Date" }
            cellItem.push(item["pant:transientSourceName"])
            cellItem.push(item["pant:dateUploaded"])
            cellItem.push(lastUpdateDate)

            data.push({ cells: cellItem })
          })
          if (responseJSON.results.length > 0) {
            this.setState({ rows: data })
          } else {
            const rows = [{ cells: ["", "", "", ""] }]
            this.setState({ rows })
          }
        })
        .then(() => {
          if (JSON.stringify(this.state.results) === "[]") {
            this.setState({
              displayLoadIcon: false,
              isEmptyResults: true,
              // selectAllCheckValue: false
            })
          } else {
            this.setState({
              displayLoadIcon: false,
              isEmptyResults: false,
              // selectAllCheckValue: false,
            })
          }
        })
        .catch(error => {
          // might be a timeout error
          this.setState({
            displayLoadIcon: false,
            isSearchException: true
          }, () => { console.log("[doSearch] error ", error) })

        })
    } else {
      this.setState({ isEmptyResults: true })
    }
  }

  public changePerPageLimit = (pageLimitValue) => {
    this.setState({ pageLimit: pageLimitValue, page: 1, itemsPerPage: pageLimitValue }, () => {
      this.props.onGetdocumentsSelected([])
      this.doSearch()
      this.props.onSelectContentType("")
    })
  }

  private onSelect(event, isSelected, rowId) {
    let rows;
    if (rowId === -1) {
      rows = this.state.rows.map(oneRow => {
        oneRow.selected = isSelected;
        return oneRow;
      });
    } else {
      rows = [...this.state.rows];
      rows[rowId].selected = isSelected;
    }
    this.setState({
      rows
    });
    // update props.documentSelected
    let selectedRows;
    selectedRows = this.state.rows.map(oneRow => {
      if (oneRow.selected === true) {
        return oneRow;
      }
    })
    // filter undefined values
    selectedRows = selectedRows.filter(r => r !== undefined)

    this.props.onGetdocumentsSelected(selectedRows);

    if (selectedRows.length > 0 || isSelected == true) {
      this.props.onSelectContentType(this.props.contentType);
    }
    if (selectedRows.length == 0 && isSelected == false) {
      this.props.onSelectContentType("");
    }
  }

  private toggleSelect(checked) {
    this.setState({
      canSelectAll: checked
    });
  }
}


export { SearchResults }; 