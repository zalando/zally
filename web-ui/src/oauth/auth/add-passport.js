'use strict';

const env = require('../../server/env');
const cookieSession = require('cookie-session');
const passport = require('passport');
const cookieParser = require('cookie-parser');
const {
  ZalandoOAuthStrategy,
  CredentialsLoader,
} = require('../../../packages/passport-oauth2-zalando/dist');
const { simple } = require('../passport/credentials-loader');
const kubernetes = CredentialsLoader.kubernetes;

module.exports = app => {
  const OAUTH_SCOPES = (function(scopes) {
    if (Array.isArray(scopes)) {
      return scopes;
    }
    if (typeof scopes === 'string') {
      return scopes.split(',');
    }
    return [];
  })(env.OAUTH_SCOPES || []);

  const strategy = new ZalandoOAuthStrategy(
    {
      clientID: env.OAUTH_CLIENT_ID,
      clientSecret: env.OAUTH_CLIENT_SECRET,
      scope: OAUTH_SCOPES,
      authorizationURL: env.OAUTH_AUTHORIZATION_URL,
      tokenURL: env.OAUTH_ACCESS_TOKEN_URL,
      profileURL: env.OAUTH_TOKENINFO_URL,
      callbackURL: `${env.OAUTH_REDIRECT_URL}`,
      credentialsLoader:
        env.CREDENTIALS_LOADER === 'kubernetes'
          ? kubernetes(
              env.CREDENTIALS_CLIENT_ID_PATH,
              env.CREDENTIALS_CLIENT_SECRET_PATH
            )
          : simple(env.OAUTH_CLIENT_ID, env.OAUTH_CLIENT_SECRET),
    },
    (accessToken, refreshToken, profile, done) => {
      const user = {
        username: profile.name,
        provider: profile.provider,
        accessToken: accessToken,
        refreshToken: refreshToken,
      };
      done(null, user);
    }
  );

  strategy.getClientId = function() {
    return this._oauth2._clientId;
  };

  strategy.getClientSecret = function() {
    return this._oauth2._clientSecret;
  };

  app.set('passportStrategy', strategy);

  // ADD COOKIE SUPPORT
  app.use(cookieParser());
  app.use(
    cookieSession({
      name: 'session',
      secret: env.SESSION_SECRET,
      sameSite: true,
      maxAge: 24 * 60 * 60 * 1000, // 24 hours
    })
  );

  // PASSPORT CONFIGURATION
  passport.use(strategy);
  passport.serializeUser((user, done) => done(null, user));
  passport.deserializeUser((user, done) => done(null, user));

  // PASSPORT INITIALIZE
  app.use(passport.initialize());
  app.use(passport.session());
};
