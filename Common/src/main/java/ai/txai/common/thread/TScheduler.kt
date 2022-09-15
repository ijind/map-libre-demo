package ai.txai.common.thread

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

/**
 * Time: 11/03/2022
 * Author Hay
 */
object TScheduler {

    fun io(): Scheduler {
        return Schedulers.from(Dispatcher.IO.executor)
    }

    fun cpu(): Scheduler {
        return Schedulers.from(Dispatcher.CPU.executor)
    }

    fun cache(): Scheduler {
        return Schedulers.from(Dispatcher.CACHE.executor)
    }

    fun single(): Scheduler {
        return Schedulers.from(Dispatcher.SINGLE.executor)
    }
}