'use strict';

const dotenvParseVariables = require('dotenv-parse-variables');
const dotenv = require('dotenv').config();
const dotenvParsedVariables = dotenvParseVariables(dotenv.parsed || {});

const defaults = {
  PORT: 3000,
  LOG_LEVEL: 'debug',
  SESSION_SECRET: 'this-should-be-secret',
  ZALLY_API_URL: 'http://localhost:8000',
  OAUTH_REDIRECT_URL: 'http://localhost:3000/auth/callback',
  OAUTH_ENABLED: false,
  OAUTH_AUTHORIZATION_URL: '',
  OAUTH_ACCESS_TOKEN_URL: '',
  OAUTH_TOKENINFO_URL: '',
  OAUTH_REFRESH_TOKEN_URL: '',
  OAUTH_CLIENT_ID: '',
  OAUTH_CLIENT_SECRET: '',
  CREDENTIALS_LOADER: 'simple',
  CREDENTIALS_CLIENT_ID_PATH: '',
  CREDENTIALS_CLIENT_SECRET_PATH: '',
  DEBUG: false,
};

module.exports = Object.assign(defaults, process.env, dotenvParsedVariables);
