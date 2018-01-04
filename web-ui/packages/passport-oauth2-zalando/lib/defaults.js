import * as path from 'path';
import { CredentialsLoader } from './loader';

const DEFAULT_OPTIONS = {
  clientID: 'fake',
  scope: [],
  credentialsDir:
    process.env.CREDENTIALS_DIR ||
    path.resolve(process.env.CREDENTIALS_DIR, '/credentials'),
  authorizationURL: process.env.AUTHORIZATION_URL,
  tokenURL: process.env.ACCESS_TOKEN_URL,
  profileURL: process.env.TOKEN_INFO_URL,
  onCredentialsError: error => {
    throw error;
  },
};

function defaultCredentialsLoader(options) {
  const credentialsFile = path.resolve(options.credentialsDir, 'client.json');
  return CredentialsLoader.stups(credentialsFile);
}

export function withDefaultOptions(options) {
  options = Object.assign({}, DEFAULT_OPTIONS, options);
  options.credentialsLoader =
    options.credentialsLoader || defaultCredentialsLoader(options);
  return options;
}
