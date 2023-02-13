package com.sl.mecm.authentication.server.config;

import com.alibaba.fastjson2.JSONObject;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TokenCacheEventListener implements CacheEventListener<String, JSONObject> {
    @Override
    public void onEvent(CacheEvent<? extends String, ? extends JSONObject> cacheEvent) {
        log.info("Token Cache Event for:" + cacheEvent.getType().name()
                + ", key:[" + cacheEvent.getKey() + "]"
                + ", new value:[" + cacheEvent.getNewValue() + "]"
                + ", old value:[" + cacheEvent.getOldValue() + "]");
    }
}
