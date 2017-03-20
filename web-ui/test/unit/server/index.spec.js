'use strict';

const chai = require('chai');
const expect = chai.expect;
const sinon = require('sinon');
const sinonChai = require('sinon-chai');
const proxyquire = require('proxyquire');
chai.use(sinonChai);


describe('server.index', () => {

  /*eslint-disable no-unused-vars*/

  let index, createHttpServer, env, logger, webpack, webpackConfig, express, listen, listenSpy, app, appSpy;

  before(() => {

    listen = (port, cb) => {
      cb();
    };

    createHttpServer = () => {
      listenSpy = sinon.spy(listen);
      return {
        'listen': listenSpy
      };
    };

    env = {
      'PORT': 3000,
      'NODE_ENV':'development'
    };

    logger = {
      info: () => {}
    };

    webpack = () => {
      return 'compiler';
    };

    webpackConfig = {};

    app = {
      get: sinon.spy(),
      use: sinon.spy()
    };

    express = () => {
      return app;
    };

    sinon.spy(logger, 'info');
    listenSpy = sinon.spy(listen);

    process.env.NODE_ENV = 'development';

    index = proxyquire('../../../src/server/index', {
      './create-http-server': createHttpServer,
      './logger': logger,
      './env': env,
      'webpack': webpack,
      '../../webpack.config': webpackConfig,
      'express': express
    });

  });

  it('should start the server on env.PORT', () => {
    expect(logger.info).to.have.been.calledOnce;
    expect(listenSpy).to.have.been.calledOnce;
    expect(listenSpy).to.have.been.calledWith(3000);
  });

});
