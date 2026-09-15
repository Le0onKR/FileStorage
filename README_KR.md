# FileStorage

`java.nio.file` 기반의 경량 Java 파일 스토리지 유틸리티입니다.

`FileStorage`는 인스턴스 기반의 스토리지 작업과, 루트 디렉토리에 종속되지 않는 정적(static) 파일시스템 유틸리티를 동시에 제공합니다.

[![Java](https://img.shields.io/badge/Java-17%2B-orange)]()

---

## 목차

- [특징](#특징)
- [요구사항](#요구사항)
- [기본 사용법](#기본-사용법)
- [보안: Root 경계 검증](#보안-root-경계-검증)
- [예제](#예제)
  1. [JSON 파일 쓰기/읽기](#1-json-파일-쓰기읽기)
  2. [자동 확장자 처리](#2-자동-확장자-처리)
  3. [기존 파일명 그대로 사용하기](#3-기존-파일명-그대로-사용하기)
  4. [파일 존재 여부 확인](#4-파일-존재-여부-확인)
  5. [파일 가져오기](#5-파일-가져오기)
  6. [디렉토리 생성](#6-디렉토리-생성)
  7. [파일 검색](#7-파일-검색)
  8. [폴더 검색](#8-폴더-검색)
  9. [파일 삭제](#9-파일-삭제)
  10. [디렉토리 재귀 삭제](#10-디렉토리-재귀-삭제)
  11. [파일 경로 resolve](#11-파일-경로-resolve)
- [객체 직렬화](#객체-직렬화)

---

## 특징

- 루트 기반 파일 스토리지
- 자동 파일 확장자 처리
- 파일 및 디렉토리 관리
- 재귀적 파일/디렉토리 탐색
- 파일 삭제 및 재귀적 디렉토리 삭제
- 클래스 존재 여부 확인
- 선택적 객체 직렬화/역직렬화 지원
- `java.nio.file.Path` 기반 API
- 루트에 독립적인 정적(static) 유틸리티 제공

## 요구사항

- Java 17+

---

## 기본 사용법

루트 디렉토리를 지정해 스토리지 인스턴스를 생성합니다.

```java
import dev.AidenKR.FileStorage.FileStorage;
import dev.AidenKR.FileStorage.FileType;

import java.nio.file.Path;

FileStorage storage = new FileStorage(
        Path.of("./storage")
);
```

이제 `storage` 인스턴스는 다음을 루트 디렉토리로 사용합니다.

```text
./storage
```

---

## 예제

### 1. JSON 파일 쓰기/읽기

```java
import dev.AidenKR.FileStorage.FileStorage;
import dev.AidenKR.FileStorage.FileType;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws Exception {

        FileStorage storage = new FileStorage(
                Path.of("./storage")
        );

        storage.write(
                "config",
                FileType.JSON,
                """
                {
                    "name": "FileStorage",
                    "enabled": true
                }
                """
        );

        String data = storage.read(
                "config",
                FileType.JSON
        );

        System.out.println(data);
    }
}
```

다음 파일이 자동으로 생성됩니다.

```text
storage/
└── config.json
```

---

### 2. 자동 확장자 처리

`FileType`이 파일 확장자를 자동으로 결정합니다.

```java
storage.write(
        "config",
        FileType.JSON,
        "{}"
);
```

생성 결과:

```text
config.json
```

확장자를 직접 지정할 수도 있습니다.

```java
storage.write(
        "config.json",
        FileType.JSON,
        "{}"
);
```

확장자는 중복 추가되지 않습니다.

```text
config.json        ✅
config.json.json   ❌
```

---

### 3. 기존 파일명 그대로 사용하기

확장자를 추가하지 않고 지정한 파일명을 그대로 사용하려면 `FileType.DETECT`를 사용합니다.

```java
storage.write(
        "server.conf",
        FileType.DETECT,
        "port=8080"
);
```

생성 결과:

```text
storage/
└── server.conf
```

파일명이 그대로 사용됩니다.

---

### 4. 파일 존재 여부 확인

스토리지 루트 기준으로 확인:

```java
if (storage.exists("config", FileType.JSON)) {
    System.out.println("config.json exists");
}
```

정적 API를 사용해 임의의 `Path`를 확인할 수도 있습니다.

```java
Path path = Path.of("./storage/config.json");

if (FileStorage.exists(path)) {
    System.out.println("File exists");
}
```

---

### 5. 파일 가져오기

```java
var file = storage.getFile(
        "config",
        FileType.JSON
);

if (file != null) {
    System.out.println("File: " + file.getName());
    System.out.println("Path: " + file.getPath());
}
```

파일이 존재하지 않으면 `getFile()`은 `null`을 반환합니다.

---

### 6. 디렉토리 생성

정적 API로 디렉토리를 생성합니다.

```java
FileStorage.createDirectories(
        Path.of("./storage/config")
);
```

중첩된 디렉토리도 한 번에 생성할 수 있습니다.

```java
FileStorage.createDirectories(
        Path.of("./storage/config/database")
);
```

누락된 상위 디렉토리는 자동으로 생성됩니다.

```text
storage/
└── config/
    └── database/
```

---

### 7. 파일 검색

`getFiles()`는 지정한 디렉토리를 재귀적으로 탐색합니다.

```java
var files = storage.getFiles();

for (var file : files) {
    System.out.println(file.getPath());
}
```

특정 디렉토리만 검색할 경우:

```java
var files = storage.getFiles("config");

for (var file : files) {
    System.out.println(file.getName());
}
```

예를 들어 아래와 같은 구조에서:

```text
storage/
├── config.json
├── database/
│   └── database.json
└── logs/
    └── application.log
```

`storage.getFiles()`를 호출하면 모든 파일을 재귀적으로 반환합니다.

---

### 8. 폴더 검색

```java
var folders = storage.getFolders();

for (var folder : folders) {
    System.out.println(folder.getPath());
}
```

예를 들어 아래와 같은 구조에서:

```text
storage/
├── config/
├── database/
│   └── backup/
└── logs/
```

`getFolders()`는 다음을 반환합니다.

```text
config
database
database/backup
logs
```

루트 디렉토리 자체는 결과에 포함되지 않습니다.

---

### 9. 파일 삭제

```java
storage.delete(
        "config",
        FileType.JSON
);
```

다음 파일을 삭제합니다.

```text
storage/config.json
```

임의의 경로도 삭제할 수 있습니다.

```java
FileStorage.delete(
        Path.of("./storage/config.json")
);
```

---

### 10. 디렉토리 재귀 삭제

```java
FileStorage.deleteDirectory(
        Path.of("./storage/config")
);
```

아래와 같은 디렉토리 구조가 있다면:

```text
config/
├── app.json
├── database/
│   ├── mysql.json
│   └── postgres.json
└── cache/
    └── cache.json
```

`config/` 디렉토리 전체(모든 파일 및 하위 디렉토리 포함)가 삭제됩니다.

---

### 11. 파일 경로 resolve

파일을 생성하지 않고 경로만 resolve할 수 있습니다.

```java
Path path = FileStorage.resolve(
        Path.of("./storage"),
        "config",
        FileType.JSON
);

System.out.println(path);
```

결과:

```text
storage/config.json
```

다른 파일시스템 작업 전에 결과 `Path`가 필요한 경우 유용합니다.

> `name`이 `root` 밖을 가리키는 경로(`../` 포함, 절대경로 등)라면 `IllegalArgumentException`이 발생합니다. 자세한 내용은 [보안: Root 경계 검증](#보안-root-경계-검증) 참고.

---

## 보안: Root 경계 검증

`name`(또는 `directory`)에 `../`가 포함되거나 절대경로가 들어오면, 계산된 최종 경로가 `root` 밖으로 벗어날 수 있습니다. 이를 막기 위해 아래 API들은 최종 경로가 `root`를 벗어나는 경우 `IllegalArgumentException`을 던집니다.

- `write(name, type, data)`
- `read(name, type)`
- `exists(name, type)`
- `delete(name, type)`
- `getFile(name, type)`
- `getFiles(directory)` — `String` 오버로드
- `getFolders(directory)` — `String` 오버로드
- 정적 `resolve(root, name, type)`

```java
storage.write("../../etc/passwd", FileType.DETECT, "x");
// IllegalArgumentException: Resolved path escapes the storage root: ../../etc/passwd

storage.write("/etc/passwd", FileType.DETECT, "x");
// IllegalArgumentException: Resolved path escapes the storage root: /etc/passwd
```

반면 `Path`를 직접 받는 오버로드(`getFiles(Path)`, `getFolders(Path)`, 정적 `delete(Path)`, `createDirectories(Path)`, `deleteDirectory(Path)` 등)는 이 검증을 하지 않습니다. 호출자가 이미 `Path` 객체를 구성한 시점에서는 전체 통제권을 가진 저수준 API로 취급하기 때문입니다. 사용자 입력(업로드 파일명, API로 받은 문자열 등)을 그대로 넘겨야 하는 경우에는 반드시 `String` 기반 API를 사용하세요.

---

## 객체 직렬화

`FileStorage`는 `FileSerializer`를 선택적으로 사용해 Java 객체를 저장하고 불러올 수 있습니다.

### 시리얼라이저 설정

```java
FileSerializer serializer = ...;

FileStorage storage = new FileStorage(
        Path.of("./storage"),
        serializer
);
```

시리얼라이저는 Java 객체를 저장 데이터로, 저장 데이터를 다시 Java 객체로 변환하는 역할을 담당합니다.

### 객체 저장

```java
Config config = new Config(
        "FileStorage",
        true
);

storage.save(
        "config",
        FileType.JSON,
        config
        );
```

시리얼라이저가 객체를 문자열로 변환하고, `FileStorage`가 이를 파일로 씁니다.

> ⚠️ 원본 문서가 여기서 잘려 있습니다(`save()` 이후 실제 파일 경로/결과 예시, `load()`(객체 불러오기) 예제, 그리고 있다면 `FileSerializer` 인터페이스 자체에 대한 설명이 빠져 있습니다). 해당 부분 원문을 주시면 이어서 정리해드리겠습니다.