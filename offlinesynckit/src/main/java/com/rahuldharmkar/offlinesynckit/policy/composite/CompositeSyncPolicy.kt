package com.rahuldharmkar.offlinesynckit.policy.composite

import android.content.Context
import com.rahuldharmkar.offlinesynckit.policy.SyncPolicy

/**
 * Combines multiple [SyncPolicy] instances into one policy.
 *
 * Use [allOf] when every policy must allow sync.
 * Use [anyOf] when at least one policy must allow sync.
 */
class CompositeSyncPolicy private constructor(
    private val policies: List<SyncPolicy>,
    private val mode: Mode
) : SyncPolicy {

    override fun canSync(
        context: Context
    ): Boolean {
        return when (mode) {
            Mode.ALL -> policies.all { policy ->
                policy.canSync(context)
            }

            Mode.ANY -> policies.any { policy ->
                policy.canSync(context)
            }
        }
    }

    private enum class Mode {
        ALL,
        ANY
    }

    companion object {

        /**
         * Creates a policy that allows sync only when all policies allow sync.
         */
        fun allOf(
            vararg policies: SyncPolicy
        ): SyncPolicy {
            require(policies.isNotEmpty()) {
                "At least one SyncPolicy is required"
            }

            return CompositeSyncPolicy(
                policies = policies.toList(),
                mode = Mode.ALL
            )
        }

        /**
         * Creates a policy that allows sync when any policy allows sync.
         */
        fun anyOf(
            vararg policies: SyncPolicy
        ): SyncPolicy {
            require(policies.isNotEmpty()) {
                "At least one SyncPolicy is required"
            }

            return CompositeSyncPolicy(
                policies = policies.toList(),
                mode = Mode.ANY
            )
        }
    }
}