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
    series,
    src,
    dest,
    task,
    watch,
  } = require('gulp'),
  // gulpIf = require('gulp-if'),
  sourceMaps = require('gulp-sourcemaps'),
  sass = require('gulp-sass'),
  // sassGlobbing = require('gulp-sass-globbing'),
  sassLint = require('gulp-sass-lint'),
  postCss = require('gulp-postcss'),
  autoprefixer = require('autoprefixer'),
  cssNano = require('cssnano'),
  pxToRem = require('postcss-pxtorem'),
  browserSync = require('browser-sync').create();

// File locations
const cssSource = 'scss/**/*.scss',
      cssOutput = '../../../src/main/resources/SLING-INF/content/static/';

// const isDev = process.env.NODE_ENV === 'dev';

/**
 * CSS Compilation
 */
const compileCSS = () => {
  return src(cssSource)
    // Lint first
    .pipe(sassLint())
    .pipe(sassLint.format())
    // Not sure this line is needed, leaving for now
    // .pipe(gulpIf(!isDev, sassLint.failOnError()))
    .pipe(sourceMaps.init())
    .pipe(sass())
    .pipe(
      postCss([
        pxToRem({
          'propList': ['*',],
        }),
        autoprefixer(),
      ])
    )
    // Write an unminified version with sourcemaps
    // to this directory for dev
    .pipe(sourceMaps.write())
    .pipe(dest('./'))
    .pipe(postCss([cssNano(),]))
    .pipe(dest(cssOutput));
};

// const compileCSS = series(globScss, processScss);
// const compileCSS = processScss;

/**
 * Start Browsersync
 */
task('startBrowserSync',
  () => browserSync.init({
    'server': './',
    'index': 'a-doc-styleguide.html',
  })
);

/**
 * Gulp tasks
 */
// Builds into static
task('default', compileCSS);

const watchTasks = () => {
  compileCSS();
  watch(
    cssSource,
    series(compileCSS, () => browserSync.reload())
  );

  watch('*.html', () => browserSync.reload());
};

// Starts browsersync, watches project for changes and reloads all browsers
task('watch',
  parallel(
    'startBrowserSync',
    watchTasks
  )
);
