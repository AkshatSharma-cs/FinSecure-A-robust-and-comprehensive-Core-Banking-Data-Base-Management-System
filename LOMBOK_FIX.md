# Lombok Configuration & Fix Guide

## Overview

FinSecure uses Lombok for boilerplate reduction. If you encounter Lombok-related errors, follow this guide.

## Maven Configuration (Already Included)

The `pom.xml` includes the correct Lombok annotation processor configuration:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <source>17</source>
        <target>17</target>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

## IDE Setup

### IntelliJ IDEA
1. File → Settings → Plugins → Search "Lombok" → Install
2. File → Settings → Build → Compiler → Annotation Processors
3. Check "Enable annotation processing"
4. Restart IntelliJ

### Eclipse / STS
1. Download lombok.jar from https://projectlombok.org/download
2. Run: `java -jar lombok.jar`
3. It will auto-detect your Eclipse installation and install
4. Restart Eclipse

### VS Code
1. Install "Language Support for Java" extension
2. Install "Lombok Annotations Support for VS Code" extension

## Common Lombok Errors

### "Cannot find symbol: method getXxx()"
**Cause:** Annotation processing not enabled.
**Fix:**
```
IntelliJ: Settings → Build → Compiler → Annotation Processors → Enable
```

### "cannot find symbol @Builder"
**Cause:** Missing Lombok dependency or annotation processing.
**Fix:** Run `mvn clean install` to re-download dependencies.

### Compilation fails with "package lombok does not exist"
**Fix:**
```bash
mvn dependency:resolve
mvn clean compile
```

### Builder with default values
All entities using `@Builder` with default field values use `@Builder.Default`.
This is intentional and correct:
```java
@Builder.Default
private Boolean active = true;
```

## Lombok Annotations Used

| Annotation            | Purpose                                      |
|-----------------------|----------------------------------------------|
| `@Getter`             | Generate getters for all fields              |
| `@Setter`             | Generate setters for all fields              |
| `@NoArgsConstructor`  | Generate no-args constructor                 |
| `@AllArgsConstructor` | Generate all-args constructor                |
| `@Builder`            | Generate builder pattern                     |
| `@Builder.Default`    | Set default value for builder field          |
| `@RequiredArgsConstructor` | Constructor for final/non-null fields   |
| `@Slf4j`              | Inject `log` logger via SLF4J                |

## Verify Lombok is Working

After setup, this should compile without error:
```java
@Getter @Setter @Builder
public class Test {
    private String name;
    @Builder.Default
    private boolean active = true;
}
// Usage: Test t = Test.builder().name("hello").build();
```

## Production Build (No IDE Needed)

Lombok works perfectly with plain Maven:
```bash
mvn clean package -DskipTests
java -jar target/finsecure-backend-1.0.0.jar
```
