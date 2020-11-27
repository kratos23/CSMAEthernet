package com.pavelkrylov.csma_cd

import java.util.*
import kotlin.random.Random

val GLOBAL_RANDOM = Random(47)

fun main() {
    val channel = Channel()
    val n = 23
    var curX = 0
    val nodes = mutableListOf<Node>()
    repeat(n) { ind->
        curX += 5 + GLOBAL_RANDOM.nextInt(5)
        val newNode = Node(curX.toDouble(),ind, channel)
        nodes.add(newNode)
        channel.addNode(newNode)
    }
    while (true) {
        nodes.forEach { node->
            if (node.trySendFrame()) {
                val timeS = String.format(Locale.US, "%.15f", Clock())
                println("Node ${node.nodeId} started broadcasting at $timeS time.")
            }
        }
        Clock.tick()
        println()
        Thread.sleep(100)
    }
}