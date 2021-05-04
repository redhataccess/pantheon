import React, { useContext, useState } from "react";
import { Bullseye, Button, Alert, Modal, AlertActionCloseButton, FormGroup, TextInput, ModalVariant } from "@patternfly/react-core";
import "@app/app.css";
import { Redirect } from "react-router-dom"
import { GitImportContext } from "@app/contexts/GitImportContext"


export default function GitImport(props: any) {


  const [branch, setBranch] = useState("")
  const [git2pantheonURL, setGit2pantheonURL] = useState("")
  const [isFormSubmitted, setIsFormSubmittedput] = useState(false)
  const [isMissingFields, setIsMissingFields] = useState(false)
  const [isSucess, setIsSucess] = useState(false)
  const [redirect, setStateRedirect] = useState(false)
  const [repository, setRepository] = useState("")
  const [submitMsg, setSubmitMsg] = useState("")
  // const [status, setStatus] = useState("")

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
    if (repository === "" || branch === "") {
      setIsMissingFields(true)
    } else {
      setIsFormSubmittedput(true)
      cloneRepoGit2Pantheon(branch, repository)
      console.log('GIT IMPORT STATUS CONTEXT', status)
      // if(context[status] == "failed"){ 
      setIsSucess(status)
      // setSubmitMsg('Failed to upload repo. Please try again')
      // } 
      // else{
      //   setIsSucess(true)
      // }
    }
  }

  // const { branch, repository, isFormSubmitted, isMissingFields, isSucess } = this.state;
  return (
    <React.Fragment>
      {console.log('COTEXT TYPE', context)}
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
              The git import has been submitted. Do you want to be redirected to the module library?
            </Modal>)}
          <div>
            {isMissingFields && (
              <div className="notification-container">
                <Alert
                  variant="warning"
                  title="A repository url and branch name are required."
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

  // function cloneRepo = (postBody) => {
  //   console.log("My repo is: " + this.state.repository + " and branch is " + this.state.branch + ".")
  //   if (this.state.repository === "") {
  //     this.setState({ isMissingFields: true })
  //   } else {
  //     fetch("/conf/pantheon/pant:syncServiceUrl")
  //       .then((resp => {
  //         if (!resp.ok) {
  //           this.setState({ isFormSubmitted: true, isSucess: false, msgType: "danger", submitMsg: "Error occurred, could not find the git2pantheon URL configuration." })
  //         } else {
  //           resp.text().then((text) => {
  //             this.setState({ git2pantheonURL: text })
  //             console.log("The response text from pant:syncServiceUrl is: " + text)
  //           })
  //             .then(() => {
  //               if (this.state.git2pantheonURL === "") {
  //                 this.setState({ isFormSubmitted: true, isSucess: false, msgType: "danger", submitMsg: "Error occurred, the git2pantheon URL configuration is blank." })
  //               } else {
  //                 console.log("The git2pantheon URL is: " + this.state.git2pantheonURL)
  //                 const payload = {
  //                   branch: this.state.branch,
  //                   repo: this.state.repository
  //                 };
  //                 fetch(this.state.git2pantheonURL + "/api/clone", {
  //                   body: JSON.stringify(payload),
  //                   method: "POST"
  //                 }).then(response => {
  //                   // check 202 as well if 201 is being checked
  //                   if (response.status === 201 || response.status === 200 || response.status === 202) {
  //                     console.log(" Works " + response.status)
  //                     this.setState({ isFormSubmitted: true, isSucess: true, msgType: "success", submitMsg: "The git import has been submitted it might take up to 1 minute to see it in the module library." })
  //                   } else if (response.status === 500 || response.status === 400) {
  //                     console.log(" Failed " + response.status)
  //                     response.json().then((json) => {
  //                       this.setState({ submitMsg: json.error.details })
  //                     });
  //                   } else {
  //                     this.setState({ isSucess: false, msgType: "danger", submitMsg: "The git import submission has failed." })
  //                   }
  //                   this.setState({ isFormSubmitted: true })
  //                 })
  //                   .catch(err => {
  //                     this.setState({ isFormSubmitted: true, isSucess: false, msgType: "danger", submitMsg: "Error occurred, it is likely that react could not connect to the git2pantheon URL." })
  //                     console.log("Error occurred, it is likely that react could not connect to the git2pantheon URL. The error was:", err);
  //                   });
  //               }
  //             })
  //         }
  //       }))
  //   }
  // }