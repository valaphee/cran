```
    ________
   / ____/ /___ _      __
  / /_  / / __ \ | /| / /
 / __/ / / /_/ / |/ |/ /
/_/   /_/\____/|__/|__/
```

![license](https://img.shields.io/badge/License-Apache_2.0-blue.svg)
![version](https://img.shields.io/badge/Version-0.0.1-darkred.svg)

Flow-based programming "language", the aim of this project is not to create an entry-friendly programming language, and more over<br>
to create a capable language, which is easy extensible and modular, and therefore allow this project to grow and gain as many<br>
integrations as possible.

The language itself should be intuitive, or simply not allow specific behavior which might be unlogical/hard to track, like for example<br>
mutable structures, without willingly activating it. (See list nodes, which are by default immutable and can be toggled to be mutable.)

This project consists of 5 parts:
* flow: Actual implementation (contains all common nodes)
* flow-ext-*: Extensions
* flow-gui: JavaFX-based visual editor
* flow-meta: Metadata for additional details of graphs (required for development)
* flow-spec: Specification for nodes (required for development)
* flow-svc: Flow gRPC services for development
* flow-svc-all: Flow processor with gRPC services (with all extensions)
