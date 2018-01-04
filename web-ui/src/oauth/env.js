'use strict';

const { stringToBool } = require('./util');
const dotenvParseVariables = require('dotenv-parse-variables').default;
const dotenv = require('dotenv').config();
const dotenvParsedVariables = dotenvParseVariables(dotenv.parsed || {});

const defaults = {
  PORT: 8442,
  LOG_LEVEL: 'debug',
  SESSION_SECRET: 'this-should-be-secret',
  PUBLIC_URL: 'http://localhost:8442',
  ZALLY_API_URL: 'https://zally.overarching.zalan.do',
  OAUTH_ENABLED: true,
  OAUTH_AUTHORIZATION_URL: '',
  OAUTH_ACCESS_TOKEN_URL: '',
  OAUTH_TOKENINFO_URL: '',
  OAUTH_REFRESH_TOKEN_URL: '',
  OAUTH_CLIENT_ID: '',
  OAUTH_CLIENT_SECRET: '',
  OAUTH_SCOPES: '',
  CREDENTIALS_LOADER: 'simple',
  CREDENTIALS_CLIENT_ID_PATH: '',
  CREDENTIALS_CLIENT_SECRET_PATH: '',
  DEBUG: false,
};

const env = stringToBool(
  Object.assign(defaults, process.env, dotenvParsedVariables)
);

module.exports = env;
