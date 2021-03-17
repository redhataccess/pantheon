require("dotenv").config();

// If we have a .env file respect the HOST and PORT settings
// If not default to localhost
const HOST = process.env.HOST || 'localhost';
// If not default to 9595
const PORT = process.env.PORT || 9595;
// Proxy for Pantheon Data
const PANTHEON_PROXY = process.env.PANTHEON_PROXY || 'http://localhost:8181';

module.exports = {
  host: {
    local: HOST
  },
  port: PORT,
  open: true,
  startPath: "/",
  verbose: true,
  routes: {
    // Here are some routing examples to get started.
    "/": {
      host: {
          local: "http://localhost:9000"
      }
    },

    "/content": {
      host: {
        local: PANTHEON_PROXY
      }
    },
    "/system": {
      host: {
        local: PANTHEON_PROXY
      }
    },
    "/pantheon/builddate.json": {
      host: {
        local: PANTHEON_PROXY
      }
    },
    "/pantheon/internal/modules.json": {
      host: {
        local: PANTHEON_PROXY
      }
    },
    "/pantheon": {
      host: {
        local: PANTHEON_PROXY
      }
    },
    "/conf": {
      host: {
        local: PANTHEON_PROXY
      }
    },
    "/api": {
      host: {
      local: PANTHEON_PROXY
      }
    },
    "/auth/login": {
      host: {
      local: PANTHEON_PROXY
      }
    },
    "/j_security_check": {
      host: {
        local: PANTHEON_PROXY
      }
    }
    // Route a URL path to an app server, and watch local files for changes.
    // This is most useful for putting a local development at a certain
    // path on your spandx server.  Includes browser-sync auto-reloading.
    // '/': { host: 'http://localhost:8080/', watch: '~/projects/my-app' },
  }
};

