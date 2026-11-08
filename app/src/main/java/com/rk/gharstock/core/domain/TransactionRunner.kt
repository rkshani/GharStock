package com.rk.gharstock.core.domain

/** Runs a block of repository calls atomically. */
interface TransactionRunner {
    suspend operator fun <T> invoke(block: suspend () -> T): T
}
