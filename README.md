# Delta-M
Database-agnostic, Embeddable, Lightweight, Templated, Adaptive — Migrations.

Embed-friendly: ship migrations inside your library; no global toolchain required.

DB-agnostic: choose SQL per dialect (Postgres, H2, …) at startup.

Idempotent by design: IF NOT EXISTS and UPSERT patterns for safe re-runs.

Multi-instance safe: lock + changelog; PAV flow (Probe → Apply → Verify).

Zero deps: core uses only java.sql (tests aside).

## Install
<dependency>
  <groupId>io.dams</groupId>
  <artifactId>delta-m-core</artifactId>
  <version>0.1.0</version>
</dependency>
<!-- Optional dialect helpers -->
<dependency>
  <groupId>io.dams</groupId>
  <artifactId>delta-m-pg</artifactId>
  <version>0.1.0</version>
</dependency>

## Idempotency rules

DDL: always use IF NOT EXISTS for tables, indexes, constraints.

DML: use UPSERTs (ON CONFLICT DO NOTHING/UPDATE, or vendor-equivalent).

Flow: Probe changelog → Apply step → Verify by appending to changelog.

## Concurrency & locks

Postgres: advisory locks (pg_try_advisory_lock) + changelog table.

H2 / others: lock table strategy + changelog table.
Configurable lock key and timeout.

## Supported databases

✅ PostgreSQL, ✅ H2

🔜 MySQL, SQL Server, Oracle

## License

Apache-2.0 (tbd).

## Что важно для надёжности

Идемпотентность шага. В DDL используем IF NOT EXISTS / «создать, если нет» / «удалить, если есть». Для данных — upsert по ключу.

Один Connection + advisory‑lock. Лок на всю миграцию — исключаем параллельные прогоны.

Запись об ошибке (не обязательно). Можем логировать appendFailure(...) для диагностики, но наличие записи «успеха» — единственный сигнал «шаг сделан».

Версионирование шага (опционально). Если код шага поменялся, а ключ тот же — подумайте, нужно ли детектить «дрейф» (поле checksum/version).

## Памятка по идемпотентности шагов

PostgreSQL: CREATE TABLE IF NOT EXISTS, CREATE INDEX IF NOT EXISTS, ALTER TABLE ... ADD COLUMN IF NOT EXISTS, CREATE TYPE IF NOT EXISTS, jsonb тип.

H2: поддержка IF NOT EXISTS шире для базовых объектов, но нет CONCURRENTLY, поведение некоторых ALTER отличается — делайте отдельные шаги для H2.

Данные: INSERT ... ON CONFLICT (pk) DO NOTHING/UPDATE (PG), для H2 — MERGE INTO.

Побочные эффекты: избегайте операций, которые нельзя повторить без вреда (например, «перелить» данные без idempotent ключей).