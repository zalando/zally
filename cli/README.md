# Zally Command Line Interface

This is Zally's cli tool: it reads a swagger file locally and lints it by
requesting violations check at a given Zally server.

## Build

1. Follow [Go installation instructions](https://golang.org/doc/install)

1. Make sure that `$GOPATH` variable is set (and `$GOROOT` if necessary)

1. Clone the repository:

    ```bash
    git clone git@github.com:zalando/zally.git
    ```

1. Run tests:

    ```bash
    cd zally
    GO111MODULE=on ./test.sh
    ```

1. Build the binary:

    ```bash
    go build
    ```

## Release it

1. Install `goreleaser` tool:

    ```bash
    go get -v github.com/goreleaser/goreleaser
    ```

    Alternatively you can download a latest release from [goreleaser Releases Page](https://github.com/goreleaser/goreleaser/releases)

1. Clean up folder `cli/zally/dist` if exists

1. Make sure that the repository state is clean:

    ```bash
    git status
    ```

1. Tag the release:

    ```bash
    git tag v1.1.0
    ```

1. Run `goreleaser`:

    ```bash
    cd cli/zally
    goreleaser release --skip-publish
    ```

1. Check builds inside `cli/zally/dist` directory.

1. Publish release tag to GitHub:

    ```bash
    git push origin v1.1.0
    ```
