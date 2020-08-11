/* eslint-env node, es6 */
/* global require */
'use strict';

/**
 * Configuration
 */

// Load dependencies
const
  {
    parallel,
    // series,
    // src,
    // dest,
    task,
    // watch,
  } = require('gulp'),
  shell = require('gulp-shell'),
  // gulpIf = require('gulp-if'),
  webpack = require('webpack'),
  webpackConfig = require('./webpack.dev.js');

  // @todo Not sure if there's a cool kid way to use spandx in gulp,
  //       currently resorting to gulp-shell calling node run
  // spandx = require('spandx');

// @todo Could possible run webpack in dev and have spandx be the server,
// instead of having webpack's server proxied by spandx... but didn't figure it out

// const runWebpack = (callback) => {
//   return new Promise((resolve, reject) => {
//     webpack(webpackConfig, (err, stats) => {
//       if (err) {
//         return reject(err)
//       }
//       if (stats.hasErrors()) {
//         return reject(new Error(stats.compilation.errors.join('\n')))
//       }
//       resolve()
//     })
//   })
// };

task(
  'startWebpack',
  shell.task('npm run webpack:dev')
);

task(
  'startSpandx',
  shell.task('npm run spandx')
);

task('default',
  parallel(
    'startSpandx',
    'startWebpack'
    // runWebpack
  )
);
