require('webpack');
const path = require('path');

module.exports = {
    mode: 'production',
    entry: {
        'hook/parameterized_build_hook': ['./src/hook/view.js'],
        'jenkins_settings/server_form': ['./src/jenkins_settings/view.js']
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
                        presets: ['@babel/preset-env', '@babel/preset-react'],
                    },
                },
            },
        ],
    },
    plugins: [
    ],
    externals: [
    ],
};