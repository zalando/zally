const app = require('express')();
const env = require('./src/server/env');
const logger = require('./src/server/logger');
const webpackDevServerProxy = require('./src/server/dev/webpack-dev-server-proxy');
const { addAuth, requireLogin } = require('./src/oauth/auth');

/**
 * Proxy to webpack-dev-server for development
 */
if (env.NODE_ENV === 'development') {
  logger.debug('Use webpack dev proxy.');
  webpackDevServerProxy(app, require('./webpack/dev'));
}

const oauthEnabled =
  env.OAUTH_ENABLED && env.OAUTH_ENABLED === 'true' ? true : false;
const ZALLY_API_URL = oauthEnabled ? '/zally-api' : env.ZALLY_API_URL;

const zally = require('./src/server')({
  windowEnv: {
    OAUTH_ENABLED: oauthEnabled,
    ZALLY_API_URL,
    DEBUG: env.DEBUG,
  },
});

if (oauthEnabled) {
  logger.info('OAuth is enabled.');
  // Add auth support
  addAuth(app);
  // Proxy zally api to avoid CORS restriction and add authorization token
  app.use(
    '/zally-api',
    requireLogin(),
    require('./src/server/zally-api-handler')
  );
}

/**
 * Health check
 */
app.get('/health', (req, res) => {
  res.json({ alive: true });
});

/**
 * Mount zally-web-ui app
 */
app.use(zally);

/**
 * Basic generic error handling
 */
app.use(function(err, req, res, next) {
  logger.error(JSON.stringify(err), err);
  res.status(err.status || 500);
  res.json(err);
});

const port = parseInt(env.PORT) || 3000;

app.listen(port, () => {
  logger.info(`zally-web-ui running at http://localhost:${port}`);
});
