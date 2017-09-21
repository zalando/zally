'use strict';

describe('server.window-env-handler', () => {
  const envHandler = require('../window-env');
  let mockWrite, res, req;

  test('should export a function', () => {
    expect(envHandler).toBeInstanceOf(Function);
  });

  describe('when invoking the function', () => {
    beforeEach(() => {
      mockWrite = jest.fn();
      res = {
        setHeader: () => {},
        write: mockWrite,
        end: () => {},
      };
      req = {
        app: {
          mountpath: '/linter',
        },
      };
    });

    test('should send the response', () => {
      envHandler({
        windowEnv: { foo: 'bar' },
      })(req, res);
      expect(mockWrite).toHaveBeenCalledWith(
        'window.env = {"foo":"bar","MOUNTPATH":"/linter/"}'
      );
    });
    test('should send the response even with no option', () => {
      envHandler()(req, res);
      expect(mockWrite).toHaveBeenCalledWith(
        'window.env = {"MOUNTPATH":"/linter/"}'
      );
    });
  });
});
