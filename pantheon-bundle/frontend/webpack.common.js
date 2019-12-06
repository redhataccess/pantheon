const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const TsconfigPathsPlugin = require('tsconfig-paths-webpack-plugin');
const Dotenv = require('dotenv-webpack');
const env = process.env.NODE_ENV;
let envPath = '';
switch (env) {
    case 'development':
        envPath = '.env.development';
        break;
    case 'testing':
        envPath = '.env.testing';
        break;
    case 'staging':
        envPath = '.env.staging';
        break;
    case 'production':
        envPath = '.env';
        break;
    default:
        envPath = '.env.default';
}
module.exports = {
    entry: {
        app: './src/index.tsx'
    },
    plugins: [
        new HtmlWebpackPlugin({
            template: './src/index.html'
        }),
        new Dotenv({
            path: envPath, // Path to .env file
            safe: true // load .env.example (defaults to "false" which does not use dotenv-safe)
        })
    ],
    module: {
        rules: [{
                test: /\.(tsx|ts)?$/,
                use: [{
                    loader: 'ts-loader'
                }]
            },
            {
                test: /\.(svg|ttf|eot|woff|woff2)$/,
                use: {
                    loader: 'file-loader',
                    options: {
                        // Limit at 50k. larger files emited into separate files
                        limit: 5000,
                        outputPath: 'fonts',
                        name: '[name].[ext]',
                    }
                },
                include: function(input) {
                    // only process modules with this loader
                    // if they live under a 'fonts' or 'pficon' directory
                    return input.indexOf('fonts') > -1 || input.indexOf('pficon') > -1;
                }
            },
            {
                test: /\.(jpe?g|png|gif)$/i,
                use: [{
                    loader: 'url-loader',
                    options: {
                        limit: 5000,
                        outputPath: 'images',
                        name: '[name].[ext]',
                    }
                }]
            },
            {
                test: /\.svg$/,
                use: [{
                    loader: 'url-loader',
                    options: {
                        limit: 5000,
                        outputPath: 'svgs',
                        name: '[name].[ext]',
                    }
                }],
                include: input => input.indexOf('background-filter.svg') > 1
            },
            {
                test: /\.svg$/,
                use: {
                    loader: 'svg-url-loader',
                    options: {}
                },
                include: function(input) {
                    // only process SVG modules with this loader if they live under a 'bgimages' directory
                    // this is primarily useful when applying a CSS background using an SVG
                    return input.indexOf('bgimages') > -1;
                }
            },
            {
                test: /\.svg$/,
                use: {
                    loader: 'raw-loader',
                    options: {}
                },
                include: function(input) {
                    // only process SVG modules with this loader when they don't live under a 'bgimages',
                    // 'fonts', or 'pficon' directory, those are handled with other loaders
                    return (input.indexOf('bgimages') === -1) &&
                        (input.indexOf('fonts') === -1) &&
                        (input.indexOf('background-filter') === -1) &&
                        (input.indexOf('pficon') === -1);
                }
            }
        ]
    },
    output: {
        filename: '[name].bundle.js',
        path: path.resolve(__dirname, 'dist')
    },
    resolve: {
        extensions: ['.ts', '.tsx', '.js', '.json'],
        plugins: [
            new TsconfigPathsPlugin({
                configFile: path.resolve(__dirname, './tsconfig.json')
            })
        ]
    },
};