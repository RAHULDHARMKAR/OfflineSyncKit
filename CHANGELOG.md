# Changelog

All notable changes to this project will be documented in this file.

The format is inspired by Keep a Changelog and follows Semantic Versioning (SemVer).

---

# [1.4.0] 

## Added
- SyncPolicy framework
- AlwaysSyncPolicy
- WifiOnlySyncPolicy
- ChargingOnlySyncPolicy
- CompositeSyncPolicy
- Policy-based sync skipping

## Improved
- Sample app policy configuration
- SyncConfig policy support

# [1.3.0] 

## Added
- Payload encryption support
- AES-GCM encryption provider
- Key provider abstraction
- Default sync key provider
- HMAC-SHA256 payload signing
- Signature header support using `X-Sync-Signature`

## Improved
- Security architecture
- Sample app security configuration
- README security documentation

# [1.2.0] 
---
## 🚀 Added

### Queue Management

- Queue Query API
- SyncQueueFilter
- Batch Object Enqueue
- Batch Object Enqueue & Sync
- Queue Query Engine

### Enterprise Networking

- SyncRequest
- SyncClient.Builder
- SyncAuthTokenProvider
- SyncHeaderProvider

### Serialization

- SyncSerializer
- SyncSerializerRegistry
- Generic Object Synchronization

### Architecture

- SyncEngine
- RetryEngine
- ConflictEngine
- QueueQueryEngine

### Sample Application

- Batch enqueue demonstration
- Queue query examples
- Improved sample UI
- Statistics improvements

### Developer Experience

- GitHub Actions CI
- Improved README
- GitHub Release support
- Public API documentation (KDoc)

---

## ✨ Improved

- Cleaner Builder API
- Cleaner public API
- Improved internal architecture
- Better separation of responsibilities
- Better object serialization
- Better retry handling
- Better conflict resolution
- Better queue querying
- Better sample application

---

## 🛠 Fixed

- Various internal refactoring improvements
- Build stability improvements
- JitPack compatibility improvements
- Sample application improvements

---

## 📚 Documentation

- README improvements
- Installation guide updates
- Builder API documentation
- SyncRequest documentation
- SyncConfig documentation
- SyncApiAdapter documentation
- Public API KDoc improvements

---

# [2.0.0] - 2026-06-28

## Added
- Pull synchronization
- Bidirectional synchronization
- PullSyncEngine
- BidirectionalSyncEngine
- SyncPullAdapter
- SyncPullRequest
- SyncPullResult
- SyncPulledItem
- SyncPullDataHandler
- SyncTenantProvider
- Delta sync token support
- Delta timestamp support
- Sync state token storage
- Multi-tenant queue storage
- Tenant-aware queue filtering

## Improved
- Pull item persistence
- Encrypted pulled item storage
- Sample app bidirectional sync demo
- Tenant-aware sync architecture


---

# [1.0.0] - 2026-06-19

🎉 Initial public release

## Features

- Offline Queue
- Room Database
- WorkManager Integration
- Automatic Synchronization
- Manual Synchronization
- Retry Policy
- Merge Policy
- Conflict Resolution
- Queue Statistics
- Sync Events
- Object Synchronization
- JitPack Publishing