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
  shell = require('gulp-shell'),
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
const
  cssSource = 'scss/**/*.scss',
  cssOutput = '../../../src/main/resources/SLING-INF/content/static/',
  asciiDocTemplates = '../../../src/main/resources/apps/pantheon/templates/haml/html5';

task(
  'compileAsciiDocs',
  parallel(
    shell.task(`asciidoctor -T ${asciiDocTemplates} -a pantheonenv=localwebassets dev-assets/ascii-doc-styleguide.adoc`),
  )
);

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
    .pipe(dest('./dev-assets/'))
    // Make production CSS and put in prod location
    .pipe(postCss([cssNano(),]))
    .pipe(dest(cssOutput));
};

// const compileCSS = series(globScss, processScss);
// const compileCSS = processScss;

/**
 * Start Browsersync
 */
const startBrowserSync = (done) => {
  browserSync.init({
    'server': './dev-assets/',
    'index': 'ascii-doc-styleguide.html',
  });
  done();
};

const reloadBrowserSync = (done) => {
  browserSync.reload();
  done();
};

/**
 * Gulp tasks
 */
// Builds dev assets in dev-assets and prod CSS in to the correct folder (see cssOutput variable)
task('default', parallel(compileCSS));

task('build:dev', parallel(compileCSS, 'compileAsciiDocs'));

const watchTasks = () => {
  compileCSS();
  watch(
    cssSource,
    series(
      compileCSS,
      reloadBrowserSync
    )
  );

  watch(
    `${asciiDocTemplates}/**/*.haml`,
    series(
      'compileAsciiDocs',
      reloadBrowserSync
    )
  );

  watch(
    'dev-assets/**/*.adoc',
    series(
      'compileAsciiDocs',
      reloadBrowserSync
    )
  );
};

// Starts browsersync, watches project for changes and reloads all browsers
task('watch',
  parallel(
    startBrowserSync,
    watchTasks
  )
);
