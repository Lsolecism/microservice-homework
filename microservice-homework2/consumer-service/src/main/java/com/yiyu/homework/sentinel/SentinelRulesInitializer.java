package com.yiyu.homework.sentinel;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentinelRulesInitializer {

    public static final String QPS_RESOURCE = "qpsDemo";
    public static final String HOTSPOT_RESOURCE = "hotSpotDemo";
    public static final String SLOW_RESOURCE = "slowCallDemo";
    public static final String EXCEPTION_RATIO_RESOURCE = "exceptionRatioDemo";
    public static final String SYSTEM_ENTRY_RESOURCE = "systemEntryDemo";

    @PostConstruct
    public void initRules() {
        initQpsFlow();
        initHotParamFlow();
        initDegradeRules();
        initSystemCpuRule();
    }

    private void initQpsFlow() {
        FlowRule rule = new FlowRule();
        rule.setResource(QPS_RESOURCE);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(1);
        FlowRuleManager.loadRules(List.of(rule));
    }

    private void initHotParamFlow() {
        ParamFlowRule rule = new ParamFlowRule(HOTSPOT_RESOURCE);
        rule.setParamIdx(0);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(1);
        rule.setDurationInSec(1);
        rule.setClusterMode(false);
        rule.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
        ParamFlowRuleManager.loadRules(Collections.singletonList(rule));
    }

    private void initDegradeRules() {
        DegradeRule slow = new DegradeRule(SLOW_RESOURCE);
        slow.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        slow.setCount(200);
        slow.setSlowRatioThreshold(0.5);
        slow.setMinRequestAmount(5);
        slow.setTimeWindow(5);
        slow.setStatIntervalMs(1000);

        DegradeRule ex = new DegradeRule(EXCEPTION_RATIO_RESOURCE);
        ex.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        ex.setCount(0.35);
        ex.setMinRequestAmount(5);
        ex.setTimeWindow(5);
        ex.setStatIntervalMs(1000);

        DegradeRuleManager.loadRules(List.of(slow, ex));
    }

    private void initSystemCpuRule() {
        SystemRule rule = new SystemRule();
        rule.setHighestCpuUsage(0.85);
        SystemRuleManager.loadRules(Collections.singletonList(rule));
    }
}
