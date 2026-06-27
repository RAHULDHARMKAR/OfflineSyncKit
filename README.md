# OfflineSyncKit


![License](https://img.shields.io/badge/license-MIT-green.svg)
![Platform](https://img.shields.io/badge/platform-Android-brightgreen.svg)
![Version](https://img.shields.io/badge/version-v1.2.0--dev-blue.svg)

A lightweight, production-ready Offline First Synchronization SDK for Android.

OfflineSyncKit helps Android applications reliably store local changes and synchronize them with remote servers when connectivity becomes available.

Designed for:

* POS Systems
* Healthcare Apps
* CRM Applications
* Field Force Applications
* Inventory Management
* Delivery Applications
* Enterprise Offline Applications

---

# Why OfflineSyncKit?

OfflineSyncKit is designed for Android applications that must continue working without internet connectivity.

Instead of losing user actions while offline, OfflineSyncKit automatically stores operations locally and synchronizes them when connectivity is restored.

# Features

## Offline Queue

Store operations locally when internet is unavailable.

Supported operations:

* CREATE
* UPDATE
* DELETE

---

## Automatic Synchronization

Automatically synchronizes pending items when:

* Internet becomes available
* Manual sync is triggered
* Periodic sync executes

---

## Retry Mechanism

Built-in retry support:

* Configurable retry count
* Failed item tracking
* GIVE_UP status handling
* Retry individual items
* Retry all failed items

---

## WorkManager Integration

Supports:

* One-time synchronization
* Periodic synchronization
* Background synchronization

---

## Merge Policies

Supported merge policies:

* APPEND_ONLY
* REPLACE_SAME_ENTITY
* REPLACE_SAME_ENTITY_OPERATION

---

## Conflict Resolution

Built-in conflict handling:

* LOCAL_WINS
* SERVER_WINS
* MANUAL
* Custom conflict resolver

---

## Sync Events

Observe synchronization lifecycle:

* Enqueued
* Started
* Success
* Failed
* Conflict
* GiveUp

---

## Statistics

Real-time queue statistics:

* Pending Count
* Syncing Count
* Synced Count
* Failed Count
* Conflict Count
* Give Up Count

---

## Logging

Configurable logging support for debugging and monitoring.

---

# Installation

Add JitPack repository:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add dependency:

```kotlin
dependencies {
    implementation("com.github.RAHULDHARMKAR:OfflineSyncKit:v1.1.0")
}
```

---

# Quick Start

```kotlin
val syncKit = SyncClient.Builder(applicationContext)
    .apiAdapter(
        object : SyncApiAdapter {

            override suspend fun sync(
                request: SyncRequest
            ): SyncApiResult {

                return SyncApiResult(
                    success = true
                )
            }
        }
    )
    .build()
```
---
# Enqueue Item

```kotlin
syncKit.enqueue(
    entityName = "customer",
    entityId = "123",
    operation = SyncOperation.CREATE,
    payload = """
        {
            "name":"Rahul"
        }
    """.trimIndent()
)
```

---

# Enqueue Object

```kotlin
data class Customer(
    val id: String,
    val name: String
)

val customer = Customer(
    id = "123",
    name = "Rahul"
)

syncKit.enqueueObject(
    entityName = "customer",
    entityId = customer.id,
    operation = SyncOperation.CREATE,
    entity = customer,
    serializer = { item ->
        gson.toJson(item)
    }
)
```

---



# Serializer Registry

OfflineSyncKit supports registering serializers once and reusing them whenever you enqueue objects.

```kotlin
val registry = SyncSerializerRegistry()

registry.register(
    Customer::class,
    SyncSerializer<Customer> { customer ->
        """
        {
          "id": "${customer.id}",
          "name": "${customer.name}"
        }
        """.trimIndent()
    }
)
```

Pass the registry into `SyncConfig`:

```kotlin
val syncKit = SyncClient.Builder(applicationContext)
    .apiAdapter(apiAdapter)
    .config(
        SyncConfig(
            serializerRegistry = registry
        )
    )
    .build()
```

Now enqueue objects without passing a serializer every time:

```kotlin
syncKit.enqueueObjectAndSyncIfOnline(
    entityName = "customer",
    entityId = customer.id,
    operation = SyncOperation.CREATE,
    entity = customer,
    type = Customer::class
)
```

This keeps object sync cleaner and avoids repeating serialization logic across the app.# Manual Sync

```kotlin
val result = syncKit.syncNow()

println(result.successCount)
println(result.failedCount)
```
---

# Payload Encryption

OfflineSyncKit supports encrypting queued payloads before they are stored in the local database.

By default, payloads are stored as plain text using `NoOpSyncEncryptionProvider`.

For applications handling sensitive information (such as healthcare, finance, CRM, or POS systems), you can enable AES-GCM encryption.

Example:

```kotlin
val encryptionProvider = AesSyncEncryptionProvider(
    keyProvider = DefaultSyncKeyProvider(
        "12345678901234567890123456789012"
            .toByteArray(Charsets.UTF_8)
    )
)

val syncKit = SyncClient.Builder(applicationContext)
    .apiAdapter(apiAdapter)
    .config(
        SyncConfig(
            encryptionProvider = encryptionProvider
        )
    )
    .build()
```


---
# Auto Sync

```kotlin
syncKit.scheduleAutoSync()
```

---

# Periodic Sync

```kotlin
val config = SyncConfig(
    enablePeriodicSync = true,
    periodicSyncIntervalMinutes = 15
)
```

---

# Retry Policy

```kotlin
val config = SyncConfig(
    retryPolicy = SyncRetryPolicy(
        maxRetryCount = 5
    )
)
```

---

# Conflict Resolution

```kotlin
val config = SyncConfig(
    conflictStrategy = SyncConflictStrategy.MANUAL
)
```

Custom resolver:

```kotlin
val config = SyncConfig(
    conflictResolver = SyncConflictResolver {
        SyncConflictResolution.MarkManual
    }
)
```

---

# Observe Queue

```kotlin
syncKit.observeQueue()
```

---

# Observe Statistics

```kotlin
syncKit.observeStats()
```

---

# Pause Synchronization

```kotlin
syncKit.pauseSync()
```

Resume:

```kotlin
syncKit.resumeSync()
```

---

# Queue Maintenance

Delete item:

```kotlin
syncKit.deleteItem(id)
```

Clear all items:

```kotlin
syncKit.clearAllItems()
```

---

# Sync Direction

Supported directions:

```kotlin
SyncDirection.PUSH
SyncDirection.PULL
SyncDirection.BOTH
```

---

# Enterprise Configuration

Configure authentication and custom request headers.

```kotlin
val syncKit = SyncClient.Builder(applicationContext)
    .apiAdapter(apiAdapter)
    .config(
        SyncConfig(
            authTokenProvider = SyncAuthTokenProvider {
                "Bearer your-token"
            },
            headerProvider = SyncHeaderProvider {
                mapOf(
                    "X-App-Version" to "1.2.0",
                    "X-Device" to "Android"
                )
            }
        )
    )
    .build()
```
---

# Sync Request

Every synchronization request is represented by a `SyncRequest`.

It contains:

- Entity information
- Payload
- Authentication token
- Custom headers
- Retry information
- Metadata

This makes the SDK easy to extend without changing the public API.

# Architecture

OfflineSyncKit uses:

* Room Database
* Kotlin Coroutines
* Kotlin Flow
* WorkManager
* Offline Queue Architecture

---

# Roadmap

### v1.2.x
- Builder API
- Enterprise Networking
- SyncRequest
- Authentication Provider
- Header Provider

### v1.3.x
- Payload Encryption
- Secure Storage
- Redacted Logging

### v1.4.x
- Queue Inspector
- Statistics Dashboard
- Debug Utilities

### v2.0.0
- Push Sync
- Pull Sync
- Bidirectional Sync
- Delta Synchronization
- Conflict Merge Engine



# License

MIT License

Copyright (c) Rahul Dharmkar


