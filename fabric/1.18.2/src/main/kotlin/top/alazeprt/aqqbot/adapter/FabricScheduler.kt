package top.alazeprt.aqqbot.adapter

import top.alazeprt.aqqbot.util.Cancelable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

object FabricScheduler {

    private val taskList: MutableList<FabricCancelable> = CopyOnWriteArrayList()

    private val executor = Executors.newScheduledThreadPool(16)


    fun runTask(task: Runnable): Cancelable {
        val cancelable = FabricCancelable(task, System.currentTimeMillis(), -1)
        taskList.add(cancelable)
        return cancelable
    }

    fun runTaskAsync(task: Runnable): Cancelable {
        val future = executor.submit(task)
        return object : Cancelable {
            override fun cancel() {
                future.cancel(true)
            }
        }
    }

    fun runTaskLater(task: Runnable, delay: Long): Cancelable {
        val cancelable = FabricCancelable(task, System.currentTimeMillis() + delay * 50L, -1)
        taskList.add(cancelable)
        return cancelable
    }

    fun runTaskLaterAsync(task: Runnable, delay: Long): Cancelable {
        val future = executor.schedule(task, delay * 50L, java.util.concurrent.TimeUnit.MILLISECONDS)
        return object : Cancelable {
            override fun cancel() {
                future.cancel(true)
            }
        }
    }

    fun runTaskTimer(task: Runnable, delay: Long, period: Long): Cancelable {
        val cancelable = FabricCancelable(task, System.currentTimeMillis() + delay * 50L, period * 50L)
        taskList.add(cancelable)
        return cancelable
    }

    fun runTaskTimerAsync(task: Runnable, delay: Long, period: Long): Cancelable {
        val future = executor.scheduleAtFixedRate(task, delay * 50L, period * 50L, java.util.concurrent.TimeUnit.MILLISECONDS)
        return object : Cancelable {
            override fun cancel() {
                future.cancel(true)
            }
        }
    }

    fun cancelAllTasks() {
        executor.shutdown()
        taskList.forEach { it.cancel() }
        taskList.clear()
    }

    fun submitTaskToMainThread() {
        val toRemove = mutableListOf<FabricCancelable>()
        taskList.forEach {
            it.run()
            if (it.nextRunTime == -1L)  {
                toRemove.add(it)
            }
        }
        taskList.removeAll(toRemove)
    }
}