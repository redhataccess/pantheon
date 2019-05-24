import * as React from 'react';

interface IChrome {
  header: React.ReactNode;
  sidebar: React.ReactNode;
}

export const Chrome: React.FunctionComponent<IChrome> = (props) => {
  return (
    <div className="pf-c-page" id="page">
      {props.header}
      {props.sidebar}
      {props.children}
    </div>
  );
}
