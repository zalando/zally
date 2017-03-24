'use strict';

const chai = require('chai');
const expect = chai.expect;
const sinon = require('sinon');
const sinonChai = require('sinon-chai');
const proxyquire = require('proxyquire').noCallThru();
chai.use(sinonChai);

describe('server.zally-api-handler', () => {

  /*eslint-disable no-unused-vars*/

  const apiDefinitionURL = 'https://example.com/api-definition';
  let env, request, logger, zallyApiHandler, req, res, fetch, fetchError;

  before(() => {

    env = {
      'ZALLY_API_URL': 'https://example.com'
    };

    request = sinon.spy();

    logger = {
      debug: () => {},
      error: () => {}
    };

    req = {
      url: '/zally-api/path-to-api',
      body: {
        api_definition: apiDefinitionURL
      },
      headers: {
        authorization: ''
      }
    };

    res = {
      json: () => {},
      status: () => {}
    };

    fetch = sinon.spy(() => {
      return {
        text: () => {
          return '{"foo": "bar"}';
        },

        json: () => {
          return { "baz": "qux"};
        }
      }
    });

    sinon.spy(logger, 'debug');
    sinon.spy(logger, 'error');
    sinon.spy(res, 'json');
    sinon.spy(res, 'status');

    zallyApiHandler = proxyquire('../../../src/server/zally-api-handler',{
      './env': env,
      './logger': logger,
      './fetch': fetch
    });

    zallyApiHandler(req, res);

  });

  it('should export a function', () => {
    expect(zallyApiHandler).to.be.a.function;
  });

  describe('when invoking the function', () => {

    it('should log a debug message for fetching schema', () => {
      expect(logger.debug).to.have.been.calledWith(`Fetch swagger schema: ${apiDefinitionURL}`);
    });

    it('should fetch apiDefinitionURL', () => {
      expect(fetch).to.have.been.calledWith(`${apiDefinitionURL}`);
    });

    it('should log a debug message for parse schema', () => {
      expect(logger.debug).to.have.been.calledWith(`Parse schema: ${apiDefinitionURL}`);
    });

    it('should fetch violations', () => {
      expect(fetch).to.have.been.calledWith('https://example.com/path-to-api');

    });

    it('should respond with violations as json', () => {
      expect(res.json).to.have.been.calledWith({"baz": "qux"});
    })

  });

  describe('when invoking the function and encountering an error', () => {

    before(() => {

     fetchError = sinon.spy(() => {
        throw { message: 'Test Exception', status: 500};
      });

      zallyApiHandler = proxyquire('../../../src/server/zally-api-handler',{
        './logger': logger,
        './fetch': fetchError
      });

      res.json.reset();
      res.status.reset();
      logger.error.reset();

      zallyApiHandler(req, res);

    });

    it('should log the error', () => {
      expect(logger.error).to.have.been.calledOnce;
      expect(logger.error).to.have.been.calledWith({message: 'Test Exception', status: 500});

    });

    it('should respond with the status of error and error message', () => {
      expect(res.status).to.have.been.calledOnce;
      expect(res.status).to.have.been.calledWith(500);
      expect(res.json).to.have.been.calledOnce;
      expect(res.json).to.have.been.calledWith({
        type: 'about:blank',
        title: 'Test Exception',
        detail: 'Test Exception',
        status: 500
      });
    })
  });

});
