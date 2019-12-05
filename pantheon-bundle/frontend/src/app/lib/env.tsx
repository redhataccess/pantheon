import { resolve } from "path"

import { config } from "dotenv"

config({ path: resolve(__dirname, "../../../.env") })
console.log('[env] process.env.CP_HOST ' + process.env.CP_HOST)
console.log("[env] process.env.NODE_ENV " + process.env)