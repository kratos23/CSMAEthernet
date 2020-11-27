package com.pavelkrylov.csma_cd

import java.util.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.roundToInt

class Channel {
    private class Broadcast(private val startX: Double,
                            private var frameSize: Int,
                            private val startTime: Double = Clock()) {

        //returns segment of time when signal will be visible at point x
        fun getSignalTimeAtPoint(x: Double): Pair<Double, Double> {
            val distance = abs(x - startX)
            val resultStart = startTime + distance / PROPAGATION_SPEED
            val resultEnd = resultStart + frameSize / TRANSMISSION_RATE
            return resultStart to resultEnd
        }

        fun stopAtTime(time: Double) {
            assert(time >= startTime);
            val elapsedTime = time - startTime;
            val sentBytes = ceil(elapsedTime * TRANSMISSION_RATE).roundToInt()
            frameSize = frameSize.coerceAtMost(sentBytes);
        }
    }

    private val broadcastMap = TreeMap<Int, Broadcast>()

    private fun getNextId(): Int {
        return if (broadcastMap.isEmpty()) {
            1
        } else {
            broadcastMap.lastKey() + 1;
        }
    }

    //returns unique broadcast identifier
    @Suppress("ControlFlowWithEmptyBody")
    fun startBroadcast(frameSize: Int, x: Double): Int {
        val broadcast = Broadcast(x, frameSize)
        val id = getNextId()
        broadcastMap[id] = broadcast
        while (checkForCollision() ?: Double.MAX_VALUE < Clock());
        return id
    }

    private var minCollisionTime = 0.0

    //returns min collisionTime
    private fun nodeCollisionCheck(node: Node): Double? {
        val openType = 0
        val closeType = 1
        val points = mutableListOf<Pair<Double, Int>>()
        broadcastMap.forEach { (_, broadcast) ->
            val (startTime, endTime) = broadcast.getSignalTimeAtPoint(node.x)
            points.add(startTime to openType)
            points.add(endTime to closeType)
        }
        points.sortWith { p1, p2 ->
            val (time1, type1) = p1
            val (time2, type2) = p2
            if (time1 != time2) {
                time1.compareTo(time2)
            } else {
                type1.compareTo(type2)
            }
        }
        var balance = 0
        for ((time, type) in points) {
            when (type) {
                openType -> {
                    balance++;
                    if (balance > 1 && time > minCollisionTime) {
                        return time
                    }
                }
                closeType -> {
                    balance--;
                }
            }
        }
        return null
    }

    private fun checkForCollision(): Double? {
        val minNode = nodes.minByOrNull { node ->
            nodeCollisionCheck(node) ?: -1.0
        }
        if (minNode != null) {
            val collisionTime = nodeCollisionCheck(minNode)
            if (collisionTime != null) {
                minNode.onCollisionDetected(collisionTime)
                minCollisionTime = minCollisionTime.coerceAtLeast(collisionTime)
            }
            return collisionTime
        }
        return null
    }

    fun checkChannelFree(x: Double, time: Double = Clock()): Boolean {
        return broadcastMap.filterValues { broadcast ->
            val (startTime, endTime) = broadcast.getSignalTimeAtPoint(x)
            time in startTime..endTime
        }.isEmpty()
    }

    fun stopBroadCast(broadcastId: Int, stopTime: Double) {
        broadcastMap[broadcastId]?.stopAtTime(stopTime)
    }

    private val nodes = mutableListOf<Node>()

    fun addNode(node: Node) {
        nodes.add(node)
    }

    companion object {
        private const val C = 299_722_458.0
        private const val PROPAGATION_SPEED = 2.0 / 3.0 * C // meters per second
        private val TRANSMISSION_RATE = 2.94 * 10.0.pow(6) // bytes per second
    }
}