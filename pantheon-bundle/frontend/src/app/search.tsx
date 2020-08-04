import React, { Component, FormEvent } from 'react'
import {
  Alert, AlertActionCloseButton,
  DataList, DataListItem, DataListItemRow, DataListItemCells,
  DataListCell, Button, Modal,
  Level, LevelItem, Checkbox
} from '@patternfly/react-core'
import '@app/app.css'
import { BuildInfo } from './components/Chrome/Header/BuildInfo'
import { Pagination } from '@app/Pagination'
import { BrowserRouter as Router, Route, Link, Switch } from 'react-router-dom'
import { IAppState } from '@app/app'
import { SearchFilter } from '@app/searchFilter';
import SpinImage from '@app/images/spin.gif';
import { Fields, SlingTypesPrefixes } from '@app/Constants';

export interface ISearchState {
  alertOneVisible: boolean
  checkNextPageRow: string
  columns: string[]
  confirmDelete: boolean
  contentType: string
  deleteState: string
  filterQuery: string
  isEmptyResults: boolean
  isModalOpen: boolean
  isSearchException: boolean
  displayLoadIcon: boolean
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
  public published = "-"

  constructor(props) {
    super(props);
    this.state = {
      alertOneVisible: true,
      checkNextPageRow: "",
      columns: ['Name', 'Description', 'Source Type', 'Source Name', 'Upload Time'],
      confirmDelete: false,
      contentType: "module",
      deleteState: '',
      displayLoadIcon: true,
      filterQuery: '',
      isEmptyResults: false,
      isModalOpen: false,
      isSearchException: false,
      moduleName: '',
      modulePath: '',
      moduleType: '',
      moduleUpdatedDate: '',
      nextPageRowCount: 1,
      page: 1,
      pageLimit: 25,
      redirect: false,
      redirectLocation: '',
      results: [{ "pant:transientPath": '', "pant:dateUploaded": '', "name": "", "jcr:title": "", "jcr:description": "", "sling:transientSource": "", "pant:transientSourceName": "", "checkedItem": false,"publishedDate": "-","pant:moduleType": "-", "variant": ""}],
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
              onClick={this.newSearch}
              filterQuery={this.setQuery}
              // we could add one that monitors for changes and when is tru we run getRows. To discuss.
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
                        <span className="sp-prop-nosort" id="span-name" aria-label="column name">Title</span>
                      </DataListCell>,
                      <DataListCell key="resource source">
                        <span className="sp-prop-nosort" id="span-source-type">Published</span>
                      </DataListCell>,
                      <DataListCell key="source name">
                        <span className="sp-prop-nosort" id="span-source-name">Draft Uploaded</span>
                      </DataListCell>,
                      <DataListCell key="upload time">
                        <span className="sp-prop-nosort" id="span-upload-time" aria-label="column upload time">Module Type</span>
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
                {this.state.displayLoadIcon && (
                  <Level gutter="md">
                    <LevelItem />
                    <LevelItem>
                      <div className="notification-container">
                        <br />
                        <br />
                          <img src={SpinImage} alt="Spinlogo"/>
                        <br />
                        <br />
                      </div></LevelItem>
                    <LevelItem />
                  </Level>
                )}                            
                {!this.state.displayLoadIcon && (this.state.results.map((data, key) => (
                  <DataListItemRow id="data-rows" key={key}>
                    {this.props.userAuthenticated && !this.state.isEmptyResults &&
                      <Checkbox aria-labelledby="width-ex3-check1"
                        className="checkbox"
                        isChecked={data[Search.KEY_CHECKEDITEM]}
                        checked={data[Search.KEY_CHECKEDITEM]}
                        aria-label="controlled checkbox example"
                        id={data[Search.KEY_TRANSIENTPATH]}
                        name={data[Search.KEY_TRANSIENTPATH]}
                        onChange={this.handleDeleteCheckboxChange}
                        key={'checked_' + key}
                      />}
                    <DataListItemCells key={"cells_" + key}
                      dataListCells={[
                        <DataListCell key={"title_" + key} width={2}>
                          {this.props.userAuthenticated && data["jcr:title"] !== '-' &&
                            <Link to={data['sling:resourceType'].substring(SlingTypesPrefixes.PANTHEON.length) + "/" + data['pant:transientPath'] + "?variant=" + data.variant} key={"link_" + key}>{data["jcr:title"]}</Link>}
                          {this.props.userAuthenticated && data["jcr:title"] === '-' && data["pant:transientPath"] &&
                            <Link to={data['sling:resourceType'].substring(SlingTypesPrefixes.PANTHEON.length) + "/" + data['pant:transientPath'] + "?variant=" + data.variant} key={"link_" + key}>{data["pant:transientPath"]}</Link>}
                          {!this.props.userAuthenticated && data["jcr:title"] !== '-' &&
                            <a href={"/" + data['pant:transientPath'] + ".preview" + "?variant=" + data.variant} target="_blank">{data["jcr:title"]}</a>}
                          {!this.props.userAuthenticated && data["jcr:title"] === '-' && data["pant:transientPath"] &&
                            <a href={"/" + data['pant:transientPath'] + ".preview" + "?variant=" + data.variant} target="_blank">{data["pant:transientPath"]}</a>}
                        </DataListCell>,      
                        <DataListCell key={"published-date_" + key}>                          
                          <span>{data[Fields.PANT_PUBLISHED_DATE]}</span>
                        </DataListCell>,
                        <DataListCell key={"date-uploaded_" + key}>                              
                          <span>{data[Fields.PANT_DATE_UPLOADED]}</span>
                        </DataListCell>,
                        <DataListCell key={"module-type_" + key}>
                          <span >{data[Fields.PANT_MODULE_TYPE]}</span>
                        </DataListCell>
                      ]}
                    />
                  </DataListItemRow>
                )))}

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
                          title={"No modules found with your search"}
                          action={<AlertActionCloseButton onClose={this.dismissNotification} />}
                        />
                        <br />
                        <br />
                      </div></LevelItem>
                    <LevelItem />
                  </Level>

                )}
                {this.state.isSearchException && (
                  <Level gutter="md">
                    <LevelItem />
                    <LevelItem>
                      <div className="notification-container">
                        <br />
                        <br />
                        <Alert
                          variant="danger"
                          title={"Error in fetching search results"}
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
    const nameStr = 'name'
    this.state.results.map(data => {
      newResults.push(JSON.parse(JSON.stringify(data))) // clones the object
      if (data[Search.KEY_TRANSIENTPATH] === event.target[nameStr]) {
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
      this.newSearch()
    }
  };

  private newSearch = () => {
    this.setState({ page: 1 }, () => {
      this.doSearch()
    })
  }

  // Handle gateway timeout on slow connections.
  private doSearch = () => {
    this.setState({ displayLoadIcon: true })
    fetch(this.buildSearchUrl())
      .then(response => response.json())
      .then(responseJSON => {
        this.setState({ results: responseJSON.results, nextPageRowCount: responseJSON.hasNextPage ? 1 : 0 })
      })
      .then(() => {        
        if (JSON.stringify(this.state.results) === "[]") {
          this.setState({
            displayLoadIcon: false,
            isEmptyResults: true,
            selectAllCheckValue: false
          })
        } else {          
          this.setState({
            displayLoadIcon: false,
            isEmptyResults: false,            
            selectAllCheckValue: false,
          })          
        }
      })
      .catch(error => {
        // might be a timeout error
        this.setState({
          displayLoadIcon: false,
          isSearchException: true
        },()=>{ console.log("[doSearch] error ", error) })
        
      })
  }

  private dismissNotification = () => {
    this.setState({ isEmptyResults: false, isSearchException: false });
  };

  private setQuery = (prod: string) => {
    this.setState({ filterQuery: prod })
  };

  private buildSearchUrl() {
    let backend = "/pantheon/internal/modules.json?"
    backend += this.state.filterQuery
    if (this.state.filterQuery.trim() !== "") {
      backend += "&"
    }
    backend += "offset=" + ((this.state.page - 1) * this.state.pageLimit) + "&limit=" + this.state.pageLimit
    if (!backend.includes("Uploaded") && !backend.includes('direction')) {
      backend += "&key=Uploaded&direction=desc"
    }
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