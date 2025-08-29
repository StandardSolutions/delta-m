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
- **MigrationLoader**: Loads SQL migration files from classpath resources, supports template substitution
- **Database**: Interface for database-specific implementations (PostgreSQL, H2)
- **AdvisoryLock**: Database-specific locking mechanism for multi-instance safety
- **ChangeLog**: Tracks executed migrations to prevent re-execution

### Database-Specific Implementations

- **PostgreSQL**: Uses advisory locks (`pg_try_advisory_lock`) for concurrency control
- **H2**: Uses lock table strategy for concurrency control

### Migration File Structure

- Migration files follow pattern: `{number}__{description}.sql` (e.g., `001__create_changelog_table.sql`)
- Located in `src/main/resources/migrations/` (PostgreSQL) or `src/main/resources/h2-migrations/` (H2)
- Support template variables: `${CHANGELOG_TABLE}`, `${LOCK_TABLE}`, `${SCHEMA}`, `${name}`

### Options System

Configuration via `DamsOptions` class supporting:
- `db-schema`: Database schema (default: "public")
- `db-changelog-table-name`: Changelog table name (default: "dams_3db_changelog")
- `db-lock-table-name`: Lock table name (default: "dams_db_lock")
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