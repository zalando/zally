'use strict';

const chai = require('chai');
const expect = chai.expect;
const sinon = require('sinon');
const sinonChai = require('sinon-chai');
const proxyquire = require('proxyquire');
chai.use(sinonChai);

describe('server.env-handler', () => {

  let env, request, logger, envHandler, req, res, fs, path;

  before(() => {
    request = sinon.spy();

    logger = {
      debug: () => {},
      error: () => {}
    };

    req = {
      pipe: () => {
        return req;
      }
    };

    res = {
      setHeader: () => {},
      write: () => {},
      end: () => {}
    };

    fs = {

    };

    path = {};

    sinon.spy(logger, 'debug');
    sinon.spy(logger, 'error');
    sinon.spy(req, 'pipe');

  });

  before(() => {

    env = {
      public: () => {
        return {foo: 'bar'};
      }
    };

    envHandler = proxyquire('../../../src/server/env-handler',{
      './env': env,
      'request': request,
      './logger': logger
    });

  });

  it('should export a function', () => {
    expect(envHandler).to.be.a.function;
  });

  describe('when invoking the function without ZALANDO_OAUTH && CREDENTIALS_DIR', () => {
    before(() => {
      sinon.spy(res, 'setHeader');
      sinon.spy(res, 'write');
      sinon.spy(res, 'end');
      envHandler(req, res);
    });

    it('should say true is true', () => {
      expect(true).to.be.true;
    });

    it('should send the response', () => {
      expect(res.setHeader).to.have.been.calledOnce;
      expect(res.setHeader).to.have.been.calledWith('content-type', 'text/javascript');
      expect(res.write).to.have.been.calledOnce;
      expect(res.write).to.have.been.calledWith('window.env = {"foo":"bar"}');
      expect(res.end).to.have.been.calledOnce;
    });

  });

  describe('when invoking the function with ZALANDO_OAUTH && CREDENTIALS_DIR and cannot read clientJSON', () => {
    before(() => {
      env = {
        public: () => {
          return {foo: 'bar'};
        },
        'ZALANDO_OAUTH': 'zalandoOauth',
        'CREDENTIALS_DIR': 'credentialsDir'
      };

      fs = {
        readFile: (path, cb) => {
          cb({message: 'Dummy file read error.'}, null);
        }
      };

      path = {
        join: () => {
          return 'clientJsonFilePathWithError';
        }
      };

      sinon.spy(fs, 'readFile');
      sinon.spy(path, 'join');

      res.setHeader.reset();
      res.write.reset();
      res.end.reset();
      logger.error.reset();
      logger.debug.reset();


      envHandler = proxyquire('../../../src/server/env-handler',{
        './env': env,
        'request': request,
        './logger': logger,
        'fs': fs,
        'path': path
      });

      envHandler(req, res);

    });

    it('should create clientJSONPath', () => {
      expect(path.join).to.have.been.calledOnce;
      expect(path.join).to.have.been.calledWith('credentialsDir', 'client.json');
    });

    it('should log a debug message', () => {
      expect(logger.debug).to.have.been.calledOnce;
      expect(logger.debug).to.have.been.calledWith('Reading clientJsonFilePathWithError');
    });

    it('should read file', () => {
      expect(fs.readFile).to.have.been.calledOnce;
      expect(fs.readFile).to.have.been.calledWith('clientJsonFilePathWithError');
    });

    it('should log error', () => {
      expect(logger.error).to.have.been.calledTwice;
      expect(logger.error).to.have.been.calledWith('Cannot read client.json!');
    });


    it('should send a response', () => {
      expect(res.setHeader).to.have.been.calledOnce;
      expect(res.setHeader).to.have.been.calledWith('content-type', 'text/javascript');
      expect(res.write).to.have.been.calledOnce;
      expect(res.write).to.have.been.calledWith('window.env = {"foo":"bar"}');
      expect(res.end).to.have.been.calledOnce;
    });
  });

  describe('when invoking the function with ZALANDO_OAUTH && CREDENTIALS_DIR and cannot parse clientJson file data', () => {
    before(() => {
      env = {
        public: () => {
          return {foo: 'bar'};
        },
        'ZALANDO_OAUTH': 'zalandoOauth',
        'CREDENTIALS_DIR': 'credentialsDir'
      };

      fs = {
        readFile: (path, cb) => {
          cb(null, 'A non json data.');
        }
      };

      path = {
        join: () => {
          return 'clientJsonFilePath';
        }
      };

      sinon.spy(fs, 'readFile');
      sinon.spy(path, 'join');
      res.setHeader.reset();
      res.write.reset();
      res.end.reset();
      logger.error.reset();
      logger.debug.reset();

      envHandler = proxyquire('../../../src/server/env-handler',{
        './env': env,
        'request': request,
        './logger': logger,
        'fs': fs,
        'path': path
      });

      envHandler(req, res);

    });

    it('should create clientJSONPath', () => {
      expect(path.join).to.have.been.calledOnce;
      expect(path.join).to.have.been.calledWith('credentialsDir', 'client.json');
    });

    it('should log a debug message', () => {
      expect(logger.debug).to.have.been.calledOnce;
      expect(logger.debug).to.have.been.calledWith('Reading clientJsonFilePath');
    });

    it('should read file', () => {
      expect(fs.readFile).to.have.been.calledOnce;
      expect(fs.readFile).to.have.been.calledWith('clientJsonFilePath');
    });

    it('should log json parse error', () => {
      expect(logger.error).to.have.been.calledOnce;
      expect(logger.error).to.have.been.calledWith('Cannot parse client.json');
    });

    it('should send a response', () => {
      expect(res.setHeader).to.have.been.calledOnce;
      expect(res.setHeader).to.have.been.calledWith('content-type', 'text/javascript');
      expect(res.write).to.have.been.calledOnce;
      expect(res.write).to.have.been.calledWith('window.env = {"foo":"bar"}');
      expect(res.end).to.have.been.calledOnce;
    });

  });

  describe('when invoking the function with ZALANDO_OAUTH && CREDENTIALS_DIR and can successfully read clientJson file', () => {
    before(() => {
      env = {
        public: () => {
          return {foo: 'bar'};
        },
        'ZALANDO_OAUTH': 'zalandoOauth',
        'CREDENTIALS_DIR': 'credentialsDir'
      };

      fs = {
        readFile: (path, cb) => {
          cb(null, '{"client_id": "id:123"}');
        }
      };

      path = {
        join: () => {
          return 'clientJsonFilePath';
        }
      };

      sinon.spy(fs, 'readFile');
      sinon.spy(path, 'join');
      res.setHeader.reset();
      res.write.reset();
      res.end.reset();
      logger.error.reset();
      logger.debug.reset();

      envHandler = proxyquire('../../../src/server/env-handler',{
        './env': env,
        'request': request,
        './logger': logger,
        'fs': fs,
        'path': path
      });

      envHandler(req, res);

    });

    it('should create clientJSONPath', () => {
      expect(path.join).to.have.been.calledOnce;
      expect(path.join).to.have.been.calledWith('credentialsDir', 'client.json');
    });

    it('should log a debug message', () => {
      expect(logger.debug).to.have.been.calledOnce;
      expect(logger.debug).to.have.been.calledWith('Reading clientJsonFilePath');
    });

    it('should read file', () => {
      expect(fs.readFile).to.have.been.calledOnce;
      expect(fs.readFile).to.have.been.calledWith('clientJsonFilePath');
    });

    it('should send a response', () => {
      expect(res.setHeader).to.have.been.calledOnce;
      expect(res.setHeader).to.have.been.calledWith('content-type', 'text/javascript');
      expect(res.write).to.have.been.calledOnce;
      expect(res.write).to.have.been.calledWith('window.env = {"foo":"bar","OAUTH_CLIENT_ID":"id:123"}');
      expect(res.end).to.have.been.calledOnce;
    });

  });

});
