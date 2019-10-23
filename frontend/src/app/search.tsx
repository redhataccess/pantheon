import React, { Component, FormEvent } from 'react'
import {
  Alert, AlertActionCloseButton, TextInput,
  DataList, DataListItem, DataListItemRow, DataListItemCells,
  DataListCell, FormGroup, Button, Modal,
  Level, LevelItem, Checkbox
} from '@patternfly/react-core'
import '@app/app.css'
import { BuildInfo } from './components/Chrome/Header/BuildInfo'
import { Pagination } from '@app/Pagination'
import { BrowserRouter as Router, Route, Link, Switch } from 'react-router-dom'
import { IAppState } from '@app/app'
import { SearchFilter } from '@app/searchFilter'

export interface ISearchState {
  alertOneVisible: boolean
  checkNextPageRow: string
  columns: string[]
  confirmDelete: boolean
  deleteState: string
  input: string
  isEmptyResults: boolean
  isModalOpen: boolean
  isSortedUp: boolean
  moduleName: string
  modulePath: string
  moduleType: string
  moduleUpdatedDate: string
  nextPageRowCount: number
  page: number
  pageLimit: number
  redirect: boolean
  redirectLocation: string
  results: any
  selectAllCheckValue: boolean
  showDropdownOptions: boolean
  sortKey: string
}

class Search extends Component<IAppState, ISearchState> {
  public static KEY_CHECKEDITEM: string = "checkedItem"
  public static KEY_TRANSIENTPATH: string = "pant:transientPath"

  constructor(props) {
    super(props);
    this.state = {
      alertOneVisible: true,
      checkNextPageRow: "",
      columns: ['Name', 'Description', 'Source Type', 'Source Name', 'Upload Time'],
      confirmDelete: false,
      deleteState: '',
      input: '',
      isEmptyResults: false,
      isModalOpen: false,
      isSortedUp: true,
      moduleName: '',
      modulePath: '',
      moduleType: '',
      moduleUpdatedDate: '',
      nextPageRowCount: 1,
      page: 1,
      pageLimit: 25,
      redirect: false,
      redirectLocation: '',
      results: [{ "pant:transientPath": '', "pant:dateUploaded": '', "name": "", "jcr:title": "", "jcr:description": "", "sling:transientSource": "", "pant:transientSourceName": "", "checkedItem": false }],
      selectAllCheckValue: false,
      showDropdownOptions: true,
      sortKey: ''
    };
  }

  public componentDidMount() {
    this.doSearch()
  }


  public render() {
    const { isEmptyResults } = this.state;

    return (
      <React.Fragment>
        <div>
          <div>
          <SearchFilter
            onKeyDown={this.getRows} 
            onChange={this.setInput} 
            value={this.state.input}
            onClick={this.newSearch}
            onSort={this.setSortedUp}
            isSortedUp={this.state.isSortedUp}
            />
            <div className="notification-container">
              <Pagination
                handleMoveLeft={this.updatePageCounter("L")}
                handleMoveRight={this.updatePageCounter("R")}
                handleMoveToFirst={this.updatePageCounter("F")}
                pageNumber={this.state.page}
                nextPageRecordCount={this.state.nextPageRowCount}
                handlePerPageLimit={this.changePerPageLimit}
                perPageLimit={this.state.pageLimit}
                showDropdownOptions={this.state.showDropdownOptions}
                bottom={false}
              />
            </div>
            <DataList aria-label="Simple data list" >
              <DataListItem aria-labelledby="simple-item1">
                <DataListItemRow id="data-rows-header" >
                  {this.props.userAuthenticated && !this.state.isEmptyResults &&
                    <Checkbox aria-labelledby="width-ex1-check1"
                      className="checkbox"
                      isChecked={this.state.selectAllCheckValue}
                      checked={this.state.selectAllCheckValue}
                      aria-label="controlled checkbox example"
                      id="check"
                      onChange={this.handleSelectAll}
                    />}
                  <DataListItemCells
                    dataListCells={[
                      <DataListCell width={2} key="title">
                        <button onClick={this.sortByName} className="sp-prop" id="span-name" aria-label="sort column by name">Name</button>
                      </DataListCell>,
                      <DataListCell width={2} key="description">
                        <button onClick={this.sortByDescription} className="sp-prop" id="span-name" aria-label="sort column by description">Description</button>
                      </DataListCell>,
                      <DataListCell key="resource source">
                        <span className="sp-prop-nosort" id="span-source-type">Source Type</span>
                      </DataListCell>,
                      <DataListCell key="source name">
                        <span className="sp-prop-nosort" id="span-source-name">Source Name</span>
                      </DataListCell>,
                      <DataListCell key="upload time">
                        <button onClick={this.sortByUploadTime} className="sp-prop" id="span-name" aria-label="sort column by upload time">Upload Time</button>
                      </DataListCell>,
                    ]}
                  />
                </DataListItemRow>
                {/* Delete button at the top */}
                <DataListItemRow id="data-rows" key={this.state.results[Search.KEY_TRANSIENTPATH]}>
                  {
                    this.buildTransientPathArray().length > 0 ?
                      <Button variant="primary" onClick={this.confirmDeleteOperation}>Delete</Button>
                      : null
                  }
                </DataListItemRow>
                {this.state.results.map((data, key) => (
                  <DataListItemRow id="data-rows">
                    {this.props.userAuthenticated && !this.state.isEmptyResults &&
                      <Checkbox aria-labelledby="width-ex3-check1"
                        className="checkbox"
                        isChecked={data[Search.KEY_CHECKEDITEM]}
                        checked={data[Search.KEY_CHECKEDITEM]}
                        aria-label="controlled checkbox example"
                        id={data[Search.KEY_TRANSIENTPATH]}
                        name={data[Search.KEY_TRANSIENTPATH]}
                        onChange={this.handleDeleteCheckboxChange}
                        key={key}
                      />}

                    <DataListItemCells key={data[Search.KEY_TRANSIENTPATH]}
                      dataListCells={[
                        <DataListCell key="div-title" width={2}>
                          {this.props.userAuthenticated &&
                            <Link to={data['pant:transientPath']}>{data["jcr:title"]}</Link>}
                          {!this.props.userAuthenticated &&
                            <a href={"/" + data['pant:transientPath'] + ".preview"} target="_blank">{data["jcr:title"]}</a>}
                        </DataListCell>,
                        <DataListCell key="div-description" width={2}>
                          <span>{data["jcr:description"]}</span>
                        </DataListCell>,
                        <DataListCell key="div-transient-source">
                          <span>{data["pant:transientSource"]}</span>
                        </DataListCell>,
                        <DataListCell key="div-transient-source-name">
                          <span>{data["pant:transientSourceName"]}</span>
                        </DataListCell>,
                        <DataListCell key="div-created">
                          <span >{this.formatDate(new Date(data["pant:dateUploaded"]))}</span>
                        </DataListCell>
                      ]}
                    />
                  </DataListItemRow>
                ))}
                {/* Delete button at the bottom */}
                <DataListItemRow id="data-rows" key={this.state.results[Search.KEY_TRANSIENTPATH]}>
                  {
                    this.buildTransientPathArray().length > 0 ?
                      <Button variant="primary" onClick={this.confirmDeleteOperation}>Delete</Button>
                      : null
                  }

                </DataListItemRow>
                {isEmptyResults && (
                  <Level gutter="md">
                    <LevelItem />
                    <LevelItem>
                      <div className="notification-container">
                        <br />
                        <br />
                        <Alert
                          variant="warning"
                          title={"No modules found with your search of: " + this.state.input}
                          action={<AlertActionCloseButton onClose={this.dismissNotification} />}
                        />
                        <br />
                        <br />
                      </div></LevelItem>
                    <LevelItem />
                  </Level>

                )}
              </DataListItem>
            </DataList>
            <div className="notification-container">
              <Pagination
                handleMoveLeft={this.updatePageCounter("L")}
                handleMoveRight={this.updatePageCounter("R")}
                handleMoveToFirst={this.updatePageCounter("F")}
                pageNumber={this.state.page}
                nextPageRecordCount={this.state.nextPageRowCount}
                handlePerPageLimit={this.changePerPageLimit}
                perPageLimit={this.state.pageLimit}
                showDropdownOptions={!this.state.showDropdownOptions}
                bottom={true}
              />
              <BuildInfo />
            </div>
            {/* Alert for delete confirmation */}
            <div className="alert">
              {this.state.confirmDelete === true && <Modal
                isSmall={true}
                title="Confirmation"
                isOpen={!this.state.isModalOpen}
                onClose={this.hideAlertOne}
                actions={[<Button key="yes" variant="primary" onClick={this.delete}>Yes</Button>,
                <Button key="no" variant="secondary" onClick={this.cancelDeleteOperation}>No</Button>]}
              >
                Are you sure you want to delete the selected items?
                </Modal>}
              {/* Alerts after confirmation on delete */}
              {this.state.deleteState === 'positive' && <Modal
                isSmall={true}
                title="Success"
                isOpen={!this.state.isModalOpen}
                onClose={this.hideAlertOne}
                actions={[<Button key="cancel" variant="primary" onClick={this.hideAlertOne}>OK</Button>]}
              >
                Selected items were deleted.
                </Modal>}
              {this.state.deleteState === 'negative' && <Modal
                isSmall={true}
                title="Failure"
                isOpen={!this.state.isModalOpen}
                onClose={this.hideAlertOne}
                actions={[<Button key="cancel" variant="primary" onClick={this.hideAlertOne}>OK</Button>]}
              >
                Selected items were not found.
                </Modal>}
              {this.state.deleteState === 'unknown' && <Modal
                isSmall={true}
                title="Error"
                isOpen={!this.state.isModalOpen}
                onClose={this.hideAlertOne}
                actions={[<Button key="cancel" variant="primary" onClick={this.hideAlertOne}>OK</Button>]}
              >
                An unknown error occured, please check if you are logged in.
                </Modal>}
            </div>
          </div>
        </div>
      </React.Fragment>
    );
  }

  private setInput = (event) => this.setState({ input: event });

  private handleSelectAll = (checked: boolean, event: FormEvent<HTMLInputElement>) => {
    const newResults: any[] = []
    this.state.results.map(dataitem => {
      newResults.push(JSON.parse(JSON.stringify(dataitem))) // clones the object
      newResults[newResults.length - 1][Search.KEY_CHECKEDITEM] = checked
    })

    this.setState({
      results: newResults,
      selectAllCheckValue: checked
    })
  }

  private handleDeleteCheckboxChange = (checked: boolean, event: FormEvent<HTMLInputElement>) => {
    const newResults: any[] = []
    this.state.results.map(data => {
      newResults.push(JSON.parse(JSON.stringify(data))) // clones the object
      if (data[Search.KEY_TRANSIENTPATH] === event.target['name']) {
        newResults[newResults.length - 1][Search.KEY_CHECKEDITEM] = checked
      }
    })

    this.setState({
      results: newResults,
      selectAllCheckValue: false
    })
  };

  private delete = (event: React.MouseEvent<HTMLButtonElement, MouseEvent>) => {
    const formData = new FormData()
    const keydata = this.buildTransientPathArray()
    formData.append(':operation', 'delete')
    for (const item of keydata) {
      formData.append(':applyTo', '/content/' + item)
    }
    fetch('/content/' + keydata[0], {
      body: formData,
      method: 'post'
    }).then(response => {
      if (response.status === 200) {
        this.setState({ deleteState: 'positive' })
      } else if (response.status === 403) {
        this.setState({ deleteState: 'negative' })
      } else {
        this.setState({ deleteState: 'unknown' })
      }
    });
  }

  private getRows = (event) => {
    if (event.key === 'Enter') {
      this.setState({ page: 1 }, () => {
        this.doSearch()
      })
    }
  };

  private newSearch = () => {
    this.setState({ page: 1 }, () => {
      this.doSearch()
    })
  }

  // Handle gateway timeout on slow connections.
  private doSearch = () => {
    fetch(this.buildSearchUrl())
      .then(response => response.json())
      .then(responseJSON => this.setState({ results: responseJSON.results, nextPageRowCount: responseJSON.hasNextPage ? 1 : 0 }))
      .then(() => {
        if (JSON.stringify(this.state.results) === "[]") {
          this.setState({
            isEmptyResults: true,
            selectAllCheckValue: false
          })
        } else {
          this.setState({
            isEmptyResults: false,
            selectAllCheckValue: false,
          })
        }
      })
      .catch(error => {
        // might be a timeout error
        console.log("[doSearch] error ", error)
      })
  }


  private formatDate(date: Date) {
    // 2019/05/07 14:21:36
    let dateStr = date.getFullYear().toString() + "/" +
      (date.getMonth() + 1).toString() + "/" +
      date.getDate().toString() + " " +
      date.getHours().toString() + ":" +
      date.getMinutes().toString() + ":" +
      date.getSeconds().toString()

    if (dateStr.includes("NaN")) {
      dateStr = ""
    }
    return dateStr
  };

  private dismissNotification = () => {
    this.setState({ isEmptyResults: false });
  };

  private sortByName = () => {
    this.sort("jcr:title")
  }

  private sortByDescription = () => {
    this.sort("jcr:description")
  }

  private sortByUploadTime = () => {
    this.sort("pant:dateUploaded")
  }

  private sort(key: string) {
    // Switch the direction each time some clicks.
    this.setSortedUp()
    this.setState({sortKey: key }, () => {
      this.getSortedRows()
    }) 
  };

  private setSortedUp = () => {
    this.setState({ isSortedUp: !this.state.isSortedUp }, () => {      this.getSortedRows()
    })
  };

  private getSortedRows() {
    fetch(this.buildSearchUrl())
      .then(response => response.json())
      .then(responseJSON => this.setState({
        isEmptyResults: responseJSON.results === '[]',
        nextPageRowCount: responseJSON.hasNextPage ? 1 : 0,
        results: responseJSON.results
       }))
  };

  private buildSearchUrl() {
    let backend = "/modules.json?search="
    if (this.state.input != null) {
      backend += this.state.input
    }
    backend += "&key=" + this.state.sortKey + "&direction=" + (this.state.isSortedUp ? "desc" : "asc")
    backend += "&offset=" + ((this.state.page - 1) * this.state.pageLimit) + "&limit=" + this.state.pageLimit
    return backend
  }

  private hideAlertOne = () => this.setState({ alertOneVisible: false }, () => {
    this.setState({
      confirmDelete: false,
      deleteState: '',
      page: 1
    }, () => { this.doSearch() });
  });

  private confirmDeleteOperation = () => this.setState({ confirmDelete: !this.state.confirmDelete })

  private cancelDeleteOperation = () => this.setState({ confirmDelete: !this.state.confirmDelete })

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

  private changePerPageLimit = (pageLimitValue) => {
    this.setState({ pageLimit: pageLimitValue, page: 1 }, () => {
      // console.log("pageLImit value on calling changePerPageLimit function: "+this.state.pageLimit)
      return (this.state.pageLimit + " items per page")
    })
  }

  private buildTransientPathArray = () => {
    const tPaths: string[] = []
    this.state.results.map(item => {
      if (item[Search.KEY_CHECKEDITEM]) {
        tPaths.push(item[Search.KEY_TRANSIENTPATH])
      }
    })
    return tPaths
  }
}

export { Search }