import React, { createContext, useState, useEffect } from "react";

export let GitImportContext = createContext({} as any);

export interface IGitImportProviderProps {
  children?: React.ReactNode;
}

export interface IGitImport {
  // status: string,
  // totalFilesUploaded: number,
  // repos: Array<{}>;
  cloneRepoGit2Pantheon: () => any;
  uploads: any[];
  // Array<{ totalFiles: Number, repoName: String }>

}

export function GitImportProvider({ children }: IGitImportProviderProps) {
  // const backend = "/content/products.query.json?nodeType=pant:product&orderby=name"
  const [repo, setRepo]: any = useState([] as IGitImport[]);

  const [status, setStatus] = useState(false)
  const [uploads, setUploads] = useState([] as any)
  console.log("TEST CONTEXT UPLOADS STATE", uploads)

  const hdrs = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  }

  const cloneRepoGit2Pantheon = (branch, repository) => {
    console.log('IN CLONE FUNCTION BRANCH', branch)
    console.log('IN CLONE FUNCTION REPOSITORY', repository)

    const body = {
      'branch': branch,
      'repo': repository
    }

    fetch('http://127.0.0.1:5000/api/clone', {

      body: JSON.stringify(body),
      headers: hdrs,
      method: 'POST'
    })
      .then(response => response.text().then(text => {
        console.log('text', text)
        if (text.includes("error")) {
          console.log('error')
          setStatus(false)
          const singleUpload = {
            totalFiles: 0,
            repoName: repository,
            success: false
          }
          let uploadsStateCopy = uploads


          uploadsStateCopy.push(singleUpload)

          setUploads(uploadsStateCopy)
        } else {
          setStatus(true)
          fetchUploads(text, repository)

        }

      })


      )
      .catch(e => console.error(e));

    // }

  }

  const fetchUploads = (text, repository) => {
    fetch('http://127.0.0.1:5000/api/status', {
      body: text,
      headers: hdrs,
      method: 'POST'
    }).then(response => {
      response.json().then((json) => {
        if (json.status === "done") {
          console.log('json', json)
          // setStatus(json.status)
          const singleUpload = {
            totalFiles: json.total_files_uploaded,
            repoName: repository,
            success: true,
          }
          let uploadsStateCopy = uploads


          uploadsStateCopy.push(singleUpload)

          setUploads(uploadsStateCopy)
          return
        }
        setTimeout(() => {
          fetchUploads(text, repository)
        }, 2000);

      });
    }).catch(e => console.error(e));

  }

  // components wrapped in this provider will now have access to uploads and cloneRepoGit2Pantheon
  return <GitImportContext.Provider value={{ uploads, status, cloneRepoGit2Pantheon: cloneRepoGit2Pantheon }}>
    {children}
  </GitImportContext.Provider>
}
