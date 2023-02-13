package com.sl.mecm.authentication.server.config;

import com.alibaba.fastjson2.JSONObject;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean("mecmCacheManager")
    public CacheManager initCacheManager(){
        final URL myUrl = getClass().getResource("/ehcache.xml");
        assert myUrl != null;
        XmlConfiguration xmlConfig = new XmlConfiguration(myUrl);
        CacheManager cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
        cacheManager.init();
        return cacheManager;
    }

    @Bean("authTokenCache")
    public Cache<String, String> getAuthTokenCache(CacheManager mecmCacheManager){
        return mecmCacheManager.getCache("authTokenCache", String.class, String.class);
    }
}
