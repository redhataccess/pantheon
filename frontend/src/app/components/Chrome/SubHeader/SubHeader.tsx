import * as React from 'react';
import { Grid, GridItem } from '@patternfly/react-core';

export const SubHeader: React.FunctionComponent<{}> = () => {
  const audioElement: React.RefObject<HTMLAudioElement> = React.createRef();
  return (
    <Grid>
      <GridItem span={8}>
        <div className="pf-c-content">
          <h1>Pantheon</h1>
        </div>
      </GridItem>
      <GridItem span={4}>

      </GridItem>
    </Grid>
  );
}
