import kotlin.random.Random

class Node(val x: Double /* distance to the first node in meters*/,
           val nodeId: Int,
           private val channel: Channel) {
    private var collisionCount = 0
    private val rnd = Random(GLOBAL_RANDOM.nextInt())
    private var broadcastID: Int? = null
    private var blockedUntilTime = -1.0

    fun onCollisionDetected(collisionTime: Double) {
        collisionCount++
        val id = broadcastID
        if (id != null) {
            channel.stopBroadCast(id, collisionTime)
            broadcastID = null
            blockedUntilTime = collisionTime + getBackoffTime()
        }
    }

    private fun getBackoffTime(): Double {
        val intervalsCount = rnd.nextInt(1 shl
                collisionCount.coerceAtMost(BACKOFF_COLLISIONS_LIMIT))
        return Clock.TICK * intervalsCount
    }

    fun trySendFrame(): Boolean {
        if (channel.checkChannelFree(x)) {
            if (broadcastID != null) {
                collisionCount = 0 // Frame was successfully transmitted
            } else if (collisionCount > ERROR_COLLISIONS_COUNT) {
                collisionCount = 0
            }
        }
        if (channel.checkChannelFree(x) && blockedUntilTime < Clock()) {
            broadcastID = channel.startBroadcast(FRAME_SIZE, x)
            return true
        }
        return false
    }

    companion object {
        private const val ERROR_COLLISIONS_COUNT = 16
        private const val BACKOFF_COLLISIONS_LIMIT = 10
        private const val FRAME_SIZE = 1_518 // in bytes
    }
}