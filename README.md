# BSGOCore
- This is a personal project
- This repository is neither sponsored nor endorsed by Bigpoint
- The owner of this repository is not responsible for any damages caused by this software
- The owner of this repository is not responsible for other people's actions (such as forks)
- The server was created for educational purposes only
- **The server is a version of the original BSGO 2.0 Resurrection project with minor changes**

### Contributing
**Contributions are welcome!  
Please feel free to open issues for bugs and feature requests, or submit pull requests with improvements.**

## Repository Notes
- The commit history has been reset to provide a clean and maintainable starting point.
- Earlier iterations of this project contained non-essential artifacts, so the history was pruned to avoid confusion.


---
## Introduction
BSGOCore is an open-source game server emulator designed for hosting massively multiplayer online games (MMOPGs).
It is based on the discontinued game Battlestar Galactica Online (BSGO) and seeks to recreate the gameplay experience of the original game as well as provide additional features or change the existing behaviour.

---

The server is developed in Java (JDK 21) with Quarkus and built using Maven. SQLite is used as the default database, with schemas and migrations managed via Flyway.

This implementation is intentionally kept simple and pragmatic: it works and provides the needed functionality, but it is still in an early stage and not yet a polished solution. The database integration, for example, is rudimentary — data is buffered and written back periodically, resulting in behavior that is closer to eventual consistency rather than strict consistency. The current setup was designed to enable faster development and iteration rather than to represent a final or optimal architecture.

---

## [Youtube Video showcase](https://youtu.be/0n3tcONcx90)

## **moved**: [Features](docs/FEATURES.md)

---

## Usage (WIP)
### Install prerequisites
- Game files are not provided, you have to own a copy yourself.
- Loginserver not provided (for testing not required)
- Chatserver not provided (for testing not required)
- Java (tested with JDK 21)
- Maven 3.9.9 or higher
### setup for test run
- copy contents ``.env.example`` to ``.env``
- edit .env, set
  - CLIENT_PATH
  - GAMESERVER_IGNORE_HASHES=true
- *The files are not provided yet(will add this later)* required: ServerConfigurationUtils folder (the server will search for template files and Cards (something like template files but in an ubiquitous language of the game)
  - AugmentTemplates
  - ColliderTemplates
  - JsonCards
  - LootTemplates
  - MissionTemplateConfiguration
  - SectorTemplates
  - ShipConfigTemplates
  - ZoneTemplates(empty, not implemented yet)
- start the server using the following command in the project root directory:
```
mvn clean quarkus:dev
// in another terminal (powershell)
// runclient will access the path to your client using .env file
.\runclient.bat 127.0.0.1 en
```


## MISC

### - [Project History](docs/HISTORY.md)
### - [Infrastructure](docs/INFRA.md)
### - [Paints](docs/PAINTS.md)

## Credits
- [Christer Ericson’s Real-time CollisionDetection](https://realtimecollisiondetection.net/)
  - build most of the collision detection system using the book as source
- [UnityCsReference](https://github.com/Unity-Technologies/UnityCsReference)
  - to ensure compatibility with calculations in Unity