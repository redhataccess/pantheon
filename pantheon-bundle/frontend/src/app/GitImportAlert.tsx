import React, { useContext, useEffect, Component, useState } from "react";
import { AlertGroup, Alert, AlertActionCloseButton } from "@patternfly/react-core";
import "@app/app.css";
import { GitImportContext } from "@app/contexts/GitImportContext"


export default function GitImportAlert(props: any) {

const [reposUploaded, setReposUploaded] = useState([]as any)

const { uploads } = useContext(GitImportContext);
useEffect(()=> {
  setReposUploaded(uploads)

}, [uploads])
console.log('GITIMPORTALET', uploads)
return (
  <AlertGroup isToast className="git-import-alert-group">
  {/* { reposUploaded.length > 0 && reposUploaded.map(upload =>  */}
    <Alert
    title="test">
     <p>Repository name: </p>
     <p>Total files uploaded: </p>
   </Alert>
   {reposUploaded.map(u => <p>{u.repoName}</p>)}
  {/* )} */}
   </AlertGroup>
);

  return (
       <AlertGroup isToast className="git-import-alert-group">
       { reposUploaded.length > 0 && reposUploaded.map(upload => 
         <Alert
         isLiveRegion
          variant={reposUploaded.success === true ? "success" : "danger"}
          title={reposUploaded.success === true ? "Git Import Successful" : "Git Import Failed"}
          key={`alert-${reposUploaded.repoName}`}
          timeout
          // actionClose={<AlertActionCloseButton onClose={() => alert('Clicked the close button')} />}

        >
          <p>Repository name: {reposUploaded.repoName}</p>
          <p>Total files uploaded: {reposUploaded.totalFiles}</p>
        </Alert>
       )}
        </AlertGroup>
  );
}
