package memory.cache.cacheReplacementStrategy;

import memory.Memory;
import memory.cache.Cache;

/**
 * TODO 最近不经常使用算法
 */
public class LFUReplacement implements ReplacementStrategy {

    @Override
    public void hit(int rowNO) {
        Cache cache = Cache.getCache();
        cache.addVisited(rowNO);
    }

    @Override
    public int replace(int start, int end, char[] addrTag, byte[] input) {
        Cache cache = Cache.getCache();
        int rep = start;
        for(int i = start; i <= end; ++i) {
            if(cache.getVisited(i) < cache.getVisited(rep)) rep = i;
        }
        if(Cache.isWriteBack && cache.isDirty(rep)) Memory.getMemory().write(cache.getAddr(rep), Cache.LINE_SIZE_B, cache.getData(rep));
        cache.update(rep, addrTag, input);
        return rep;
    }

}
