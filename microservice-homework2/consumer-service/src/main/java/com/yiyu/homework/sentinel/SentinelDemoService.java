package com.yiyu.homework.sentinel;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

@Service
public class SentinelDemoService {

    @SentinelResource(value = SentinelRulesInitializer.QPS_RESOURCE, blockHandler = "blockQps")
    public String qpsDemo() {
        return "QPS demo ok";
    }

    @SuppressWarnings("unused")
    public static String blockQps(BlockException ex) {
        return "blocked: QPS flow control";
    }

    /** 热点参数限流需在 entry/exit 中显式传入参数，否则 ParamFlow 易异常（常见误报为白名单/校验类错误）。 */
    public String hotSpotDemo(String sku) {
        Object hotArg = sku != null ? sku : "";
        Entry entry = null;
        try {
            entry = SphU.entry(SentinelRulesInitializer.HOTSPOT_RESOURCE, EntryType.IN, 1, hotArg);
            return "hotspot ok, sku=" + sku;
        } catch (BlockException ex) {
            return "blocked: hotspot param flow, sku=" + sku;
        } finally {
            if (entry != null) {
                entry.exit(1, hotArg);
            }
        }
    }

    @SentinelResource(value = SentinelRulesInitializer.SLOW_RESOURCE, blockHandler = "blockSlow")
    public String slowCallDemo() throws InterruptedException {
        Thread.sleep(500);
        return "slow call finished";
    }

    @SuppressWarnings("unused")
    public static String blockSlow(BlockException ex) {
        return "blocked: slow-call ratio circuit breaking";
    }

    @SentinelResource(
            value = SentinelRulesInitializer.EXCEPTION_RATIO_RESOURCE,
            blockHandler = "blockExRatio",
            fallback = "fallbackExRatio")
    public String exceptionRatioDemo() {
        if (ThreadLocalRandom.current().nextDouble() < 0.6) {
            throw new IllegalStateException("simulated business failure");
        }
        return "exception ratio demo ok";
    }

    @SuppressWarnings("unused")
    public static String blockExRatio(BlockException ex) {
        return "blocked: exception ratio circuit breaking";
    }

    @SuppressWarnings("unused")
    public static String fallbackExRatio(Throwable t) {
        return "fallback: " + t.getClass().getSimpleName();
    }

    @SentinelResource(value = SentinelRulesInitializer.SYSTEM_ENTRY_RESOURCE, blockHandler = "blockSystem")
    public String systemEntryDemo() {
        return "system rule entry ok";
    }

    @SuppressWarnings("unused")
    public static String blockSystem(BlockException ex) {
        return "blocked: system protection (CPU usage)";
    }
}
