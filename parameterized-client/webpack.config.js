require('webpack');
const path = require('path');

module.exports = {
    mode: 'production',
    optimization: {
      minimize: false
    },
    entry: {
        'static/parameterized_build_hook': ['./src/parameterized-build-hook.js'],
    },
    output: {
        path: path.join(__dirname, '../src/main/resources/'),
        filename: '[name].pack.js'
    },
    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                use: {
                    loader: 'babel-loader',
                    options: {
                        presets: ['babel-preset-env'],
                    },
                },
            },
            {
                test: /\.soy$/,
                exclude: /node_modules/,
                use: {
                    loader: '@atlassian/atlassian-soy-loader',
                    options: {
                        dontExpose: false
                    }
                }
            }
        ],
    },
    plugins: [
    ],
    externals: [
        'aui'
    ],
};