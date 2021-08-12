import React from 'react';
import "@app/app.css";
import { Alert } from '@patternfly/react-core';

export interface IErrorBoundaryProps {
    hasError: boolean
}

class ErrorBoundary extends React.Component<IErrorBoundaryProps, any> {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error) {
    // Update state so the next render will show the fallback UI.
    return { hasError: true };
  }

  componentDidCatch(error) {
    // Log error
    console.log(error);
  }

  render() {
    if (this.state.hasError) {
      // Render fallback UI
      return(
        <React.Fragment>
     
        <Alert isInline variant="danger" title="An Error Has Occurred.">
          <p>
            Please click{' '}
            <a href="/pantheon">here</a>
           {' '} to return to the homepage.
          </p>
        </Alert>
       
      </React.Fragment>
      )
    }

    return this.props.children; 
  }
}

export { ErrorBoundary }; 

