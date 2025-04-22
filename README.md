# MySQL Schema Crawler & Model Generator

**A Spring Boot application that connects to a MySQL database, extracts schema metadata, dynamically generates Java model classes at runtime, and exposes REST APIs for metadata and model information.**

---
## Project Structure
```plaintext
mysql-schema-crawler/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/example/crawler/
        │       ├── CrawlerApplication.java
        │       ├── config/
        │       │   └── ConfigProperties.java
        │       ├── controller/
        │       │   ├── MetadataController.java
        │       │   └── ModelController.java
        │       ├── model/
        │       │   ├── ColumnInfo.java
        │       │   ├── TableInfo.java
        │       │   ├── ForeignKeyInfo.java
        │       │   └── IndexInfo.java
        │       └── service/
        │           ├── DatabaseCrawlerService.java
        │           └── ModelGeneratorService.java
        └── resources/
            ├── application.properties
            └── config.json
```
## Table of Contents

1. [Introduction](#introduction)
2. [Overall Architecture](#overall-architecture)
3. [Technical Design](#technical-design)
   - [Configuration](#configuration)
   - [DatabaseCrawlerService](#databasecrawlerservice)
   - [ModelGeneratorService](#modelgeneratorservice)
   - [Controllers & Endpoints](#controllers--endpoints)
4. [Crawler Workflow](#crawler-workflow)
5. [Deployment & Usage](#deployment--usage)
6. [Extensibility & Best Practices](#extensibility--best-practices)
7. [Sample Output](#sample-output)

---

## Introduction

This tool automates the introspection of a MySQL database schema and the generation of corresponding Java model classes at runtime. It is ideal for projects requiring dynamic object mapping, rapid prototyping, or code generation without manual model coding.

<image>  <!-- Architecture diagram -->

---
## Overall Architecture

The system is composed of four main layers:

1. **Configuration Loader**  
   Reads JSON or properties to obtain JDBC connection details and crawler options.
2. **Database Crawler**  
   Uses JDBC `DatabaseMetaData` to retrieve tables, columns, primary keys, foreign keys, and indexes.
3. **Model Generator**  
   Utilizes [ByteBuddy](https://bytebuddy.net/) for runtime class generation, mapping SQL types to Java types and defining fields/getters/setters.
4. **REST API**  
   Spring Web controllers expose endpoints to fetch raw metadata and to trigger model generation.

---
## Crawler Workflow – Flowchart

![Untitled Diagram drawio (1)](https://github.com/user-attachments/assets/8cd03204-b0f4-4aeb-8842-235dc3c7155c)

## Technical Design

### Configuration

- **application.properties**: Default JDBC settings and path to `config.json`.
- **config.json**: Overrides allowing multiple environments (dev, prod).

```json
{
  "jdbcUrl": "jdbc:mysql://localhost:3306/mydb",
  "username": "root",
  "password": "password",
  "schemaPattern": "%",
  "generateModels": true
}
```

---

### DatabaseCrawlerService

- Connects via Spring-managed `DataSource`.
- Invokes `DatabaseMetaData.getTables()`, `getColumns()`, `getPrimaryKeys()`, `getImportedKeys()`, and `getIndexInfo()`.
- Populates **TableInfo**, **ColumnInfo**, **ForeignKeyInfo**, and **IndexInfo** POJOs.

```java
List<TableInfo> crawlSchema(String schemaPattern) throws SQLException;
```

---

### ModelGeneratorService

- Iterates over each `TableInfo`.
- For each column in a table:
  - Maps SQL types (`VARCHAR`, `INT`, `DATE`, etc.) to Java types.
  - Defines private fields and public getters/setters via ByteBuddy.
- **Relationship support**:
  - Foreign keys generate object references.
  - Many-to-many join tables produce `List<Other>` on both sides.

```java
void generateModels(List<TableInfo> schemas);
```

---

### Controllers & Endpoints

| Path                         | HTTP Method | Description                                   |
|------------------------------|-------------|-----------------------------------------------|
| `/api/metadata?schema=%`     | GET         | Retrieve full schema metadata.                |
| `/api/models/generate`       | POST        | Crawl schema and generate model classes.      |
| `/api/models/list`           | GET         | List names of generated model classes.        |

---

## Crawler Workflow

1. **Load Configuration**  
   Read JSON/properties to configure JDBC URL, credentials, and patterns.
2. **Establish JDBC Connection**  
   Spring Boot auto-configures a `DataSource` bean.
3. **Invoke Metadata APIs**  
   Use `DatabaseMetaData` to iterate tables and extract:
   - Columns (name, type, size, nullable)
   - Primary keys per table
   - Foreign keys relations
   - Index definitions


4. **Generate Java Classes**  
   For each table:
   - Instantiate a ByteBuddy builder
   - Define bean properties (fields, getters/setters)
   - Inject relationships (FK references, collections for many-to-many)
   - Load classes into the application ClassLoader
5. **Expose via REST**  
   Return metadata JSON or generation status.

---

## Deployment & Usage

1. **Prerequisites**
   - Java 11+ and Maven installed.
   - MySQL 5.7+ instance reachable.
2. **Clone & Configure**
   ```bash
   git clone https://github.com/yuvraj_jaat/vistora_assg.git
   cd vistora_assg
   ```
   Update `config.json` or `application.properties` with your DB details.
3. **Build & Run**
   ```bash
   mvn clean package
   mvn spring-boot:run
   ```
4. **Test Endpoints**
   
   You can test the REST endpoints using **Postman** by importing the following URLs and executing them:
   
   1. **GET Schema Metadata**
      - Method: `GET`
      - URL: `http://localhost:8080/api/metadata?schema=%`
      - Description: Fetches full database schema metadata.
      ![image](https://github.com/user-attachments/assets/d3412d62-66d9-4030-8c87-cc933f82f2b6)

   2. **Trigger Model Generation**
      - Method: `POST`
      - URL: `http://localhost:8080/api/models/generate`
      - Description: Crawls the database and generates Java model classes at runtime.
        ![image](https://github.com/user-attachments/assets/ce646138-62c5-4999-b501-6a9b2fdf0006)

   3. **List Generated Model Classes**
      - Method: `GET`
      - URL: `http://localhost:8080/api/models/list`
      - Description: Lists the dynamically generated class names.
      ![image](https://github.com/user-attachments/assets/0bac8334-eae7-4090-905f-71de5c322444)

---

## Sample Output

```json
[
  {
    "name": "user",
    "columns": [...],
    "primaryKeys": ["id"],
    "foreignKeys": [],
    "indexes": [ ... ]
  },
  {
    "name": "order",
    "columns": [...],
    "primaryKeys": ["order_id"],
    "foreignKeys": [{"pkTable":"user","pkColumn":"id","fkTable":"order","fkColumn":"user_id"}],
    "indexes": [...]
  }
]
```

---


