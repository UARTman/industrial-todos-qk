# industrial-todos-qk
[![Java CI with Gradle](https://github.com/UARTman/industrial-todos-qk/actions/workflows/java-ci.yml/badge.svg?event=push)](https://github.com/UARTman/industrial-todos-qk/actions/workflows/java-ci.yml)

## Running the application in dev mode

In order to run the application in development mode, you must generate and provide the JWT keys.

After generating the keys, create the `.env` file with the following content in the root directory of the project:

```env
smallrye.jwt.verify.key.location=/full/path/to/publicKey.pem
smallrye.jwt.sign.key.location=file:/full/path/to/privateKey.pem
smallrye.jwt.encrypt.key.location=file:/full/path/to/publicKey.pem
```

Afterward, you can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./gradlew build -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

[//]: # ()
[//]: # (## Creating a native executable)

[//]: # ()
[//]: # (You can create a native executable using:)

[//]: # ()
[//]: # (```shell script)

[//]: # (./gradlew build -Dquarkus.native.enabled=true)

[//]: # (```)

[//]: # ()
[//]: # (Or, if you don't have GraalVM installed, you can run the native executable build in a container using:)

[//]: # ()
[//]: # (```shell script)

[//]: # (./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true)

[//]: # (```)

[//]: # ()
[//]: # (You can then execute your native executable with: `./build/industrial-todos-qk-1.0-SNAPSHOT-runner`)

[//]: # ()
[//]: # (If you want to learn more about building native executables, please consult <https://quarkus.io/guides/gradle-tooling>.)

## Kubernetes deployment

The schema for kubernetes deployment can be acquired in the `build/kubernetes/` directory after a successful build.

A secret `todos-backend` must exist with values `postgres-password` (self-explanatory),
`publicKey.pem` and `privateKey.pem` (used for JWT signing and validation).

## Swagger ui in prod

The production version of the app includes Swagger UI at `/q/swagger-ui`.
