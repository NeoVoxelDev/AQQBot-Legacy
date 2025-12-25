package top.alazeprt.aqqbot.adapter

import net.fabricmc.loader.api.FabricLoader
import top.alazeprt.aqqbot.util.Cancelable

class FabricCancelable(val task: Runnable, @Volatile var nextRunTime: Long, val period: Long) : Cancelable {
    override fun cancel() {
        nextRunTime = -1L
    }

    fun run() {
        if (nextRunTime == -1L) return
        if (nextRunTime <= System.currentTimeMillis()) {
            task.run()
            if (period != -1L) {
                nextRunTime += period
            } else {
                nextRunTime = -1L
            }
        }
    }
}