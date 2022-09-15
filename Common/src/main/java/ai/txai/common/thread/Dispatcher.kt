package ai.txai.common.thread

import com.blankj.utilcode.util.ThreadUtils
import kotlinx.coroutines.asCoroutineDispatcher

/**
 * Time: 02/03/2022
 * Author Hay
 */
object Dispatcher {
    /**
     * IO操作使用改线程池，比如访问网络，本地读写数据，读写文件
     */
    val IO = ThreadUtils.getIoPool().asCoroutineDispatcher()

    /**
     * 重计算消耗CPU的操作使用
     */
    val CPU = ThreadUtils.getCpuPool().asCoroutineDispatcher()

    /**
     * 单个线程，需要保证同步的使用该线程， 注意不要运行耗时操作
     */
    val SINGLE = ThreadUtils.getSinglePool().asCoroutineDispatcher()

    /**
     * 无限制线程池，少量使用
     */
    val CACHE = ThreadUtils.getCachedPool().asCoroutineDispatcher()
}