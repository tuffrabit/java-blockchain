# github.com/tuffrabit/java-blockchain

## Description
I wanted to have a go at modern Java + Spring Boot. This is a very simple block chain implementation. I find block chains to be good use cases for learning a language/stack. It forces one to learn how to build web/REST endpoints in that stack while also digging into relatively low level concepts with the language (arrays, bytes, hashing, object relationships, serialization, etc).

This block chain is intended to represent a single node that handles adding transactions, mining blocks, and achieving consensus with other nodes over REST endpoints.

DO NOT USE THIS FOR REAL CRYPTO. THIS IS JUST FOR ME AND FOR LEARNING.

## Toolchain
openjdk 24.0.1

## Build/Run
`./mvnw spring-boot:run`

## TODO
- Add consensus
- Add tests
- Add example request collection