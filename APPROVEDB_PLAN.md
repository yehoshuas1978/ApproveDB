# ApproveDB Plan

**Status**: Inactive product concept; no repository or implementation exists in
the current workspace
**Last reviewed**: 2026-07-02
**Recommendation**: Move to an archive or delete unless customer discovery
reactivates this product

> This is a target-state design, not a description of shipped OwlMask or
> OwlTable functionality. None of the APIs, deployment modes, approval flows,
> or database operations below should be referenced as currently available.

## Goal

ApproveDB is a small open-source, self-hosted database change approval gateway.

It receives proposed database changes over HTTP, stores them as pending approval requests, shows the exact operation and validation evidence, and lets an authorized operator approve or reject execution.

ApproveDB exists to give teams a controlled approval surface for structural database change proposals without turning the source system into a destructive SQL execution product and without pushing users into broad database workbenches.

ApproveDB is intentionally for database structure, not row data. Its scope is schema, database, table, column, and later index changes. It does not approve per-row `INSERT`, `UPDATE`, or `DELETE` operations.

## Product Positioning

- ApproveDB is an optional open-source receiver for database change proposals.
- Source systems may ignore webhooks, build their own receiver, use an internal automation platform, use ApproveDB, or let operators run reviewed SQL manually.
- Source systems emit events and proposed structural changes. ApproveDB stores, validates, and presents those proposals for approval.
- ApproveDB is customer-operated infrastructure. The operator supplies deployment, configuration, database credentials, approvers, and execution policy.
- ApproveDB should be described as a generic structural database change approval gateway, not as a source-system-specific execution engine.
- ApproveDB should be described as a structural database change approval gateway, not a row-level data editing or data correction tool.

Recommended wording:

> ApproveDB is a self-hosted approval gateway for structural database change proposals. Source systems send signed events with structured operations, ApproveDB shows the exact generated SQL and validation evidence, and an authorized operator approves or rejects execution.

## Integration Boundary

ApproveDB should stay source-system-agnostic.

- ApproveDB should not contain OwlTable-specific execution logic.
- ApproveDB should not depend on OwlTable schemas, entities, connection ids, provisioning concepts, or lifecycle semantics.
- ApproveDB should treat `sourceSystem`, `sourceEventId`, and `sourceTargetRef` as opaque external identifiers.
- Source systems are responsible for adapting their own events and target connection ids to ApproveDB's generic API contract.
- OwlTable may include first-class client-side support for ApproveDB, but ApproveDB itself remains a generic webhook and approval application.

## Non-Goals

- ApproveDB is not a database IDE.
- ApproveDB is not a query editor.
- ApproveDB is not a data browser.
- ApproveDB is not a row editor and does not approve per-row `INSERT`, `UPDATE`, or `DELETE` operations.
- ApproveDB is not a masking, subsetting, export/import, schema-diff, ERD, or migration-management product.
- ApproveDB should not compete with provisioning, validation, masking, subsetting, synthetic data generation, or broader database workbench products.
- ApproveDB should not become a broad database DevOps platform.

## Initial Supported Databases

- PostgreSQL is the first full-support database.
- MySQL should be added early because it is popular and may attract contributors who care about a small database approval gateway on its own merits.
- MySQL support must not pretend that schema/database operations are identical to PostgreSQL schema operations.
- Additional databases should wait for real user demand.
- Database-specific checks should be wrapped with clear errors or fallback behavior where possible.

### Dialect Support Matrix

PostgreSQL MVP:

- `CREATE_SCHEMA`
- `RENAME_SCHEMA`
- `DROP_SCHEMA`
- `CREATE_TABLE`
- `RENAME_TABLE`
- `DROP_TABLE`
- `ADD_COLUMN`
- `DROP_COLUMN`
- `RENAME_COLUMN`
- `ALTER_COLUMN_TYPE`

MySQL MVP:

- `CREATE_SCHEMA` as `CREATE DATABASE`
- `DROP_SCHEMA` as `DROP DATABASE`, only with critical-risk confirmation
- `CREATE_TABLE`
- `RENAME_TABLE`
- `DROP_TABLE`
- `ADD_COLUMN`
- `DROP_COLUMN`
- `RENAME_COLUMN`
- `ALTER_COLUMN_TYPE`

MySQL should not support `RENAME_SCHEMA` in the MVP. Modern MySQL does not provide a simple safe equivalent to PostgreSQL `ALTER SCHEMA ... RENAME TO ...`; emulating it by creating a new database and moving objects is operationally risky and should not be hidden behind a basic approval action.

### Unsupported Dialect Behavior

ApproveDB must fail closed when an operation is not supported by the selected dialect.

If a webhook request asks for an unsupported operation, ApproveDB should reject the request before it becomes executable and return a structured `DIALECT_OPERATION_NOT_SUPPORTED` error.

The error should include:

- dialect
- operation type
- target connection reference
- human-readable reason
- supported operations for that dialect
- whether the request was stored as rejected or rejected before storage

Example:

```json
{
  "errorCode": "DIALECT_OPERATION_NOT_SUPPORTED",
  "dialect": "mysql",
  "operationType": "RENAME_SCHEMA",
  "targetConnectionRef": "qa-mysql",
  "reason": "MySQL schema rename is not supported because MySQL does not provide a simple safe equivalent to PostgreSQL ALTER SCHEMA RENAME.",
  "supportedOperations": [
    "CREATE_SCHEMA",
    "DROP_SCHEMA",
    "CREATE_TABLE",
    "RENAME_TABLE",
    "DROP_TABLE"
  ],
  "storedState": "REJECTED"
}
```

The UI should show unsupported-dialect requests as rejected or blocked, not pending for approval.

Unsupported operations must never fall back to arbitrary SQL, best-effort emulation, or partially generated SQL.

## Brand Asset

No portable brand asset is committed with this plan. If the product is
reactivated, create or license an asset in the new repository and record its
source/license there; do not depend on a developer's local screenshot path.

## Technology Stack

ApproveDB should optimize for a simple self-hosted Docker deployment, not for a large SaaS-style architecture.

Proposed stack (revalidate versions if implementation starts):

- Java 21
- Spring Boot
- Maven
- Docker
- SQLite for ApproveDB's internal metadata, approval queue, audit log, saved target references, and settings
- PostgreSQL JDBC driver for customer target database operations
- MySQL Connector/J for customer target database operations
- Flyway for internal SQLite migrations
- Spring Security for login, sessions, CSRF protection, and roles
- Thymeleaf with HTMX for the UI
- Minimal vanilla JavaScript only where HTMX is not enough

Use Spring Boot instead of plain Java. ApproveDB needs HTTP endpoints, forms, login, sessions, CSRF protection, configuration, validation, persistence, migrations, and production-friendly packaging. Plain Java would mostly recreate framework plumbing and slow the MVP down.

SQLite is the MVP internal store. The database file should live in a configurable mounted path such as `/data/approvedb.sqlite`.

ApproveDB should not use the customer target database as its internal metadata store.

PostgreSQL may be added later as an optional internal metadata store if larger teams ask for it.

H2 should be used only for tests if needed.

The UI should use Thymeleaf with HTMX for the MVP. Avoid React or Next.js until ApproveDB clearly needs a richer frontend.

## Change Request Model

ApproveDB must accept only structured operation requests.

Webhook requests must not include executable SQL. ApproveDB generates SQL internally from allowlisted operation types and validated parameters. This keeps the webhook contract from becoming a remote SQL execution surface.

Webhook requests must not include database credentials or raw connection information.

Webhook requests may reference a saved `targetConnectionRef` that an ApproveDB administrator configured separately.

Source-system integrations may use synced target connections. When a source-system target connection is added or updated, the source system can push the connection details to ApproveDB through an authenticated admin sync API. Operation webhook requests should then reference the immutable source-system target id as `sourceTargetRef`, not a display name.

### Core Fields

- request id
- source system name
- source event id
- idempotency key
- received timestamp
- operation type
- dialect
- target connection reference
- optional source target reference
- operation parameters
- human-readable note
- generated SQL preview snapshot
- risk level
- data loss risk
- approval state
- approval timestamp
- approver identity
- rejection timestamp
- rejection identity
- rejection reason
- execution state
- execution timestamp
- execution result summary
- validation evidence snapshot

### Initial Operation Types

- `CREATE_SCHEMA`
- `RENAME_SCHEMA`
- `DROP_SCHEMA`
- `CREATE_TABLE`
- `RENAME_TABLE`
- `DROP_TABLE`
- `ADD_COLUMN`
- `DROP_COLUMN`
- `RENAME_COLUMN`
- `ALTER_COLUMN_TYPE`

ApproveDB should not support arbitrary SQL bundles in webhook requests.

## Risk Levels

- `LOW`: normally reversible metadata or creation operations, such as creating a schema.
- `MEDIUM`: operations that create durable objects or may affect application assumptions, such as creating a table.
- `HIGH`: operations that rename active objects or may break application references, permissions, search paths, migrations, or active sessions.
- `CRITICAL`: operations that delete objects or data, such as dropping tables or schemas.

Suggested defaults:

- `CREATE_SCHEMA`: `LOW`
- `CREATE_TABLE`: `MEDIUM`
- `RENAME_SCHEMA`: `HIGH`
- `RENAME_TABLE`: `HIGH`
- `ADD_COLUMN`: `MEDIUM`
- `DROP_COLUMN`: `CRITICAL`
- `RENAME_COLUMN`: `HIGH`
- `ALTER_COLUMN_TYPE`: `HIGH` or `CRITICAL` when the change may truncate, reinterpret, or invalidate existing values
- `DROP_TABLE`: `CRITICAL`
- `DROP_SCHEMA`: `CRITICAL`

## Validation Evidence

ApproveDB should provide approval evidence, not database exploration.

Useful checks:

- target connection reachability
- schema exists
- schema name is available
- table exists
- table name is available
- column exists
- column name is available
- current column type, nullability, default value, and generated/identity status
- whether the target column participates in a primary key, foreign key, unique constraint, index, or view dependency
- list of tables in a selected schema
- list of columns in a selected table
- approximate table row counts
- exact row count only on explicit demand
- approximate schema row count summary
- table count in schema
- object owner, when available
- object privileges, when available
- basic dependency warnings for drop and rename operations
- compatibility warnings for column type changes
- whether the target schema or table is empty

PostgreSQL and MySQL row counts should use approximate catalog statistics by default. Exact `COUNT(*)` can be slow and should require an explicit refresh action.

ApproveDB should not show table rows, export data, or provide ad-hoc query execution in the approval UI.

## Approval UX

Each change request should show:

- requested operation
- target database connection label
- affected schema and table names
- affected column names, when applicable
- source system and source event id
- note from the source system
- exact ApproveDB-generated SQL preview
- risk level and data loss risk
- validation evidence
- last validation timestamp
- approver decision controls
- execution result
- audit history

Destructive operations should require typed confirmation. For example, approving `DROP_SCHEMA` should require the user to type the exact schema name, and approving `DROP_COLUMN` should require the user to type the exact table and column name.

Column type changes should require an explicit compatibility review. ApproveDB should show the current type, requested type, nullability/default changes, approximate row count, dependency warnings, and whether the dialect-specific generator classifies the conversion as potentially lossy.

ApproveDB should not inspect or display row values to prove type compatibility. It may show metadata and row-count evidence only.

Default behavior:

- receive requests
- validate requests
- store requests as pending
- require human approval before execution
- record the approval decision
- execute only after approval
- record execution results

ApproveDB should not auto-execute by default.

## Decision History and Search

ApproveDB should keep a durable history of every received change request and every approval decision.

History should include:

- received requests
- rejected requests
- approved requests
- executed requests
- failed executions
- blocked unsupported-dialect requests
- generated SQL preview snapshot at decision time
- validation evidence snapshot at decision time
- approver or rejector identity
- approval or rejection reason
- execution result summary

The UI should include a searchable history page.

Search and filters should include:

- free-text search across source event id, note, schema name, table name, column name, generated SQL preview, and execution result summary
- state such as pending, approved, rejected, executed, failed, or blocked
- dialect
- operation type
- risk level
- target connection
- source system
- approver or rejector
- date range

History search should show summaries by default and require opening a request detail page to view full validation evidence and generated SQL preview.

ApproveDB should never treat history as a general SQL scratchpad. The stored SQL is the SQL generated by ApproveDB from structured requests, not SQL supplied by webhook callers.

## Security Requirements

- Webhook signature verification.
- Idempotency key handling.
- Request replay protection where possible.
- Short request timeouts.
- Reject any webhook payload that contains executable SQL fields such as `sql`, `sqls`, `sqlText`, `statement`, or `statements`.
- Generate executable SQL only inside ApproveDB from allowlisted operation types and validated identifiers.
- Database credentials configured by the operator, not supplied in the webhook payload.
- Role separation between viewers, approvers, and administrators.
- Audit records for receive, validation, approval, rejection, execution, and failure events.
- CSRF protection for the UI.
- Secret redaction in logs.
- No database password display after save.
- Optional allowlist for source systems.
- Optional allowlist for operation types per source system.
- Connection management APIs must require administrator privileges.
- Webhook operation callers must not be allowed to create or modify target database connections.
- Source-system connection sync must use an admin/setup token and must be separate from operation webhook delivery.
- Operation webhooks should be rejected when `sourceTargetRef` does not match a synced target connection for the declared `sourceSystem`.

## Connection Sync Transport Safety

Connection sync is the only place where database credentials may move from a source system to ApproveDB. Operation webhook payloads must never include credentials or raw connection details.

ApproveDB should support automatic sync-mode selection from the configured ApproveDB endpoint scheme.

Before selecting a connection-sync mode, the source system should call a non-secret capabilities endpoint.

The capabilities endpoint may be called over HTTP because it must not return secrets, credentials, filesystem paths, tokens, or other sensitive configuration. It only advertises supported features.

Automatic behavior:

- If the ApproveDB endpoint is `https://...`, the source system sends connection details through the HTTPS admin sync API.
- If the ApproveDB endpoint is `http://...`, the source system must call the capabilities endpoint and confirm that filesystem sync is supported before writing encrypted connection-sync files to the configured shared-volume outbox.
- If the ApproveDB endpoint is `http://...` and the capabilities endpoint does not advertise filesystem sync support, the source system must not write connection-sync files and must require an HTTPS endpoint for secret sync.
- If the ApproveDB endpoint is `http://...` and no shared-volume outbox is configured, the source system must block secret sync with a clear configuration error.
- ApproveDB should mirror this behavior: when configured for HTTPS sync, it expects admin API sync; when configured for local file sync, it scans the configured shared-volume inbox.

This keeps the setup simple: HTTPS means API sync. HTTP means shared-volume sync for secrets only if the receiver explicitly advertises filesystem sync support.

ApproveDB should support these connection-sync modes:

### Local Shared Volume Sync

Local shared volume sync is the recommended mode for a single-host Docker Compose deployment because it avoids certificate setup and avoids sending database credentials over HTTP.

In this mode, the source system and ApproveDB mount the same host directory. The source system writes encrypted connection-sync files into the directory, and ApproveDB imports them into its internal database.

Local shared volume sync requirements:

- disabled by default unless the Docker Compose profile or setting explicitly enables it
- uses a dedicated host directory such as `./approvedb-sync`
- mounts the directory only into the source-system container and ApproveDB
- the source system has a configured outbox path such as `/approvedb-sync/outbox`
- ApproveDB has a configured inbox path that maps to the same host directory, such as `/approvedb-sync/inbox`
- ApproveDB scans the inbox on a short interval, such as every 30 seconds
- the source system writes files with a temporary extension and then atomically renames them when complete
- ApproveDB imports only complete files
- ApproveDB deletes or archives imported files immediately after successful import
- files must include `sourceSystem`, immutable `sourceTargetRef`, target display-name snapshot, dialect, connection details, timestamp, nonce, and signature
- credential fields should be encrypted for ApproveDB before being written to disk
- files older than a short TTL should be rejected
- ApproveDB should record audit events for every imported, rejected, expired, or failed sync file
- the sync directory must not be world-readable
- the recommended Docker Compose example should use a private host path and restrictive permissions

This mode is simpler than HTTPS for local Docker, but it does not remove the need for secret handling. It moves the risk from network transport to host filesystem permissions, backups, and other containers with access to the same volume.

### HTTPS Sync

HTTPS sync is the recommended mode for any deployment where traffic may leave a single trusted Docker network or where the source system and ApproveDB are not sharing a controlled host volume.

- the source system sends connection details to ApproveDB over HTTPS.
- ApproveDB uses an admin/setup token for authorization.
- source systems should support self-signed certificates with explicit certificate fingerprint confirmation.
- source systems should show the ApproveDB URL, certificate fingerprint, and target connection summary before enabling sync.

### Local Docker Network HTTP Sync

Direct HTTP API sync of database credentials is not the recommended behavior.

An `http://...` ApproveDB endpoint should normally mean operation webhooks and non-secret control traffic use HTTP, while connection-secret sync uses local shared-volume files.

Direct HTTP API sync may be allowed only when the operator explicitly enables insecure local sync.

ApproveDB should not try to auto-detect that the source-system container and ApproveDB are on the same physical host. Containers cannot reliably prove that from inside the application.

Local HTTP sync requirements:

- disabled by default
- enabled only with an explicit setting such as `APPROVEDB_ALLOW_INSECURE_LOCAL_SYNC=true`
- requires an admin/setup token
- should be limited to a private Docker Compose network or equivalent private container network
- ApproveDB should not publish the admin sync port publicly in the recommended Docker Compose example
- the source system should show a warning that HTTP sync can expose database credentials if the network is not truly private
- ApproveDB should record audit events for every connection sync request

This mode is acceptable for a deliberate single-host Docker deployment, but the software should not claim it has cryptographically proven same-host execution.

Final recommendation: use automatic scheme-based selection plus capability discovery. `https://` uses HTTPS admin sync. `http://` uses local shared-volume sync only when the receiver advertises filesystem sync support. If an `http://...` endpoint does not support filesystem sync, require HTTPS for secret sync. Direct HTTP API secret sync should be an explicit advanced escape hatch, not a default.

## API Shape

### Capability Discovery API

ApproveDB should expose a non-secret capability discovery endpoint.

`GET /api/capabilities`

The endpoint may be called over HTTP because it does not transfer secrets. It should be safe to expose to source systems during setup.

Example response:

```json
{
  "application": "ApproveDB",
  "version": "0.1.0",
  "capabilities": {
    "operationWebhooks": true,
    "httpsConnectionSync": true,
    "filesystemConnectionSync": true,
    "directHttpConnectionSync": false
  },
  "filesystemSync": {
    "supported": true,
    "requiresEncryptedFiles": true,
    "requiresSignedFiles": true,
    "pollIntervalSeconds": 30
  },
  "supportedDialects": [
    "postgresql",
    "mysql"
  ]
}
```

The capabilities response must not include:

- database credentials
- filesystem paths
- tokens
- signing secrets
- encryption keys
- configured target connection details

### Connection Management APIs

ApproveDB should provide connection management APIs for administrators.

These APIs are for setup, operations, and authenticated source-system connection sync. They are not operation webhook APIs.

- `GET /api/admin/target-connections`
- `POST /api/admin/target-connections`
- `GET /api/admin/target-connections/{id}`
- `PUT /api/admin/target-connections/{id}`
- `DELETE /api/admin/target-connections/{id}`
- `POST /api/admin/target-connections/{id}/test`
- `PUT /api/admin/source-systems/{sourceSystem}/target-connections/{sourceTargetRef}`
- `DELETE /api/admin/source-systems/{sourceSystem}/target-connections/{sourceTargetRef}`
- `POST /api/admin/source-systems/{sourceSystem}/target-connections/{sourceTargetRef}/test`

Connection APIs should:

- require administrator privileges
- store credentials encrypted or otherwise protected by the deployment secret
- never return saved passwords or secrets after creation
- support PostgreSQL and MySQL connection types
- expose a stable `targetConnectionRef` and non-secret connection summary
- record audit events for create, update, delete, and test actions
- store source-system-owned connection identity, such as `sourceSystem` and `sourceTargetRef`, when a connection is synced from an external source system
- treat source target display names as snapshots for readability, not as identifiers

The non-secret connection summary should include enough information for a human to verify the target:

- ApproveDB connection id
- ApproveDB display label
- source system, when synced from an external system
- source target reference, when synced from an external system
- source target display-name snapshot, when synced from an external system
- dialect
- host label or hostname
- port
- database name
- default schema, when applicable
- configured username
- optional environment label
- connection fingerprint derived from non-secret connection settings
- last successful test timestamp

For synced source-system integrations, operation webhook change requests should reference `sourceTargetRef`. ApproveDB should resolve that reference to the latest synced target connection for the declared `sourceSystem`.

Webhook operation requests must not include host, port, username, password, JDBC URL, SSH tunnel settings, or other raw connection details.

If a source-system target connection is deleted or disabled, the source system should call the sync delete endpoint or mark the synced connection disabled. ApproveDB should reject new operation requests for disabled synced connections.

### Receive Change Request

`POST /api/change-requests`

The endpoint receives a proposed operation from any compatible source system.

Example structured request:

```json
{
  "sourceSystem": "example-provisioner",
  "sourceEventId": "provision-42-completed",
  "idempotencyKey": "provision-42-promotion-app",
  "operationType": "RENAME_SCHEMA",
  "dialect": "postgresql",
  "sourceTargetRef": "target-17",
  "sourceTargetNameSnapshot": "qa-target",
  "parameters": {
    "fromSchema": "app__p0007",
    "toSchema": "app",
    "archiveSchema": "app__archived__p0008"
  },
  "note": "Promote the latest provisioned schema after operator review."
}
```

### List Pending Requests

`GET /api/change-requests?state=PENDING`

### Search Change Request History

`GET /api/change-requests?state=REJECTED&dialect=postgresql&operationType=DROP_SCHEMA&q=app`

The same collection endpoint should support filtered history search across pending, approved, rejected, executed, failed, and blocked requests.

### Get Request Detail

`GET /api/change-requests/{id}`

### Refresh Validation Evidence

`POST /api/change-requests/{id}/validate`

### Approve Request

`POST /api/change-requests/{id}/approve`

### Reject Request

`POST /api/change-requests/{id}/reject`

### Execute Approved Request

`POST /api/change-requests/{id}/execute`

Execution may be combined with approval only if the operator explicitly enables that workflow.

## UI Scope

ApproveDB UI should include:

- login
- target connection setup
- administrator connection management APIs
- source-system connection sync management
- pending approval queue
- searchable decision history
- change request detail
- validation evidence panel
- SQL preview panel
- approve/reject controls
- execution status
- audit log
- settings for allowed sources and operation types

ApproveDB UI should not include:

- data grid
- query editor
- table data browsing
- export/import workflows
- masking workflows
- schema comparison
- ERD
- migration project management

## Relationship With OwlTable

OwlTable should present ApproveDB as one optional compatible receiver.

OwlTable should also present these alternatives:

- no webhook, use OwlTable's suggested SQL display and run commands manually
- customer-built webhook receiver
- internal automation or approval system
- `psql` for the lowest-overlap manual execution path
- `mysql` CLI for the lowest-overlap MySQL manual execution path
- pgAdmin for teams that require a familiar PostgreSQL UI

OwlTable should not present broad database DevOps platforms or general database workbenches as the primary path, because they can pull users into overlapping database management workflows.

## License and Ownership

- ApproveDB should use a standard open-source license such as Apache-2.0 or MIT.
- The repository should clearly state that the software is provided as-is under the chosen license.
- The documentation should be honest about maintainership.
- The documentation should not claim the project is unrelated to a commercial product if that product's founder or company creates or maintains it.
- The documentation should state that operators decide whether and how to deploy, configure, approve, and execute database changes.

## MVP Build

ApproveDB should be built in two gated phases.

If Phase 1 takes two days or more, stop after Phase 1 and do not start Phase 2 until the standalone tool has been reviewed, run locally, and the remaining scope is re-estimated.

### Phase 1: Core Approval Gateway

- Spring Boot backend.
- Thymeleaf with HTMX UI.
- PostgreSQL target connections.
- Admin connection management APIs.
- Source-system connection sync APIs for externally created or externally updated target connections.
- Capability discovery endpoint.
- Automatic scheme-based connection sync selection.
- Local shared-volume sync for `http://...` endpoints when filesystem sync is supported.
- HTTPS admin sync for `https://...` endpoints.
- PostgreSQL operations: `CREATE_SCHEMA`, `RENAME_SCHEMA`, `DROP_SCHEMA`, `CREATE_TABLE`, `RENAME_TABLE`, and `DROP_TABLE`.
- Pending request queue.
- Webhook receive endpoint.
- HMAC signature verification.
- Idempotency key storage.
- Basic validation evidence.
- SQL preview.
- Manual approval.
- Searchable decision history for approved, rejected, blocked, executed, and failed requests.
- Typed confirmation for destructive operations.
- Execute approved request.
- Audit log.
- Docker image.
- Apache-2.0 or MIT license.

### Phase 1 Stop Rule

- If Phase 1 takes two days or more, stop.
- Do not add MySQL, column operations, index operations, or advanced role controls during that initial push.
- Use the stopped Phase 1 build to validate the local Docker workflow, shared-volume sync, approval UX, and generated SQL safety.
- Resume Phase 2 only after the remaining work is deliberately re-scoped.

### Phase 2: Broader Structural Coverage

- MySQL target connections.
- PostgreSQL column operations: `ADD_COLUMN`, `DROP_COLUMN`, `RENAME_COLUMN`, and `ALTER_COLUMN_TYPE`.
- MySQL column operations: `ADD_COLUMN`, `DROP_COLUMN`, `RENAME_COLUMN`, and `ALTER_COLUMN_TYPE`.
- Index operations: `CREATE_INDEX`, `DROP_INDEX`, and `RENAME_INDEX` where the selected dialect supports safe generation.
- Index validation evidence, including indexed columns, uniqueness, index type, index size when available, and dependency warnings.
- Approximate row counts.
- Exact count on demand.
- Role separation.
- Source-system allowlists.
- Operation-type allowlists.
- Request expiration.
- Export audit log.

## Final Product Contract

- ApproveDB receives proposed database changes.
- ApproveDB validates and displays approval evidence.
- ApproveDB stores an audit trail.
- ApproveDB keeps searchable history for approved, rejected, blocked, executed, and failed requests.
- ApproveDB executes only according to operator-configured approval policy.
- ApproveDB focuses on structural database changes such as schema, database, table, column, and later index changes.
- ApproveDB does not approve per-row `INSERT`, `UPDATE`, or `DELETE` operations.
- ApproveDB does not browse table data.
- ApproveDB does not replace the source system's primary workflow.
- Source systems do not require ApproveDB unless their operators choose to use it.
