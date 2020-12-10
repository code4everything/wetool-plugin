package org.code4everything.wetool.plugin.support.cache;

import cn.hutool.core.thread.ThreadUtil;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class CounterTest extends TestCase {

    @Test
    public void test() {
        String secondKey = "test";

        Counter.increment(Counter.DEFAULT_MASTER_KEY, secondKey, 10, 1000);
        Assert.assertEquals(10, Counter.getCounter(secondKey));

        ThreadUtil.sleep(500);
        Counter.increment(Counter.DEFAULT_MASTER_KEY, secondKey, 5, 1000);
        Assert.assertEquals(15, Counter.getCounter(secondKey));

        ThreadUtil.sleep(2000);
        Assert.assertFalse(Counter.existsCounter(secondKey));

        Counter.increment(secondKey, 3);
        Assert.assertEquals(3, Counter.getCounter(secondKey));
    }
}
