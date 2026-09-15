# FileStorage

A lightweight Java file storage utility built on top of `java.nio.file`.

`FileStorage` provides a simple API for managing files and directories while supporting both instance-based storage operations and static filesystem utilities.

[![Java](https://img.shields.io/badge/Java-17%2B-orange)]()

---

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Basic Usage](#basic-usage)
- [Security: Root Boundary Enforcement](#security-root-boundary-enforcement)
- [Examples](#examples)
  1. [Write and Read a JSON File](#1-write-and-read-a-json-file)
  2. [Automatic File Extensions](#2-automatic-file-extensions)
  3. [Detect Existing File Names](#3-detect-existing-file-names)
  4. [Check File Existence](#4-check-file-existence)
  5. [Get a File](#5-get-a-file)
  6. [Create Directories](#6-create-directories)
  7. [Find Files](#7-find-files)
  8. [Find Folders](#8-find-folders)
  9. [Delete a File](#9-delete-a-file)
  10. [Delete a Directory Recursively](#10-delete-a-directory-recursively)
  11. [Resolve a File Path](#11-resolve-a-file-path)
- [Object Serialization](#object-serialization)

---

## Features

- Root-based file storage
- Automatic file extension handling
- File and directory management
- Recursive file/directory discovery
- File deletion and recursive directory deletion
- Class existence checking
- Optional object serialization/deserialization
- `java.nio.file.Path` based API
- Static utilities for root-independent operations

## Requirements

- Java 17+

---

## Basic Usage

Create a storage instance with a root directory:

```java
import dev.AidenKR.FileStorage.FileStorage;
import dev.AidenKR.FileStorage.FileType;

import java.nio.file.Path;

FileStorage storage = new FileStorage(
        Path.of("./storage")
);
```

The `storage` instance now uses:

```text
./storage
```

as its root directory.

---

## Examples

### 1. Write and Read a JSON File

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

The following file is created automatically:

```text
storage/
└── config.json
```

---

### 2. Automatic File Extensions

`FileType` automatically determines the file extension.

```java
storage.write(
        "config",
        FileType.JSON,
        "{}"
);
```

Creates:

```text
config.json
```

You can also specify the extension yourself:

```java
storage.write(
        "config.json",
        FileType.JSON,
        "{}"
);
```

The extension will not be duplicated.

```text
config.json         ✅
config.json.json    ❌
```

---

### 3. Detect Existing File Names

Use `FileType.DETECT` when you want to use the supplied file name without adding an extension.

```java
storage.write(
        "server.conf",
        FileType.DETECT,
        "port=8080"
);
```

Creates:

```text
storage/
└── server.conf
```

The file name is used as-is.

---

### 4. Check File Existence

Using the storage root:

```java
if (storage.exists("config", FileType.JSON)) {
    System.out.println("config.json exists");
}
```

You can also check an arbitrary `Path` using the static API:

```java
Path path = Path.of("./storage/config.json");

if (FileStorage.exists(path)) {
    System.out.println("File exists");
}
```

---

### 5. Get a File

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

If the file does not exist, `getFile()` returns `null`.

---

### 6. Create Directories

Create directories using the static API:

```java
FileStorage.createDirectories(
        Path.of("./storage/config")
);
```

Or create nested directories:

```java
FileStorage.createDirectories(
        Path.of("./storage/config/database")
);
```

Missing parent directories are created automatically.

Result:

```text
storage/
└── config/
    └── database/
```

---

### 7. Find Files

`getFiles()` recursively searches the specified directory.

```java
var files = storage.getFiles();

for (var file : files) {
    System.out.println(file.getPath());
}
```

For a specific directory:

```java
var files = storage.getFiles("config");

for (var file : files) {
    System.out.println(file.getName());
}
```

For example:

```text
storage/
├── config.json
├── database/
│   └── database.json
└── logs/
    └── application.log
```

Calling `storage.getFiles()` returns all files recursively.

---

### 8. Find Folders

```java
var folders = storage.getFolders();

for (var folder : folders) {
    System.out.println(folder.getPath());
}
```

For example:

```text
storage/
├── config/
├── database/
│   └── backup/
└── logs/
```

`getFolders()` returns:

```text
config
database
database/backup
logs
```

The root directory itself is not included.

---

### 9. Delete a File

```java
storage.delete(
        "config",
        FileType.JSON
);
```

This deletes:

```text
storage/config.json
```

You can also delete an arbitrary path:

```java
FileStorage.delete(
        Path.of("./storage/config.json")
);
```

---

### 10. Delete a Directory Recursively

```java
FileStorage.deleteDirectory(
        Path.of("./storage/config")
);
```

If the directory contains:

```text
config/
├── app.json
├── database/
│   ├── mysql.json
│   └── postgres.json
└── cache/
    └── cache.json
```

the entire `config/` directory tree is removed, along with all files and subdirectories.

---

### 11. Resolve a File Path

You can resolve a path without creating the file.

```java
Path path = FileStorage.resolve(
        Path.of("./storage"),
        "config",
        FileType.JSON
);

System.out.println(path);
```

Result:

```text
storage/config.json
```

This is useful when you need the resulting `Path` before performing another filesystem operation.

> If `name` resolves outside of `root` (via `../`, an absolute path, etc.), this throws `IllegalArgumentException`. See [Security: Root Boundary Enforcement](#security-root-boundary-enforcement).

---

## Security: Root Boundary Enforcement

If `name` (or `directory`) contains `../` or is an absolute path, the resulting path can end up outside of `root`. To prevent that, the following APIs throw `IllegalArgumentException` whenever the resolved path would escape `root`:

- `write(name, type, data)`
- `read(name, type)`
- `exists(name, type)`
- `delete(name, type)`
- `getFile(name, type)`
- `getFiles(directory)` — the `String` overload
- `getFolders(directory)` — the `String` overload
- the static `resolve(root, name, type)`

```java
storage.write("../../etc/passwd", FileType.DETECT, "x");
// IllegalArgumentException: Resolved path escapes the storage root: ../../etc/passwd

storage.write("/etc/passwd", FileType.DETECT, "x");
// IllegalArgumentException: Resolved path escapes the storage root: /etc/passwd
```

The overloads that take a `Path` directly (`getFiles(Path)`, `getFolders(Path)`, the static `delete(Path)`, `createDirectories(Path)`, `deleteDirectory(Path)`, etc.) do **not** perform this check — once the caller has constructed a `Path` object, this is treated as a low-level API where the caller has full control. If you're passing through untrusted input (an uploaded filename, a string from an API request, etc.), always go through the `String`-based API.

---

## Object Serialization

`FileStorage` can optionally use a `FileSerializer` to save and load Java objects.

### Configure a Serializer

```java
FileSerializer serializer = ...;

FileStorage storage = new FileStorage(
        Path.of("./storage"),
        serializer
);
```

The serializer is responsible for converting Java objects to and from stored data.

### Save an Object

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

The serializer converts the object into a string and `FileStorage` writes it to disk.

> ⚠️ The original source document cuts off here — it's missing the actual save-result example, a `load()` (object retrieval) example, and any description of the `FileSerializer` interface itself. These weren't omitted by me; they simply weren't in the source. Paste the rest of the original doc if you have it and I'll fold it in.