# zally-maven-plugin
A Maven plugin for validating OpenAPI and Swagger specs with Zally.

## How it works
The plugin validates all specs in a specified directory (and in its subdirectories) and logs violations \
(`error` level is used for logging violations of `MUST` rules, `warn` - for others). \
If there is at least one violation of a mandatory (`MUST`) rule, the plugin fails a build process.

## Usage
Configure your `pom.xml`:
```xml
<project>
    ...
    <build>
        ...
        <plugins>
            ...
            <plugin>
                <groupId>org.zalando</groupId>
                <artifactId>zally-maven-plugin</artifactId>
                <version>1.0.0</version>
                <dependencies>
                    <!-- Replace this ruleset with the one that you need -->
                    <dependency>
                        <groupId>org.zalando</groupId>
                        <artifactId>zally-ruleset-zally</artifactId>
                        <version>2.0.0</version>
                    </dependency>
                </dependencies>
                <executions>
                    <execution>
                        <goals>
                            <goal>validate</goal>
                        </goals>
                        <configuration>
                            <!-- Specify a path to a dir with specs -->
                            <inputDir>${project.basedir}/src/main/resources/specs</inputDir>
                            <!-- Remove this element, if you don't need to skip rules -->
                            <ignoredRules>
                                <ignoredRule>M009</ignoredRule>
                                <ignoredRule>M010</ignoredRule>
                            </ignoredRules>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

Run:
```
mvn validate
```