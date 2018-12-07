# Zally Server

This is Zally's heart - Zally Server. It implements all rule checks and offers an
API to request an API linting. It also provides permalinks and statistics functionalities.

## Build and Run

1. Clone Zally repository

```bash
git clone git@github.com:zalando/zally.git zally
```

1. Switch to `server` folder:

```bash
cd zally/server
```

1. Build the server:

```bash
./gradlew clean build
```

1. Run Zally server using:

```bash
./gradlew bootRun
```

The bootRun task is configured to run with 'dev' profile by default.