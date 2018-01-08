const merge = require('webpack-merge');
const common = require('./common.js');

module.exports = merge(common, {
  devtool: 'inline-source-map',
  devServer: {
    port: process.env.DEV_PORT || 3001,
    publicPath: '/assets/',
  },
});
