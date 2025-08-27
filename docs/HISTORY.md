# Project History

The project started as a **standard Java application**, built with a
plain Java setup ("vanilla Java"). Over time, once the application went
live, several shortcomings became apparent. In particular, integrating
new libraries and dependencies proved to be cumbersome and error-prone.

To address these challenges, the project was **migrated to Quarkus**.
This migration required multiple iterations and consumed a significant
amount of time and effort. Along the way, the project also underwent a
**Java version upgrade**, moving from **JDK 17 to JDK 21**. With this
upgrade, the application's threading model was reworked to leverage
**Virtual Threads** instead of traditional platform threads.

As the project evolved, some frameworks were carried over during
migration. For example, **Logback** (the logging framework) was
initially retained when moving from vanilla Java to Quarkus. However, it
later became clear that Logback caused compatibility issues with newer
versions of Quarkus. Since Logback had not been updated since 2023 while
Quarkus continued to evolve, Logback was eventually removed and replaced
with Quarkus' native logging solution.

This change resolved the compatibility issues but introduced new
limitations in logging. Previously, Logback allowed for **per-user file
handlers**, providing fine-grained log separation. With Quarkus'
logging, logs are now consolidated into a **single logfile**, which is
less ideal for detailed log analysis.

Currently, the project's history has been **cleaned and streamlined** in
order to make the codebase presentable and maintainable, while still
reflecting the major architectural and technological shifts it has
undergone.
