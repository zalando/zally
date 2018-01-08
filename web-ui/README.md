[![dependencies Status](https://david-dm.org/zalando/zally-web-ui/status.svg)](https://david-dm.org/zalando/zally-web-ui)
[![codecov](https://codecov.io/gh/zalando/zally-web-ui/branch/master/graph/badge.svg)](https://codecov.io/gh/zalando/zally-web-ui)

Zally WEB-UI
============

The project provides a simple web user interface client for [Zally Rest API](https://github.com/zalando/zally), a tool to lint your api specs.

It's implemented as an [express](https://expressjs.com/) app/middleware and a Single Page Application based on [React](https://facebook.github.io/react/). 


## Main features

* lint api spec by url or using the built-in yaml/json editor
* show active/inactive supported lint rules
* optional authentication hooks

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
## Contents

- [Requirements](#requirements)
- [Install](#install)
- [Usage](#usage)
  - [Mount to an existing application](#mount-to-an-existing-application)
  - [Configuration](#configuration)
    - [Options](#options)
- [Development](#development)
  - [Install, build and run in development mode](#install-build-and-run-in-development-mode)
  - [Run in production mode](#run-in-production-mode)
  - [Build optimized client javascript bundle](#build-optimized-client-javascript-bundle)
- [Release it](#release-it)
- [Contributing](#contributing)
  - [Contact](#contact)
  - [License](#license)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Requirements

* NodeJS >= 7.6

## Install

```bash
npm install zally-web-ui --save
```
or 
```bash
yarn add zally-web-ui
```

## Usage

### Mount to an existing application

```js
const app = require('express')()
const zally = require('zally-web-ui')(/*options*/);

app.use('/api-linter', zally);
app.listen(3000, () => {
  console.log('server running at http://localhost:3000');
});
```

### Configuration

When instantiating the app you can pass an `options` object to customize the behavior. 

```js
const options = { /* ..my options.. */}
const zally = require('zally-web-ui')(options);
```

#### Options

* **windowEnv**: the windowEnv `object` contains all the values exposed to the client on `window.env` 
* **logger** (default: `console`): custom logger
* **handlers**: the handlers `object` contains all route handlers used by zally-web-ui
* **handlers.assets**: handler that serve static assets
* **handlers.windowEnv**: handler that serve `/env.js` javascript file used to expose `windowEnv` values to the client on `window.env`
* **handlers.index**: handler that serve the single page application entrypoint on the wild card `*` to allow HTML5 History API working as expected

* **PORT**: HTTP(S) Server port
* **LOG_LEVEL**: Logging level (error|warn|info|verbose|debug|silly)
* **SESSION_SECRET**: Secret used to encrypt the session cookie
* **PUBLIC_URL**: The public URL to reach the web-ui (take in mind that if OAuth is enabled `<PUBLIC_URL>/auth/callback` will be used as the route handling the OAuth authorize response, also known as `redirect_uri`)

* **ZALLY_API_URL** (default: `http://localhost:8080`): URL pointing to Zally REST API

* **OAUTH_ENABLED** (default: `false`): enable OAuth or just Auth support on the client side (an http call will be fired on `/auth/me` endpoint to get the current logged in user, if any)
* **OAUTH_CLIENT_ID**: OAuth client id assigned to your app
* **OAUTH_CLIENT_SECRET**: OAuth client secret assigned to your app
* **OAUTH_TOKENINFO_URL**: The url used to validate the access token and retrieve token informations
* **OAUTH_REFRESH_TOKEN_URL**: The url used to refresh the access token
* **OAUTH_ACCESS_TOKEN_URL**: The url used to get an access token from an authorization code
* **OAUTH_SCOPES**: Comma separated list of scopes that the user should grant to the app
* **OAUTH_USERNAME_PROPERTY**: Property that can be found in the /tokeninfo response representing the username of the connected user (eg. `uid` or `user.uid` if nested)

* **CREDENTIALS_LOADER** : Which credentials loader the app should use to read OAUTH_CLIENT_ID and OAUTH_CLIENT_SECRET. The default is called `simple` (just get `OAUTH_CLIENT_ID` and `OAUTH_CLIENT_SECRET` from env variables),
  or it can be set to `kubernetes` to read rotating credentials from the file system.
* **CREDENTIALS_CLIENT_ID_PATH**: file system absolute path to client id file (when `CREDENTIALS_LOADER=kubernetes`)
* **CREDENTIALS_CLIENT_SECRET_PATH** :file system absolute path to client secret file (when `CREDENTIALS_LOADER=kubernetes`)

* **DEBUG**: debug flag (eg. on the client side log debug messages to console)


## Development

> A Zally Rest Api server **MUST** be running on your local machine or somewhere over the network. <br>
 Use `windowEnv.ZALLY_API_URL` configuration option to set the desired value.

### Install, build and run in development mode

```
yarn
yarn dev
```

> The `yarn dev` task starts the application server in development mode with **nodemon** and **webpack-dev-server** watching for changes.<br>
  The application server acts as a proxy to webpack-dev-server as the target.

### Run in production mode

```
yarn build
yarn start
```

### Build optimized client javascript bundle

Build webpack bundle minified and source-map file(s).

```
yarn build
```

## Release it

1. Create a pull request for the new version (e.g. branch`web-ui-release-1.1`)
    1. Bump the package version in `package.json`
    1. Add changelog information and adjust documentation
1. Publish the new version after the pull request is merged (`npm login && npm publish --access=public`)

## Contributing

People interested contributing to the web-ui project can open issues and related pull requests. 

Before opening PRs, be sure the test are running by executing `yarn test`.

### Contact

Feel free to contact one the [maintainers](MAINTAINERS)

### License

MIT license with an exception. See [license file](LICENSE).
