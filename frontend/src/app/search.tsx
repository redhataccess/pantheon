import React, { Component, FormEvent } from 'react'
import {
  Alert, AlertActionCloseButton, TextInput,
  DataList, DataListItem, DataListItemRow, DataListItemCells,
  DataListCell, FormGroup, Button, DataListCheck, Modal,
  Level, LevelItem
} from '@patternfly/react-core'
import '@app/app.css'
import { BuildInfo } from './components/Chrome/Header/BuildInfo'
import { Pagination } from '@app/Pagination'
import { BrowserRouter as Router, Route, Link, Switch } from 'react-router-dom'
import { IAppState } from '@app/app'

export interface ISearchState {
  alertOneVisible: boolean
  checkNextPageRow: string
  columns: string[]
  confirmDelete: boolean
  deleteButtonVisible: boolean
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

  public transientPaths: string[] = [];
  constructor(props) {
    super(props);
    this.state = {
      alertOneVisible: true,
      checkNextPageRow: "",
      columns: ['Name', 'Description', 'Source Type', 'Source Name', 'Upload Time'],
      confirmDelete: false,
      deleteButtonVisible: false,
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
            <FormGroup
              label="Search Query"
              fieldId="search"
              helperText="Search is case sensitive. An empty search will show all modules."
            >
              <div className="row-view">
                <TextInput id="search" type="text" onKeyDown={this.getRows} onChange={this.setInput} value={this.state.input} />
                <Button onClick={this.newSearch}>Search</Button>
              </div>
            </FormGroup>
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
                    <DataListCheck aria-labelledby="width-ex1-check1"
                      className="checkbox"
                      // isChecked={this.state.selectAllCheckValue}
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
                    this.state.deleteButtonVisible ?
                      <Button variant="primary" onClick={this.confirmDeleteOperation}>Delete</Button>
                      : null
                  }
                </DataListItemRow>
                {this.state.results.map(data => (
                  <DataListItemRow id="data-rows">
                  {console.log(data[Search.KEY_CHECKEDITEM], data[Search.KEY_TRANSIENTPATH])}
                    {this.props.userAuthenticated && !this.state.isEmptyResults &&
                      <DataListCheck aria-labelledby="width-ex3-check1"
                        className="checkbox"
                        isChecked={data[Search.KEY_CHECKEDITEM]}
                        checked={data[Search.KEY_CHECKEDITEM]}
                        aria-label="controlled checkbox example"
                        // id={data[Search.KEY_TRANSIENTPATH]}
                        name={data[Search.KEY_TRANSIENTPATH]}
                        onChange={this.handleDeleteCheckboxChange}
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
                    this.state.deleteButtonVisible ?
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
                actions={[<Button key="yes" variant="primary" onClick={this.delete(this.transientPaths)}>Yes</Button>,
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
    console.log('selectAll start')
    console.log(this.state.results)

    this.transientPaths = []
    this.state.results.map(dataitem => {
      dataitem[Search.KEY_CHECKEDITEM] = checked
      if (checked) {
        this.transientPaths.push(dataitem[Search.KEY_TRANSIENTPATH])
      }
    })

    this.setState({
      deleteButtonVisible: checked,
      selectAllCheckValue: checked
    }, () => {
      console.log('selectAll finish')
      console.log(this.state.results)
    })
  }

  private handleDeleteCheckboxChange = (checked: boolean, event: FormEvent<HTMLInputElement>) => {
    console.log('handling check change for ', event.target['name'])
    this.transientPaths = []

    this.state.results.map(data => {
      if (data[Search.KEY_TRANSIENTPATH] === event.target['name']) {
        data[Search.KEY_CHECKEDITEM] = checked

        if (data[Search.KEY_CHECKEDITEM]) {
          this.transientPaths.push(data[Search.KEY_TRANSIENTPATH])
        }
      }
    })

    this.setState({
      deleteButtonVisible: this.transientPaths.length > 0,
      selectAllCheckValue: false
    })
  };

  private delete = (keydata) => (event: any) => {
    const formData = new FormData();
    formData.append(':operation', 'delete')
    for (const item of keydata) {
      formData.append(':applyTo', '/content/' + item)
    }
    fetch('/content/' + keydata[0], {
      body: formData,
      method: 'post'
    }).then(response => {
      if (response.status === 200) {
        this.setState({ deleteState: 'positive' }, () =>
          this.transientPaths = [])
      } else if (response.status === 403) {
        this.setState({ deleteState: 'negative' }, () =>
          this.transientPaths = [])
      } else {
        this.setState({ deleteState: 'unknown' }, () =>
          this.transientPaths = [])
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
            deleteButtonVisible: false,
            isEmptyResults: true,
            selectAllCheckValue: false
          })
        } else {
          this.setState({
            deleteButtonVisible: false,
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
    this.setState({ isSortedUp: !this.state.isSortedUp, sortKey: key }, () => {
      this.getSortedRows()
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
}

export { Search }