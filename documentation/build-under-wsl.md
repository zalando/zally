# Building Under Windows Subsystem for Linux

The following notes document my working environment for building and testing a full Zally installation from a Windows 10 workstation.

## Prepare Windows Environment

- Download and install Docker for Windows

  - https://hub.docker.com/editions/community/docker-ce-desktop-windows

- Open Microsoft Store

  - Search for linux

  - Install Debian (or Ubuntu, other linuxes may need different commands for package management)

  - Launch the Debian console

## Prepare Linux Environment

- In the Debian console:

  - enter a username & password to complete the installation

- Install some general prerequisites

  ```bash
  sudo apt-get update -y && sudo apt-get install -y git openjdk-8-jdk curl make g++
  ```

  (I was quite surprised to see that `make` and `g++` were necessary to build some dependency of the web-ui)

- Install NodeJS

  - https://github.com/nodesource/distributions/blob/master/README.md#installation-instructions

  ```bash
  curl -sL https://deb.nodesource.com/setup_10.x | sudo -E bash -
  sudo apt-get install -y nodejs
  ```

- Make Windows docker binaries available on linux path:

  ```bash
  sudo ln -s /mnt/c/Program\ Files/Docker/Docker/resources/bin/docker.exe /usr/local/bin/docker
  sudo ln -s /mnt/c/Program\ Files/Docker/Docker/resources/bin/docker-compose.exe /usr/local/bin/docker-compose
  sudo ln -s /mnt/c/Program\ Files/Docker/Docker/resources/bin/docker-machine.exe /usr/local/bin/docker-machine
  sudo ln -s /mnt/c/Program\ Files/Docker/Docker/resources/bin/docker-credential-wincred.exe /usr/local/bin/docker-credential-wincred
  ```

- Clone Zally into a folder easily addressable under Windows (C:\Zally)

  ```bash
  cd /mnt/c
  git clone https://github.com/schweizerischebundesbahnen/zally
  cd zally

## Build and Run

Run the following script to build and then run the services under docker:

```bash
./build-and-run.sh
```

It'll take a while but eventually you'll see something like the following to indicate that we're ready to connect:

```
Started ApplicationKt in 36.576 seconds (JVM running for 40.144)
```

## Testing

Use a browser to check that things are working correctly:

- <http://localhost:8000/supported-rules> - should have a JSON response including a list of rules
- <http://localhost:8080/> - should show the UI including Zally icon top left
- <http://localhost:8080/rules?is_active=true> - should show a list presenting a subset of the rules above

## Troubleshooting

Variations on the following error messages are a regular occurrence when building the web-ui under Windows. The command in question can typically be rerun without producing exactly the same occurrence, so incrementally the command gets further each run. (Producing this guide took 5x attempts at the web-ui build before a full run completed).

```
error An unexpected error occurred: "ENOENT: no such file or directory, copyfile '/home/username/.cache/yarn/v1/npm-react-ace-5.8.0-872d9ee8b664300ed5ab9edac6234bbe90836836/webpack.config.base.js' -> '/mnt/c/zally/web-ui/node_modules/react-ace/webpack.config.base.js'".
```
