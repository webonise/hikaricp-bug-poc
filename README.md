"Hikari Hates the Default Schema" Bug
========================================

Given a default schema in the JDBC URL, new connections using that JDBC URL should be connected to the default schema. However, we have a symptom in an application
that suggests this is not the case for HikariCP connections. This PoC is determining if that is, in fact, the issue.

Set-Up
=========

Have MySQL running locally on its default port (`3306`). As `root`,  execute the following commands:

```sql

CREATE DATABASE hikaribug;
CREATE USER 'hikaripoc'@'localhost' IDENTIFIED BY 'hikari good or bad';
GRANT ALL ON hikaribug.* TO 'hikaripoc'@'localhost';

```

Run
======

`./gradlew run`

If it passes successfully, you're good to go.
