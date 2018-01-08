'use strict';

/* global jasmine */

describe('server.refresh-token-handler', () => {
  let mockDebug, mockWarn, mockFetch, refreshTokenHandler, reqMock, resMock;
  beforeEach(() => {
    jest.resetModules();
    mockDebug = jest.fn();
    mockWarn = jest.fn();
    mockFetch = jest.fn();

    jest.mock('../../env', () => ({
      OAUTH_REFRESH_TOKEN_URL: 'https://example.com',
    }));

    jest.mock('../../logger', () => ({
      debug: mockDebug,
      warn: mockWarn,
    }));

    jest.mock('../../fetch', () => mockFetch);

    reqMock = {
      query: {
        client_id: 'foo',
        refresh_token: 'bar',
      },
      app: {
        get: function() {
          return {
            getClientId: function() {
              return 'client';
            },
            getClientSecret: function() {
              return 'secret';
            },
          };
        },
      },
      user: {
        refreshToken: 'refresh-token',
      },
      session: {
        passport: {
          user: 'user',
        },
      },
    };

    resMock = {
      json: jest.fn(),
      status: jest.fn(),
    };

    refreshTokenHandler = require('../refresh-token-handler');
  });

  test('fetch refresh token url endpoint with right query parameters from the request and send back the json response', () => {
    const fetchResponse = {
      json: jest.fn(),
    };

    fetchResponse.json.mockReturnValueOnce(Promise.resolve({ ok: true }));
    mockFetch.mockReturnValueOnce(Promise.resolve(fetchResponse));

    return refreshTokenHandler(reqMock, resMock).then(token => {
      expect(mockFetch).toHaveBeenCalledWith(
        '?client_id=client&client_secret=secret&refresh_token=refresh-token',
        { method: 'POST' }
      );
      expect(resMock.json).toHaveBeenCalledWith(token);
    });
  });

  test('fetch refresh token url endpoint and if fails reject the promise and send back an error json response', done => {
    const fetchError = new Error('fetch fails');
    mockFetch.mockReturnValueOnce(Promise.reject(fetchError));
    return refreshTokenHandler(reqMock, resMock).catch(error => {
      expect(resMock.status).toHaveBeenCalledWith(400);
      expect(resMock.json).toHaveBeenCalledWith({
        title: fetchError.message,
        detail: jasmine.any(String),
      });
      expect(error).toEqual(fetchError);
      done();
    });
  });
});
