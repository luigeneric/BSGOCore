# BSGOCore
- This is a personal project
- This repository is neither sponsored nor endorsed by Bigpoint
- The owner of this repository is not responsible for any damages caused by this software
- The owner of this repository is not responsible for other people's actions (such as forks)
- The server was created for educational purposes only
---
## Introduction
BSGOCore is an open-source game server emulator designed for hosting massively multiplayer online games (MMOPGs).
It is based on the discontinued game Battlestar Galactica Online (BSGO) and seeks to recreate the gameplay experience of the original game as well as provide additional features or change the existing behaviour.

---

The server is developed in Java (JDK 21) with Quarkus and built using Maven. SQLite is used as the default database, with schemas and migrations managed via Flyway.

This implementation is intentionally kept simple and pragmatic: it works and provides the needed functionality, but it is still in an early stage and not yet a polished solution. The database integration, for example, is rudimentary — data is buffered and written back periodically, resulting in behavior that is closer to eventual consistency rather than strict consistency. The current setup was designed to enable faster development and iteration rather than to represent a final or optimal architecture.

---
**moved**: [Features](docs/FEATURES.md)
---
---
## Usage (WIP)
### Requirements
- Game files are not provided, you have to own a copy yourself.
- Loginserver not provided
- Chatserver not provided
- JDK 21
- Maven 3.9.9
```
WIP
```


## MISC

### - [Infrastructure](docs/INFRA.md)
### - [Paints](docs/PAINTS.md)
