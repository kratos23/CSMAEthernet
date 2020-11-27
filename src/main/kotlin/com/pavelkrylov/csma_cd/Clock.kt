package com.pavelkrylov.csma_cd

object Clock {
    const val TICK = 51.2 * 1e-6
    private var time = 0.0
    operator fun invoke() = time
    fun tick() {
        time += TICK
    }
}