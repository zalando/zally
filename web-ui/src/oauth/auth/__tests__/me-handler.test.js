'use strict';

describe('server.auth.me-handler', () => {
  let mockLogger, mockFetch, mockReq, mockRes, meHandler;

  beforeEach(() => {
    jest.resetModules();
    mockLogger = {
      debug: jest.fn(),
      warn: jest.fn(),
    };
    mockFetch = jest.fn();
    mockReq = {
      user: {
        username: 'foo',
        accessToken: 'foo',
      },
    };
    mockRes = {
      status: jest.fn(),
      json: jest.fn(),
    };

    jest.mock('../../fetch', () => mockFetch);

    jest.mock('../../env', () => ({
      OAUTH_TOKENINFO_URL: 'https://example.com',
    }));

    jest.mock('../../logger', () => mockLogger);

    meHandler = require('../me-handler.js');
  });

  test('should resolve with a "user object" if token validation succeeded', () => {
    mockFetch.mockReturnValueOnce(Promise.resolve());
    return meHandler(mockReq, mockRes).then(me => {
      expect(me).toEqual({
        username: mockReq.user.username,
        authenticated: true,
      });
      expect(mockRes.json).toHaveBeenCalled();
    });
  });

  test('should resolve with an error if token validation fails', () => {
    const mockError = {
      status: 400,
      message: 'test me-handler fails',
    };
    mockFetch.mockReturnValueOnce(Promise.reject(mockError));
    return meHandler(mockReq, mockRes).then(error => {
      expect(error).toEqual(mockError);
      expect(mockRes.json).toHaveBeenCalled();
      expect(mockRes.status).toHaveBeenCalledWith(error.status);
    });
  });
});
