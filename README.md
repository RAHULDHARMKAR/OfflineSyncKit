# OfflineSyncKit


![License](https://img.shields.io/badge/license-MIT-green.svg)
![Platform](https://img.shields.io/badge/platform-Android-brightgreen.svg)
![Version](https://img.shields.io/badge/version-v1.0.0-blue.svg)

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
    implementation("com.github.RAHULDHARMKAR:OfflineSyncKit:v1.0.0")
}
```

---

# Basic Setup

```kotlin
val syncKit = OfflineSyncKit.create(
    context = applicationContext,
    apiAdapter = object : SyncApiAdapter {

        override suspend fun sync(
            entityName: String,
            entityId: String,
            operation: SyncOperation,
            payload: String
        ): SyncApiResult {

            return SyncApiResult(
                success = true
            )
        }
    }
)
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

# Manual Sync

```kotlin
val result = syncKit.syncNow()

println(result.successCount)
println(result.failedCount)
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

# Architecture

OfflineSyncKit uses:

* Room Database
* Kotlin Coroutines
* Kotlin Flow
* WorkManager
* Offline Queue Architecture

---

# Roadmap

Upcoming features:

* Maven Central Publishing
* Kotlin Serialization Integration
* Advanced Conflict Merge Engine
* Encryption Support
* Multi-Tenant Sync Support
* Dashboard Module
* Analytics Module



# License

MIT License

Copyright (c) Rahul Dharmkar


