'use strict';

const env = require('./env');
const logger = require('./logger');
const fetch = require('./fetch');
const yaml = require('js-yaml');
const ProblemError = require('./errors').ProblemError;

function parseSchema (text) {
  try {
    // try json first
    return JSON.parse(text);
  } catch(err) {
    try{
      return yaml.safeLoad(text);
    }catch(err){
      throw new ProblemError({
        type: 'about:blank',
        title: 'Error Parsing Schema',
        status: 500,
        detail: 'There was an error parsing the swagger schema.'
      });
    }
  }
}

module.exports = async function (req, res) {
  // remove the prefix found in the incoming req.url and concatenate
  // the remaining path to ZALLY_API_URL
  //
  // ex.
  // ZALLY_API_URL=https://api.zally.com
  // req.url=/zally-api/api-violations?some-filter=true
  //
  // url -> https://api.zally.com/api-violations?some-filter=true
  //
  const url = env.ZALLY_API_URL + req.url.replace('/zally-api', '');

  const apiDefinitionURL = req.body.api_definition;

  try {

    logger.debug(`Fetch swagger schema: ${apiDefinitionURL}`);
    const fetchSchemaResponse = await fetch(apiDefinitionURL);
    const schemaAsText = await fetchSchemaResponse.text();

    logger.debug(`Parse schema: ${apiDefinitionURL}`);
    const schema = parseSchema(schemaAsText);

    const violationsResponse = await fetch(url, {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
        'Authorization': req.headers.authorization
      },
      body: JSON.stringify({ api_definition: schema })
    });

    const violations = await violationsResponse.json();

    res.json(violations);

  } catch (error) {

    logger.error(error);

    const status = error.status ? error.status : 400;

    if(404 == error.status){
      error.type = 'about:blank',
      error.title = 'Swagger URL not found',
      error.detail = 'The URL provided for swagger file could not be found'
    }

    if( 500 == error.status){
      error.type = error.type || 'about:blank',
      error.title = error.title || 'Internal Server Error',
      error.detail = error.detail || 'Ooops something went wrong!'
    }

    res.status(status);

    // use https://zalando.github.io/problem/schema.yaml#/Problem'
    res.json({
      type: error.type,
      title: error.title,
      detail: error.detail,
      instance: error.instance,
      status
    });
  }
};
