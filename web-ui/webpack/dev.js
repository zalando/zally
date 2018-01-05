const merge = require('webpack-merge');
const common = require('./common.js');

module.exports = merge(common, {
  devtool: 'inline-source-map',
  devServer: {
    https: process.env.DEV_SSL_ENABLED,
    port: process.env.DEV_PORT || 3001,
    publicPath: '/assets/',
  },
});
