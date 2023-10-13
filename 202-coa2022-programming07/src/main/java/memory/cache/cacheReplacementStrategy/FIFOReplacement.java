package memory.cache.cacheReplacementStrategy;

import memory.Memory;
import memory.cache.Cache;

/**
 * TODO 先进先出算法
 */
public class FIFOReplacement implements ReplacementStrategy {

    @Override
    public void hit(int rowNO) {
        //第一次进入才更新时间戳
        if(Cache.getCache().getTimeStamp(rowNO) == 0L)
            Cache.getCache().setTimeStamp(rowNO);
    }

    @Override
    public int replace(int start, int end, char[] addrTag, byte[] input) {
        Cache cache = Cache.getCache();
        int rep = start;
        //寻找最小时间戳，即最先进入的块
        for(int i = start; i <= end; ++i) {
            if(cache.getTimeStamp(i) < cache.getTimeStamp(rep)) rep = i;
        }
        //写回
        if(Cache.isWriteBack && cache.isDirty(rep)) Memory.getMemory().write(cache.getAddr(rep), Cache.LINE_SIZE_B, cache.getData(rep));
        cache.update(rep, addrTag, input);
        return rep;
    }

}
