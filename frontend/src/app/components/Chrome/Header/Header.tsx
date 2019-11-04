import React from 'react'
import { PageHeader } from '@patternfly/react-core'
import { Brand } from '../Header/Brand/Brand'
import { User }  from '../Header/User'
import { IAppState } from '@app/app'

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
            logo={<Brand />}
            toolbar={<User {...appState} />}
            showNavToggle={true}
            isNavOpen={isNavOpen}
            onNavToggle={onNavToggle}
        />
    )
}