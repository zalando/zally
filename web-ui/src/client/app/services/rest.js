import { client } from './http-client.js';

export const RestService = {
  getFile(url) {
    return client
      .fetch(url)
      .then(response => response.text())
      .catch(response => response.text().then(body => Promise.reject(body)));
  },
  getApiViolations(body) {
    const options = {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    };
    return client
      .fetch(`${window.env.ZALLY_API_URL}/api-violations`, options)
      .then(response => response.json())
      .catch(response => response.json().then(body => Promise.reject(body)));
  },

  getApiViolationsByURL(apiDefinitionURL) {
    return this.getApiViolations({
      api_definition_url: apiDefinitionURL,
    });
  },

  getApiViolationsBySchema(schema) {
    return this.getApiViolations({
      api_definition_string: schema,
    });
  },

  getApiViolationsByExternalId(externalId) {
    const options = {
      method: 'GET',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
    };
    return client
      .fetch(
        `${
          window.env.ZALLY_API_URL
        }/api-violations/${externalId}`,
        options
      )
      .then(response => response.json())
      .catch(response => response.json().then(body => Promise.reject(body)));
  },

  getSupportedRules() {
    const options = {
      method: 'GET',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
    };
    const url = `${window.env.ZALLY_API_URL}/supported-rules?is_active=true`;
    return client
      .fetch(url, options)
      .then(response => response.json())
      .catch(response => response.json().then(body => Promise.reject(body)));
  },

  objectToParams(params) {
    if (params) {
      return (
        '?' +
        Object.keys(params)
          .map(k => encodeURIComponent(k) + '=' + encodeURIComponent(params[k]))
          .join('&')
      );
    }
    return '';
  },
};
