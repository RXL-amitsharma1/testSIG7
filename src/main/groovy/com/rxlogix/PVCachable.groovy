package com.rxlogix

import com.rxlogix.cache.CacheService

trait PVCachable {
    CacheService cacheService

    void saveToCache() {
        cacheService.saveToCache(this.cacheName, this.cacheKey, this)
    }
}