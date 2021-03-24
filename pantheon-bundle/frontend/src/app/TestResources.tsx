import { IAppState } from "./app";

const mockStateUser: IAppState = {
    isAdmin: false,
    isAuthor: false,
    isPublisher: false,
    isNavOpen: true,
    userAuthenticated: true,
    username: "demo"
}

const mockStateAdmin: IAppState = {
    isAdmin: true,
    isAuthor: true,
    isPublisher: true,
    isNavOpen: true,
    userAuthenticated: true,
    username: "admin"
}

const mockStateGuest: IAppState = {
    isAdmin: false,
    isAuthor: false,
    isPublisher: false,
    isNavOpen: true,
    userAuthenticated: false,
    username: "anonymous"
}

export { mockStateUser, mockStateAdmin, mockStateGuest }