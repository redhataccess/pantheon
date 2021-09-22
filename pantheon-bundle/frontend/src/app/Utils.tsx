import { resolve } from "path"

export class Utils {

    /**
     * fetchHelper
     * @param endpoint: string
     * @param options: object     
     */

    static fetchHelper = (endpoint: string, options: object) => {

        if (options !== null) {
            return fetch(endpoint, options)
                .then(Utils.handleErrors)
                .then(response => response.json())
        } else {
            return fetch(endpoint)
                .then(Utils.handleErrors)
                .then(response => response.json())
        }
    }

    /**
     * handleErrors
     * @param response     
     */
    static handleErrors = (response) => {
        if (!response.ok) {
            throw Error(response.statusText)
        }
        return response
    }

    /**
     * checks if a given api end point exists for a draft node.
     * This method can be renamed to something more generic as it 
     * can be used to check any nodepath
     * @param path 
     */
    static draftExist(path) {
        let exists = false
        return fetch(path + ".json")
            .then(response => {
                if (response.ok) {
                    exists = true
                }
                return exists
            })
            .catch((error) => {
                console.log("[draftExist] error detected=>", error + " for " + path)
                return false
            })
    }
}
