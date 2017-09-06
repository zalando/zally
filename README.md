[![Build Status](https://travis-ci.org/zalando-incubator/zally.svg?branch=master)](https://travis-ci.org/zalando-incubator/zally)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/05a7515011504c06b1cb35ede27ac7d4)](https://www.codacy.com/app/zally/zally?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=zalando-incubator/zally&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/05a7515011504c06b1cb35ede27ac7d4)](https://www.codacy.com/app/zally/zally?utm_source=github.com&utm_medium=referral&utm_content=zalando-incubator/zally&utm_campaign=Badge_Coverage)

<img src="logo.png" width="200" height="200" />

### Zally: A minimalistic, simple-to-use API linter

Zally brings order to your sea of APIs. Use it to:
- enable/disable rules on the server side
- configure (some) of the existing rules
- implement your own rules in [Kotlin](https://kotlinlang.org/)

Its standard configuration will check your APIs against the rules defined in [Zalando's RESTful Guidelines](http://zalando.github.io/restful-api-guidelines/), but anyone can use it **out-of-the-box**.

Zally's easy-to-use [CLI](cli/README.md) uses the server in the background so that you can check your API *on the spot*. It also features an intuitive [Web UI](web-ui/README.md) that shows implemented rules and lints external files and (with its online editor) API definitions.

More about Zally:
- Swagger-friendly: accepts [Swagger](https://swagger.io) .yaml and JSON formats; includes a server that lints your Swagger files; and parses Swagger files using [swagger-parser](https://github.com/swagger-api/swagger-parser)
- Using `x-zally-ignore` extension in your API definition, you can disable rules for a specific API
- Applying rule changes is only necessary in the server component
- API-specific code written in Java 8 with [Spring Boot](https://github.com/spring-projects/spring-boot) for better integration
- Rule implementation is optimal/possible in Kotlin

### Technical Dependencies

- Kotlin and Java 8 (server) with Spring Boot 
- Golang 1.7+: for CLI
- Node.js 7.6+: for web UI

Find additional details [here](https://github.com/zalando-incubator/zally/pull/65#issuecomment-269474831). With Spring 5, we consider using Kotlin also directly on the API side.

### Installation and Usage

To give Zally a quick try, first run the server locally and then use the CLI tool.

The [Server Readme](server/README.md), [CLI Readme](cli/README.md) and [Web UI Readme](web-ui/README.md) include more detailed installation steps for each component.

### Quick start guide

You can build and run the whole Zally stack (web-ui, server and database) by
executing this script:

```bash
./build_and_run.sh
```

Web UI is accessible on `http://localhost:8080`; Zally server on `http://localhost:8000`

### Contributing

Zally welcomes contributions from the open source community. To get started, take a look at our [contributing guidelines](CONTRIBUTING). Then check our [Project Board](https://github.com/zalando-incubator/zally/projects/1) and [Issues Tracker](https://github.com/zalando-incubator/zally/issues) for ideas. 

#### Roadmap
For Zally [version 1.2](https://github.com/zalando-incubator/zally/milestone/3), we're focusing on:
- generating new rules
- a new quickstart script
- better integration testing approaches

If you have ideas for these items, please let us know.

### Contact

Feel free to contact one the [maintainers](MAINTAINERS).


### License

MIT license with an exception. See [license file](LICENSE).
