package memory.disk;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SchedulerTest {

    Scheduler scheduler = new Scheduler();

    int start1 = 100;
    int[] request1 = {55, 58, 39, 18, 90, 160, 150, 38, 184};

    @Test
    public void FCFSTest1() {
        double result = scheduler.FCFS(start1, request1);
        assertEquals(114, result, 0.01);
    }

    @Test
    public void SSTFTest1() {
        double result = scheduler.SSTF(start1, request1);
        assertEquals(27.5, result, 0.01);
    }

    @Test
    public void SCANTest1() {
        double result = scheduler.SCAN(start1, request1, true);
        assertEquals(166, result, 0.01);
    }

    @Test
    public void SCANTest2() {
        double result = scheduler.SCAN(start1, request1, false);
        assertEquals(164, result, 0.01);
    }

}
