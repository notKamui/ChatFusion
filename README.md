# ChatFusion

This project encompasses the basics of Network Programming and protocol design and is made in the context of a
University project.

## What is ChatFusion?

ChatFusion is a protocol, a client, and a server at the same type. The protocol itself and its specification is defined
in the [ChatFusion Protocol Specification (RFC)](RFC_CFP.txt).

Basically, the protocol is a simple text-based chat protocol that also allows servers to fuse to make networks of
servers and share their userbase.

## Requirements and build

To build this project and run both the client and server, you will need to have Java 17 (or a later version) installed
on your machine.

In the next commands, I will assume you also have Gradle installed. If not, you can replace the occurrences of `gradle`
with `./gradlew` to run the commands from the project root.

To build the artifacts, run the following command from the root of the project:

```sh
gradle clean jar
```

This will create the runnable artifacts
`ChatFusion-client-{version}.jar` and `ChatFusion-server-{version}.jar`
in the `client/build/libs/` and `server/build/libs/` directories, respectively.

You can run them as you would with any jar archive:

```sh
java -jar ChatFusion-client-{version}.jar [options]
java -jar ChatFusion-server-{version}.jar [options]
```

## Authors

* [Jimmy "notKamui" Teillard](https://git.notkamui.com/)
* [Hadi Najjar](https://github.com/hadinajjar)