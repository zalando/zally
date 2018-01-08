import {
  InternalOAuthError,
  Strategy as OAuth2Strategy,
} from 'passport-oauth2';
import { ZalandoProfile, withDefaultOptions } from '../';
import * as util from 'util';

export function ZalandoOAuthStrategy(options, verify) {
  options = withDefaultOptions(options);
  OAuth2Strategy.call(this, options, verify);
  this._profileURL = options.profileURL;
  this.name = 'zalando';
  options.credentialsLoader((error, credentials) => {
    if (error) return options.onCredentialsError(error);
    this._oauth2._clientId = credentials.clientId || this._oauth2._clientId;
    this._oauth2._clientSecret =
      credentials.clientSecret || this._oauth2._clientSecret;
  });
}

util.inherits(ZalandoOAuthStrategy, OAuth2Strategy);

ZalandoOAuthStrategy.prototype.userProfile = function(accessToken, done) {
  this._oauth2.get(
    this._profileURL,
    accessToken,
    (err, body) =>
      err
        ? done(new InternalOAuthError('Failed to fetch user profile', err))
        : done(null, ZalandoProfile.parse(body))
  );
};
