'use strict';

const chai = require('chai');
const expect = chai.expect;
const sinon = require('sinon');
const sinonChai = require('sinon-chai');
const proxyquire = require('proxyquire');
chai.use(sinonChai);

describe('server.create-http-server', () => {

  /*eslint-disable no-unused-vars*/

  let http, https, CreateHttpServer, env, fs, httpServer;

  before(() => {

    http = {
      createServer:  () => {}
    };

    env = {};

    sinon.spy(http, 'createServer');

    CreateHttpServer = proxyquire('../../../src/server/create-http-server',{
      'http': http,
      './env': env
    });

    httpServer = new CreateHttpServer();

  });

  describe('when env.SSL_ENABLED is not defined', function (){
    it('should create a http server', function (){
      expect(http.createServer).to.have.been.calledOnce;
    });
  });

  before(() => {

    http = {
      createServer:  () => {}
    };

    env = {
      'SSL_ENABLED': false
    };

    sinon.spy(http, 'createServer');

    CreateHttpServer = proxyquire('../../../src/server/create-http-server',{
      'http': http,
      './env': env
    });

    httpServer = new CreateHttpServer();

  });
  describe('when env.SSL_ENABLED is false', function (){
    it('should create a http server', function (){
      expect(http.createServer).to.have.been.calledOnce;
    });
  });


  describe('when env.SSL_ENABLED is true', function (){
    before(() => {

      https = {
        createServer:  () => {}
      };

      env = {
        'SSL_ENABLED': true
      };

      fs = {
        readFileSync: () => {}
      };

      sinon.spy(https, 'createServer');
      sinon.spy(fs, 'readFileSync');

      CreateHttpServer = proxyquire('../../../src/server/create-http-server',{
        'https': https,
        './env': env,
        'fs': fs
      });

      httpServer = new CreateHttpServer();

    });

    it('should read key and crt files', function (){
      expect(fs.readFileSync).to.have.been.calledTwice;
    });

    it('should create a https server', function (){
      expect(https.createServer).to.have.been.calledOnce;
    });

  });
});
