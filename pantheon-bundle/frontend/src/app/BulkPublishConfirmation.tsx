import React from 'react';
import { Modal, ModalVariant, Button, Title, TitleSizes, AlertActionCloseButton, Alert, AlertActionLink, Progress, ProgressVariant, ProgressSize, List, ListItem, ProgressMeasureLocation, ListComponent, OrderType } from '@patternfly/react-core';
import "@app/app.css";

export interface IBulkPublishProps {
  header: string
  subheading: string
  updateSucceeded: string
  updateFailed: string
  updateIgnored: string
  footer: string
  progressSuccessValue: number
  progressFailureValue: number
  progressWarningValue: number
  onShowBulkOperationConfirmation: (showBulkConfirmation) => any
  isBulkUnpublish: boolean
}

class BulkPublishConfirmation extends React.Component<IBulkPublishProps, any>{
  constructor(props) {
    super(props);
    this.state = {
      isModalOpen: false,
    };

  }

  render() {
    const { isModalOpen } = this.state;

    const header = (
      <React.Fragment>
        <Title id="bulk-operation-more-details" headingLevel="h1" size={TitleSizes['2xl']}>
          {this.props.header}
        </Title>
        <p className="pf-u-pt-sm">{this.props.subheading}</p>
      </React.Fragment>
    );

    const footer = (
      <Title headingLevel="h4" size={TitleSizes.md}>
        <span className="pf-u-pl-sm">{this.props.footer}</span>
      </Title>
    );

    return (
      <React.Fragment>
        <div className="p2-search__pf-c-alert">
          <Alert
            variant="info"
            title={this.props.isBulkUnpublish ? "Bulk Unpublish" : "Bulk Publish"}
            actionClose={<AlertActionCloseButton data-testid="hide-alert-button" onClose={this.hideAlert} />}
            actionLinks={
              <React.Fragment>
                <AlertActionLink data-testid="view-details-link" onClick={this.handleModalToggle}>View details</AlertActionLink>
              </React.Fragment>
            }
          >
            <div><Progress value={this.props.progressSuccessValue} title={this.props.isBulkUnpublish ? "Unpublish Successful" : "Publish Successful"} variant={ProgressVariant.success} size={ProgressSize.sm} /></div>
            <div><Progress value={this.props.progressFailureValue} title={this.props.isBulkUnpublish ? "Unpublish Failed" : "Publish Failed (metadata missing)"} variant={ProgressVariant.danger} size={ProgressSize.sm} /></div>
            {!this.props.isBulkUnpublish && <div><Progress value={this.props.progressWarningValue} title="Publish Failed (no draft version found)" variant={ProgressVariant.warning} size={ProgressSize.sm} /></div>}
          </Alert>
        </div>
        <Modal
          variant={ModalVariant.large}
          isOpen={isModalOpen}
          header={header}
          aria-label="confirmation dialog"
          aria-labelledby="confirmation-header-label"
          aria-describedby="confirmation-header-description"
          onClose={this.handleModalToggle}
          footer={footer}
        >
          <strong>{`${this.props.isBulkUnpublish ? "Unpublished" : "Published"} Succeessfully:`}</strong>
          <br />
          <span id="update-succeeded">
            <List aria-label="succeeded" component={ListComponent.ol} type={OrderType.number}>
              {this.props.updateSucceeded.length > 0 &&
                this.props.updateSucceeded.split(",").map((data, index) => (
                  data.length > 0 &&
                  <ListItem key={index}>{data}</ListItem>
                ))}
            </List>
          </span>
          <br />
          <br />
          {!this.props.isBulkUnpublish && (<div><strong>Publish Ignored:</strong>
          <span id="update-ignored">
            <List aria-label="ignored" component={ListComponent.ol} type={OrderType.number}>
              {this.props.updateIgnored.length > 0 &&
                this.props.updateIgnored.split(",").map((data, index) => (
                  data.length > 0 &&
                  <ListItem key={index}>{data}</ListItem>
                ))}
            </List>
          </span></div>)}
          <br />
          <br />
          <strong>{`${this.props.isBulkUnpublish ? "Unpublish" : "Publish"} Failed:`}</strong>
          <br />
          <span id="update-failed">
            <List aria-label="failed" component={ListComponent.ol} type={OrderType.number}>
              {this.props.updateFailed.length > 0 &&
                this.props.updateFailed.split(",").map((data, index) => (
                  data.length > 0 &&
                  <ListItem key={index}>{data}</ListItem>
                ))}
            </List>
          </span>
          <br />
          <br />

        </Modal>
      </React.Fragment>
    );
  }

  private handleModalToggle = () => {
    this.setState(({ isModalOpen }) => ({
      isModalOpen: !isModalOpen
    }));
  };

  private hideAlert = () => {
    this.props.onShowBulkOperationConfirmation(false)
    //TODO: refresh documentsSelected
    // this.SearchResults.current.doSearch()
  }
}

export { BulkPublishConfirmation }