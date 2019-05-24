import * as React from 'react';
import { PageHeader } from '@patternfly/react-core';
import { Brand } from '../Header/Brand/Brand';

export interface IHeaderProps {
  isNavOpen: boolean;
  onNavToggle: any;
}

export const Header: React.FunctionComponent<IHeaderProps> = ({
  isNavOpen,
  onNavToggle
}) => {
  return (
    <PageHeader
      logo={<Brand />}
      showNavToggle={true}
      isNavOpen={isNavOpen}
      onNavToggle={onNavToggle}
      />
  );
}
