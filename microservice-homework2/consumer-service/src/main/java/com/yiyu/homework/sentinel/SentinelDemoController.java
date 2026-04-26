package com.yiyu.homework.sentinel;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sentinel")
public class SentinelDemoController {

    private final SentinelDemoService sentinelDemoService;

    public SentinelDemoController(SentinelDemoService sentinelDemoService) {
        this.sentinelDemoService = sentinelDemoService;
    }

    @GetMapping("/qps")
    public String qps() {
        return sentinelDemoService.qpsDemo();
    }

    @GetMapping("/hot")
    public String hot(@RequestParam(name = "sku", defaultValue = "sku-1") String sku) {
        return sentinelDemoService.hotSpotDemo(sku);
    }

    @GetMapping("/slow")
    public String slow() throws InterruptedException {
        return sentinelDemoService.slowCallDemo();
    }

    @GetMapping("/exception-ratio")
    public String exceptionRatio() {
        return sentinelDemoService.exceptionRatioDemo();
    }

    @GetMapping("/system-cpu")
    public String systemCpu() {
        return sentinelDemoService.systemEntryDemo();
    }
}
