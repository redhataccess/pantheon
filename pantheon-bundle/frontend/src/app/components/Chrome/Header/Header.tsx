import React from 'react'
import { PageHeader, Brand } from '@patternfly/react-core'
// import { Brand } from '../Header/Brand/Brand'
import { User }  from '../Header/User'
import { IAppState } from '@app/app'
import logo from '../../../images/Pantheon2-logo-white.png';

export interface IHeaderProps {
    isNavOpen: boolean
    onNavToggle: any
    appState: React.PropsWithChildren<IAppState>
}

export const Header: React.FunctionComponent<IHeaderProps> = ({
    isNavOpen,
    onNavToggle,
    appState
}) => {
    return (
        <PageHeader
            logo={<Brand src={logo} alt="Pantheon" className="PageHeader__Brand" />}
            logoProps={
                {
                'href': '/',
                // @todo Replace in CSS file when a project stylesheet has been setup
                'style': {'maxWidth': '220px'}
                }
            }
            toolbar={<User {...appState} />}
            showNavToggle={true}
            isNavOpen={isNavOpen}
            onNavToggle={onNavToggle}
        />
    )
}