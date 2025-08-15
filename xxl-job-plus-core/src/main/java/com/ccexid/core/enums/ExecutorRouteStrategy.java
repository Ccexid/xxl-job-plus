package com.ccexid.core.enums;

import com.ccexid.core.route.ExecutorRouter;
import com.ccexid.core.route.strategy.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExecutorRouteStrategy implements IEnums {
    FIRST("First", new ExecutorRouteFirst()),
    LAST("Last", new ExecutorRouteLast()),
    ROUND("Round", new ExecutorRouteRound()),
    RANDOM("Random", new ExecutorRouteRandom()),
    CONSISTENT_HASH("Consistent Hash", new ExecutorRouteConsistentHash()),
    LEAST_FREQUENTLY_USED("Least Frequently Used", new ExecutorRouteLFU()),
    LEAST_RECENTLY_USED("Least Recently Used", new ExecutorRouteLRU()),
    FAILOVER("Failover", new ExecutorRouteFailover()),
    BUSY_OVER("Busy over", new ExecutorRouteBusyOver()),
    SHARDING_BROADCAST("Sharding Broadcast", null);
    private final String title;
    private final ExecutorRouter router;
}
