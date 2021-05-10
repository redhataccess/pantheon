import React, { createContext, useState, useEffect } from "react";

export let GitImportContext = createContext({} as any);

export interface IGitImportProviderProps {
  children?: React.ReactNode;
}

export interface IGitImport {
  id: number,
  repoName: string;
  totalFiles: number;
  success: boolean;
}

/**
 * Poll API until git import is completed
 */
const poll = async ({ fn, validate, interval }) => {
  let attempts = 0;
  const executePoll = async (resolve, reject) => {
    const result = await fn();
    attempts++;
    // we need to clone so we can read the response more than once with Response.json();
    const clonedResponse = result.clone();
    if (await validate(result)) {
      return resolve(clonedResponse);
    }
    //leaving this code commented out here in case we decide to add max attempts so API call doesn't last forever
    // else if (maxAttempts && attempts === maxAttempts) {
    //   return reject(new Error('Exceeded max attempts'));
    // } 
    else {
      setTimeout(executePoll, interval, resolve, reject);
    }
  };
  return new Promise(executePoll);
};

export function GitImportProvider({ children }: IGitImportProviderProps) {
  const [uploads, setUploads] = useState([] as IGitImport[]);

  const hdrs = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  };
  let statusKey;
  let currRepository;

  // fn to clone git repo
  const cloneRepoGit2Pantheon = (branch, repository) => {
    currRepository = repository;
    const body = {
      'branch': branch,
      'repo': repository
    }
    //first fetch call will return a status key as a response if successful 
    fetch('http://127.0.0.1:5000/api/clone', {
      body: JSON.stringify(body),
      headers: hdrs,
      method: 'POST'
    }).then(response => response.text().then(text => {
      statusKey = text;
      //if first fetch fails, upload has failed a return a single upload with 0 total files
      if (text.includes("error")) {
        console.log('ERROR! in /clone');
        const singleUpload = {
          id: Date.now(),
          totalFiles: 0,
          repoName: repository,
          success: false
        };
        setUploads([...uploads, singleUpload]);
      } else {
        const validateStatus = async (response) => {
          const json = await response.json();
          return json.status === "done";
        }
        //second fetch takes the status key and returns the status of repo upload once it is complete
        //we keep making this call until status === "done" signifying the upload has completed
        const getStatus = async () => {
          return await fetch('http://127.0.0.1:5000/api/status', {

            body: statusKey,
            headers: hdrs,
            method: 'POST'
          })
        };
        poll({
          fn: getStatus,
          validate: validateStatus,
          interval: 2000
        }).then((response: any) => {
          response.json().then(json => {
            const singleUpload = {
              id: Date.now(),
              totalFiles: json.total_files_uploaded,
              repoName: repository,
              success: true
            };
            setUploads([...uploads, singleUpload]);
          });
        });
      }
    })).catch(e => console.error("ERROR! in /status", e));
  }

  // components wrapped in this provider will now have access to uploads and cloneRepoGit2Pantheon
  return <GitImportContext.Provider value={{ uploads, cloneRepoGit2Pantheon }}>
    {children}
  </GitImportContext.Provider>
}
