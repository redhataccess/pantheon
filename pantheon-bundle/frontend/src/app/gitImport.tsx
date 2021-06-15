import React, { useContext, useState } from "react";
import { Bullseye, Button, Alert, Modal, AlertActionCloseButton, FormGroup, TextInput, ModalVariant } from "@patternfly/react-core";
import "@app/app.css";
import { Redirect } from "react-router-dom"
import { GitImportContext } from "@app/contexts/GitImportContext"


export default function GitImport(props: any) {


  const [branch, setBranch] = useState("")
  const [isFormSubmitted, setIsFormSubmittedput] = useState(false)
  const [isMissingFields, setIsMissingFields] = useState(false)
  const [redirect, setStateRedirect] = useState(false)
  const [repository, setRepository] = useState("")

  const handleRepoInput = repository => {
    setRepository(repository);
  };

  const handleBranchInput = branch => {
    setBranch(branch);
  };

  const setRedirect = () => {
    setStateRedirect(true);
  };

  const renderRedirect = () => {
    if (redirect) {
      return <Redirect to="/" />
    } else {
      return ""
    }
  }

  const dismissNotification = () => {
    setIsMissingFields(false);
    setIsFormSubmittedput(false);
  };

  const context = useContext(GitImportContext);

  const { cloneRepoGit2Pantheon, status } = context

  const handleSubmit = () => {
    if (repository === "") {
      setIsMissingFields(true)
    } else {
      setIsFormSubmittedput(true)
      cloneRepoGit2Pantheon(branch, repository)
    }
  }

  return (
    <React.Fragment>
      <Bullseye>
        <div className="app-container">
          {isFormSubmitted && (
            <Modal
              variant={ModalVariant.small}
              title="Request submitted."
              isOpen={true}
              onClose={dismissNotification}
              actions={[<Button key="yes" variant="primary" onClick={setRedirect}>Yes</Button>,
              <Button key="no" variant="secondary" onClick={dismissNotification}>No</Button>]}>
              The git import has been submitted and the upload will begin. Please do not refresh this page, as it will interfere with the progress of your import status. Would you like to be redirected to the module library at this time?
            </Modal>)}
          <div>
            {isMissingFields && (
              <div className="notification-container">
                <Alert
                  variant="warning"
                  title="A repository url is required."
                  actionClose={<AlertActionCloseButton onClose={dismissNotification} />}
                />
              </div>
            )}
            <FormGroup
              label="Repository URL"
              fieldId="repository-url"
            >
              <TextInput id="repository-url" type="text" placeholder="Repository" value={repository} onChange={handleRepoInput} />
            </FormGroup>
            <br />
            <FormGroup
              label="Branch"
              fieldId="branch-name"
            >
              <TextInput id="branch-name" type="text" placeholder="Branch" value={branch} onChange={handleBranchInput} />
            </FormGroup>
            <br />
            <Button aria-label="Submit the repository and branch information to the git integration service." onClick={handleSubmit}>Submit</Button>
            <div>
              {renderRedirect()}
            </div>
          </div>
        </div>
      </Bullseye>
    </React.Fragment>
  );
}