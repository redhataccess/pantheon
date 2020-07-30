require("dotenv").config();

// If we have a .env file respect the HOST and PORT settings
// If not default to localhost
const HOST = process.env.HOST || 'localhost';
// If not default to 9595
const PORT = process.env.PORT || 9595;

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
      },
      path: "/"
    },

    "/content": {
      host: {
        local: "http://localhost:8181"
      }
    },
    "/system": {
      host: {
        local: "http://localhost:8181"
      }
    },
    "/pantheon": {
      host: {
        local: "http://localhost:8181"
      }
    },
    "/j_security_check": {
      host: {
        local: "http://localhost:8181"
      }
    }
    // Route a URL path to an app server, and watch local files for changes.
    // This is most useful for putting a local development at a certain
    // path on your spandx server.  Includes browser-sync auto-reloading.
    // '/': { host: 'http://localhost:8080/', watch: '~/projects/my-app' },
  }
};

