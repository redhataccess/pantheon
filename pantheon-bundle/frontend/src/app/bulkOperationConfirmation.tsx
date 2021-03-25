import React from 'react';
import { Modal, ModalVariant, Button, Title, TitleSizes, AlertActionCloseButton, Alert, AlertActionLink, Progress, ProgressVariant, ProgressSize, List, ListItem, ProgressMeasureLocation } from '@patternfly/react-core';
import WarningTriangleIcon from '@patternfly/react-icons/dist/js/icons/warning-triangle-icon';
import "@app/app.css";

export interface IBulkOperationProps {
  header: string
  subheading: string
  updateSucceeded: string
  updateFailed: string
  updateIgnored: string
  footer: string
  progressSuccessValue: number
  progressFailureValue: number
  progressWarningValue: number
  onShowBulkEditConfirmation: (showBulkEditConfirmation) => any
  onMetadataEditError: (metadataEditError) => any
}

class BulkOperationConfirmation extends React.Component<IBulkOperationProps, any>{
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
          <Alert
            variant="info"
            title="Bulk Edit"
            actionClose={<AlertActionCloseButton data-testid="hide-alert-button" onClose={this.hideAlert} />}
            actionLinks={
              <React.Fragment>
                <AlertActionLink data-testid="view-details-link" onClick={this.handleModalToggle}>View details</AlertActionLink>
              </React.Fragment>
            }
          >
            <div><Progress value={this.props.progressSuccessValue} title="Update Succeeded" variant={ProgressVariant.success} size={ProgressSize.sm} /></div>
            <div><Progress value={this.props.progressFailureValue} title="Update failed" variant={ProgressVariant.danger} size={ProgressSize.sm} /></div>
            <div><Progress value={this.props.progressWarningValue} title="No draft version found. No action taken" variant={ProgressVariant.warning} size={ProgressSize.sm} /></div>
          </Alert>
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
          <strong>Update Succeeded:</strong>
          <br />
          <span id="update-succeeded">
            <List aria-label="succeeded">
              {this.props.updateSucceeded.length > 0 &&
                this.props.updateSucceeded.split(",").map((data) => (
                  data.length > 0 &&
                  <ListItem>{data}</ListItem>
                ))}
            </List>
          </span>
          <br />
          <br />
          <strong>Update Ignored:</strong>
          <br />
          <span id="update-ignored">
            <List aria-label="ignored">
              {this.props.updateIgnored.length > 0 &&
                this.props.updateIgnored.split(",").map((data) => (
                  data.length > 0 &&
                  <ListItem>{data}</ListItem>
                ))}
            </List>
          </span>
          <br />
          <br />
          <strong>Update Failed:</strong>
          <br />
          <span id="update-failed">
            <List aria-label="failed">
              {this.props.updateFailed.length > 0 &&
                this.props.updateFailed.split(",").map((data) => (
                  data.length > 0 &&
                  <ListItem>{data}</ListItem>
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
    this.props.onShowBulkEditConfirmation(false)
    this.props.onMetadataEditError("")
    //TODO: refresh documentsSelected
    // this.SearchResults.current.doSearch()
  }
}

export { BulkOperationConfirmation }