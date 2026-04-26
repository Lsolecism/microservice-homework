package com.yiyu.consumer.fallback;

import com.yiyu.consumer.client.ProviderClient;
import org.springframework.stereotype.Component;

@Component
public class ProviderClientFallback implements ProviderClient {
    @Override
    public String hello(String name, int sleepMs) {
        return "Consumer fallback: provider unavailable, name=" + name;
    }
}
