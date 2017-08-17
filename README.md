[![Build Status](https://travis-ci.org/zalando-incubator/zally.svg?branch=master)](https://travis-ci.org/zalando-incubator/zally)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/05a7515011504c06b1cb35ede27ac7d4)](https://www.codacy.com/app/zally/zally?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=zalando-incubator/zally&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/05a7515011504c06b1cb35ede27ac7d4)](https://www.codacy.com/app/zally/zally?utm_source=github.com&utm_medium=referral&utm_content=zalando-incubator/zally&utm_campaign=Badge_Coverage)

<img src="logo.png" width="200" height="200" />

### Zally: A minimalistic, simple-to-use API linter

Zally aims to bring order to your sea of APIs. Although its standard configuration will check your APIs against the rules defined in [Zalando RESTful Guidelines](http://zalando.github.io/restful-api-guidelines/), you can use it **out-of-the-box** by configuring your own rules and enabling/disabling them on the server side.

Zally comes with an easy-to-use [CLI](cli/README.md) that uses the server in the background so that you can check your API *on the spot*. It also features an intuitive [Web UI](web-ui/README.md) with lots of features, [such as...?]

More about Zally:
- Doesn't require a deployed service, only an API definition. This means that ...[what?]
- Swagger-friendly: accepts [Swagger](https://swagger.io) .yaml and JSON formats; includes a server that lints your Swagger files; and parses Swagger files using [swagger-parser](https://github.com/swagger-api/swagger-parser)
- Using `x-zally-ignore` extension in your API definition, you can disable rules for a specific API
- Applying rule changes is only necessary in the server component

### Technical Dependencies

- Java 8 (server): API-specific code written in Java 8 with [Spring Boot](https://github.com/spring-projects/spring-boot) for better integration
- Node.js 7.6+: for web UI
- [React??]
- Rule implementation is optimal/possible in Kotlin [why?]
- Golang 1.7+: for CLI

Find Further details can be found [here](https://github.com/zalando-incubator/zally/pull/65#issuecomment-269474831).
With Spring 5, we consider using Kotlin also on API side directly.

### Installation and Usage

To give Zally a quick try, first run the server locally and then use the CLI tool.

The [Server Readme](server/README.md), [CLI Readme](cli/README.md) and [Web UI Readme](web-ui/README.md) includes more detailed installation steps for each component.

### Quick start guide

```bash
git clone git@github.com:zalando-incubator/zally.git zally
cd zally

# Disable authentication and start a local version of Zally server
cd server
./gradlew clean build
./gradlew bootRun > /dev/null &
cd ..

# Build CLI tool
go get github.com/zalando-incubator/zally/cli/zally
cd $GOPATH/src/github.com/zalando-incubator/zally/cli/zally
go build
./zally lint /path/to/swagger/definition.yaml
```

### Contributing

Zally welcomes contributions from the open source community. To get started, take a look at our [contributing guidelines](CONTRIBUTING). Then check our [Project Board](https://github.com/zalando-incubator/zally/projects/1) and [Issues Tracker](https://github.com/zalando-incubator/zally/issues) for ideas. 

#### Roadmap
For Zally [version 1.2](https://github.com/zalando-incubator/zally/milestone/3), we're focusing on:
- generating new rules [rules for what?]
- a new quickstart script
- better integration testing approaches.

If you have ideas for these items, please let us know.

### Contact

Feel free to contact one the [maintainers](MAINTAINERS).


### License

MIT license with an exception. See [license file](LICENSE).
