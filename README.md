# Injection Defender Spring Boot Starter

Spring Boot application enables a simple injection defender, to prevent SQL injections and XSS injections.

## Quickstart

- Import dependencies

```xml
    <dependency>
        <groupId>com.yookue.springstarter</groupId>
        <artifactId>injection-defender-spring-boot-starter</artifactId>
        <version>LATEST</version>
    </dependency>
```

> By default, this starter will auto take effect, you can turn it off by `spring.injection-defender.enabled = false`

- Configure Spring Boot `application.yml` with prefix `spring.injection-defender` (**Optional**)

```yml
spring:
    injection-defender:
        defender-filter:
            filter-paths:
                - '/**'
            exclude-paths:
                - '/foo/**'
                - '/bar/**'
        sql-protection:
            throws-exception: true
        xss-protection:
            throws-exception: true
```

## Document

- Github: https://github.com/yookue/injection-defender-spring-boot-starter

## Requirement

- jdk 1.8+

## License

This project is under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

See the `NOTICE.txt` file for required notices and attributions.

## Donation

You like this package? Then [donate to Yookue](https://yookue.com/public/donate) to support the development.

## Website

- Yookue: https://yookue.com
