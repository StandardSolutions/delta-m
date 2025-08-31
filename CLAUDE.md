# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Delta-M is a database-agnostic, embeddable, lightweight migration manager for Java applications. It supports multiple database dialects (PostgreSQL, H2) with plans for MySQL, SQL Server, and Oracle.

## Build and Test Commands

```bash
# Build the project
mvn compile

# Run all tests  
mvn test

# Run tests for specific database
mvn test -Dtest="*PostgreSQL*"  # PostgreSQL tests
mvn test -Dtest="*H2*"          # H2 tests

# Package the project
mvn package

# Clean build artifacts
mvn clean
```

## Architecture

### Core Components

- **DeltaM**: Main entry point that orchestrates migration execution using Probe → Apply → Verify flow
- **MigrationLoader**: Loads SQL migration files from classpath resources with automatic discovery, supports universal template substitution via `${variable}` syntax
- **SqlMigrationStep**: Executes SQL migration files with support for multi-statement execution
- **Database**: Interface for database-specific implementations (PostgreSQL, H2)
- **AdvisoryLock**: Database-specific locking mechanism for multi-instance safety
- **ChangeLog**: Tracks executed migrations to prevent re-execution

### Database-Specific Implementations

- **PostgreSQL**: Uses advisory locks (`pg_try_advisory_lock`) for concurrency control
- **H2**: Uses lock table strategy for concurrency control

### Migration File Structure

- Migration files follow pattern: `{number}__{description}.sql` (e.g., `001__create_changelog_table.sql`)
- Located in `src/main/resources/migrations/{database_type}/` where `{database_type}` is `postgresql`, `h2`, etc.
- Automatic discovery: Files are discovered automatically from JAR or filesystem using universal resource scanning
- Template variables: All `${variable}` patterns are resolved via `delta_mOptions.tableName()` method
- Multi-statement support: SQL files can contain multiple statements separated by semicolons

### Options System

Configuration via `delta_mOptions` class supporting:
- `db-schema`: Database schema (default: "public")
- `db-changelog-table-name`: Changelog table name (default: "delta_m_3db_changelog")
- `db-lock-table-name`: Lock table name (default: "delta_m_db_lock")
- `migration-path`: Path to migration files (default: "migrations")
- `lock-timeout-millis`: Lock timeout (default: 60000ms)

## Idempotency Requirements

All migrations must be idempotent:
- DDL: Use `IF NOT EXISTS` for tables, indexes, constraints
- DML: Use `UPSERT` patterns (`ON CONFLICT DO NOTHING/UPDATE` for PostgreSQL, `MERGE INTO` for H2)
- Avoid operations that cannot be safely repeated

## Dependencies

- Core: Only `java.sql` and `slf4j-api`
- Test: JUnit 5, Testcontainers, HikariCP, PostgreSQL driver, H2 database
- Java 17+ required

### Key Design Principles

1. **Elegant Objects**: Follow Elegant Objects principles for object-oriented design
2. **Idempotency**: All migration steps use `IF NOT EXISTS` patterns and upsert operations
3. **Advisory Locking**: Single connection with advisory lock prevents parallel execution
4. **Transactional Safety**: Each migration step is atomic
5. **Database-Agnostic**: Interface-based design supports multiple database types
6. **Universal Template System**: Single `${variable}` pattern handles all template substitutions
7. **Automatic Discovery**: Migration files are discovered automatically without hardcoded lists

## Recent Improvements

### MigrationLoader Enhancements
- **Universal Resource Discovery**: Automatic scanning of migration files from both JAR and filesystem
- **Simplified Template Processing**: Single generic pattern `${variable}` replaces multiple hardcoded replacements
- **Modular Design**: `SqlMigrationStep` extracted to separate class for better maintainability
- **Elimination of Hardcoded Suffixes**: No more predefined file name patterns, supports any migration file names following the `{number}__{description}.sql` pattern