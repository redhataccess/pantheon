import React, { useContext, useEffect, Component, useState } from "react";
import { AlertGroup, Alert } from "@patternfly/react-core";
import "@app/app.css";
import { GitImportContext, IGitImport } from "@app/contexts/GitImportContext"

export default function GitImportAlert(props: any) {
  const [uploadsDisplayed, setUploadsDisplayed] = useState([] as IGitImport[]);
  const { uploads } = useContext(GitImportContext);
  const uploadsCopy = [...uploads];

  useEffect(() => {
    for (let i = 0; i < uploadsDisplayed.length; i++) {
      if (uploadsDisplayed[i].id === uploadsCopy[i].id) {
        uploadsCopy.splice(i, 1);
      }
    }
    setUploadsDisplayed([...uploads]);
  }, [uploads]);

  return (
    <AlertGroup isToast className="git-import-alert-group">
      {(uploadsCopy && uploadsCopy.length > 0) && uploadsCopy.map((upload: any) => {
        return <Alert
          isLiveRegion
          variant={upload.success === true ? "success" : "danger"}
          title={upload.success === true ? "Git Import Successful" : "Git Import Failed"}
          key={`alert-${upload.repoName}`}
          timeout>
          <p>Repository name: {upload.repoName}</p>
          <p>Total files uploaded: {upload.totalFiles}</p>
        </Alert>})}
    </AlertGroup>
  );
}
