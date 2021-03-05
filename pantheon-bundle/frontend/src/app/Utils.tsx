export class Utils {
    
    /**
     * fetchHelper
     * @param endpoint: string
     * @param options: object     
     */

    public fetchHelper = (endpoint: string, options: object) => {
    
        if (options !== null) {
            return fetch(endpoint, options)
              .then(this.handleErrors)
              .then(response => response.json())
        } else {
            return fetch(endpoint)
              .then(this.handleErrors)
              .then(response => response.json())
        }
    }

    /**
     * handleErrors
     * @param response     
     */
    public handleErrors = (response) => {
        if (!response.ok) {
            throw Error(response.statusText)
        }
        return response
    }
}
   