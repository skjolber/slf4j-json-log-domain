[![Build Status](https://travis-ci.org/skjolber/log-domain.svg?branch=master)](https://travis-ci.org/skjolber/log-domain)

# log-domain
Library supporting JSON-logging with [Logback] and [logstash-logback-encoder].

Users will benefit from

 * JSON-logging with domain-specific subtrees
 * User-friendly helper-classes generated via [Maven] plugin
 * Simple YAML-based definition format  

Multiple domains can be combined in the same log statement.

Bugs, feature suggestions and help requests can be filed with the [issue-tracker].

## License
[Apache 2.0]

# Obtain
The project is based on [Maven] and is not yet available from central Maven repository.

# Usage
The generated sources allow for writing statements like

```java
logger.info(system("fedora").tags(LINUX), "Hello world");
```

or

```java
logger.info().system("fedora").tags(LINUX).message("Hello world");
```

with log output

```json
{
  "message": "Hello world",
  "system": "fedora",
  "tags": ["linux"]
}
```
using `static` imports 

```java
import static com.example.global.GlobalMarkerBuilder.*;
import static com.example.global.GlobalTag.*;
```

#### Multiple domains
Combine multiple domains in a single log statement via `and(..)`:

```java
logger.info(name("java").version(1.7).tags(JIT) // programming language
        .and(host("127.0.0.1").port(8080)) // network
        .and(system("Fedora").tags(LINUX)), // global
        "Hello world");
```

or

```java
logger.info().name("java").version(1.7).tags(JIT)  // programming language
        .and(host("127.0.0.1").port(8080)) // network
        .and(system("Fedora").tags(LINUX)) // global
        .message("Hello world");
```

outputs domain-specific subtrees:

```json
{
  "message": "Hello world",
  "language": {
    "name": "java",
    "version": 1.7,
    "tags": ["JIT"]
  },
  "network": {
    "port": 8080,
    "host": "127.0.0.1"
  },
  "system": "fedora",
  "tags": ["linux"]
}
```

# YAML definition format
The relevant fields and tags are defined in a YAML file, from which Java sources are generated. Example definition:

```yaml
version: '1.0'
name: Global
package: com.example.global
description: Global values
keys:
  - system:
      name: operating system name
      type: string
      description: The system name
      example: Ubuntu, Windows 10 or Fedora
  - memory:
      name: physical memory
      type: integer
      format: int32
      description: Physical memory in megabytes
      example: 1024, 2048, 16384
tags:
 - linux: Linux operating system
 - mac: Apple operating system
 - windows: Microsoft windows operating system
```

The definition format consists of the fields

  * `version` - file version
  * `name` - domain name (will prefix generated java sources)
  * `package` - package of generated sources
  * `qualifier` - name of domain subtree in logged JSON output (optional) 
  * `description` - textual description of domain
  * `keys` - list of key-value definitions (see below). 
  * `tags` - list of tag definitions (see below)

In the above JSON example output, the optional `qualifier` corresponds to `network` and `language` while `keys` include `system`, `port`, `host`, `version`.

### Keys
Each key is defined by:

 * `name` - name of field (Operting System etc)
 * `type` - datatype (string, number, integer etc)
 * `format` - datatype subformat (int32, int64 etc)
 * `description` - textual description of key
 * `example` - example of legal value

The list item itself is the key in the logged key-value. The type/format datatype definition is borrowed from [Swagger Code Generator]. 

### Tags
Each tag is defined by:

 - `name` - a valid Java Enum name
 - `description` - textual description of the tag

# Generating helper sources
YAML files are converted to helper classes using `log-domain-maven-plugin`.

```xml
<plugin>
    <groupId>com.github.skjolber.log-domain</groupId>
    <artifactId>log-domain-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <executions>
        <execution>
            <id>generate</id>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <outputDirectory>target/generated-sources/domain-log-codegen</outputDirectory>
        <domains>
            <domain>
                <path>${basedir}/src/main/resources/yaml/network.yaml</path>
            </domain>
        </domains>
    </configuration>
</plugin>
```

In a multi-domain setup, the recommended approach is to generate per-domain artifacts, so that each project only generates helper classes for its own application-specific YAML file and accesses the helper classes for the other domains via a Gradle/Maven dependency.

## Support-library
A few common classes are not part of the generated sources:

```xml
<dependency>
    <groupId>com.github.skjolber.log-domain</groupId>
    <artifactId>log-domain-support-logback</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

# Testing
Verify that testing is performed using the test library. 

Capture log statements using a [JUnit Rule]

```java
public LogbackJUnitRule rule = LogbackJUnitRule.newInstance();
```

and verify logging using

```java
assertThat(rule, message("Hello world"));

assertThat(rule, key("language", "name", is("java")));
assertThat(rule, key("network", "host", is("127.0.0.1")));
assertThat(rule, key("system", is("fedora")));

assertThat(rule, tags("language", LanguageTag.JIT));

// MDC
assertThat(rule, mdc("uname", "magnus"));
```

optionally also using `Class` and `Level` filtering. Import the library using

```xml
<dependency>
    <groupId>com.github.skjolber.log-domain</groupId>
    <artifactId>log-domain-test-logback</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

# History

 - [1.0.0]: Initial version

[Apache 2.0]:				http://www.apache.org/licenses/LICENSE-2.0.html
[issue-tracker]:			https://github.com/skjolber/log-domain/issues
[Maven]:					http://maven.apache.org/
[1.0.0]:					https://github.com/skjolber/log-domain/releases/tag/log-domain-1.0.0
[Logback]:					https://logback.qos.ch/
[logstash-logback-encoder]:	https://github.com/logstash/logstash-logback-encoder
[Swagger Code Generator]:	https://github.com/swagger-api/swagger-codegen
[JUnit Rule]:				https://github.com/junit-team/junit4/wiki/rules