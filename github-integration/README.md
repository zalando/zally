# Zally Github Integration

# TODO write me

## Installation

1. Clone Zally repository
    ```bash
    git clone git@github.com:zalando-incubator/zally.git zally
    ```

2. Switch to `github-integration` folder:
	```bash
	cd zally/github-integration
	```

3. Build the server:
    ```bash
    ./gradlew clean build
    ```

4. Run Zally server using:
    ```bash
    ./gradlew bootRun
    ```
    The bootRun task is configured to run with 'dev' profile by default.