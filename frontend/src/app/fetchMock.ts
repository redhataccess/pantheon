if (!self.fetch) {
    self.fetch = (input: RequestInfo, init?: RequestInit | undefined) => {
        const p = new Promise<Response>((resolve, reject) => {
            // just don't do anything. this exists so that our tests don't try and actually resolve fetches,
            // (which is exactly what isomorphic-fetch tried to do while we were importing it...)
            // and calling nothing here allows us to skip all of our fetch.then() and fetch.catch() code.
        })
        return p
    }
}