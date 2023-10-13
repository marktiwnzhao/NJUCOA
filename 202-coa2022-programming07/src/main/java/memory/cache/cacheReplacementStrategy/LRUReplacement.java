package memory.cache.cacheReplacementStrategy;

import memory.Memory;
import memory.cache.Cache;

/**
 * TODO 最近最少用算法
 */
public class LRUReplacement implements ReplacementStrategy {

    @Override
    public void hit(int rowNO) {
        //每次命中更新一次时间戳
        Cache cache = Cache.getCache();
        cache.setTimeStamp(rowNO);
    }

    @Override
    public int replace(int start, int end, char[] addrTag, byte[] input) {
        Cache cache = Cache.getCache();
        int rep = start;
        for(int i = start; i <= end; ++i) {
            if(cache.getTimeStamp(i) < cache.getTimeStamp(rep)) rep = i;
        }
        if(Cache.isWriteBack && cache.isDirty(rep)) Memory.getMemory().write(cache.getAddr(rep), Cache.LINE_SIZE_B, cache.getData(rep));
        cache.update(rep, addrTag, input);
        return rep;
    }

}





























