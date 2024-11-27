package com.rxlogix.security.jwt

import com.hazelcast.config.MapConfig
import com.hazelcast.core.HazelcastInstance
import groovy.util.logging.Slf4j

import java.time.Duration
import java.util.concurrent.TimeUnit

@Slf4j
class JwtRepository {
    private static final String ACCESS_TOKENS = 'access-tokens'
    private static final String REFRESH_TOKENS = 'refresh-tokens'
    private static final String ACCESS_TOKENS_BLACKLIST = 'access-token-blacklist'
    private static final Duration EVICT_BEFORE_TOKEN_EXP = Duration.ofSeconds(60)
    private final HazelcastInstance hazelcastInstance

    JwtRepository(HazelcastInstance hazelcastInstance, Duration tokenTtl) {
        this.hazelcastInstance = hazelcastInstance

        def accessTokenCacheTtl = tokenTtl > EVICT_BEFORE_TOKEN_EXP
                ? tokenTtl - EVICT_BEFORE_TOKEN_EXP
                : tokenTtl
        def config = hazelcastInstance.getConfig()
        config.addMapConfig(new MapConfig(ACCESS_TOKENS)
                .setTimeToLiveSeconds(accessTokenCacheTtl.seconds as int))
        config.addMapConfig(new MapConfig(REFRESH_TOKENS)
                .setTimeToLiveSeconds(accessTokenCacheTtl.seconds * 2 as int))
        config.addMapConfig(new MapConfig(ACCESS_TOKENS_BLACKLIST)
                .setTimeToLiveSeconds(tokenTtl.seconds as int))
    }
    
    AccessToken save(String username, AccessToken token) {
        hazelcastInstance.getMap(ACCESS_TOKENS)
                .put(username, [token: token.accessToken, exp: token.expiresAt])
        hazelcastInstance.getMap(REFRESH_TOKENS)
                .put(username, token.refreshToken)
        log.debug('Saved new access token for user {}.', username)
        token
    }

    Optional<AccessToken> get(String username) {
        def accessTokens = hazelcastInstance.getMap(ACCESS_TOKENS)
        Optional.ofNullable(accessTokens.get(username))
            .map { tokenData ->
                def refreshToken = hazelcastInstance.getMap(REFRESH_TOKENS)
                        .get(username)
                new AccessToken(accessToken: tokenData['token'], refreshToken: refreshToken, expiresAt: tokenData['exp'])
            }
    }

    Optional<String> delete(String username) {
        def accessTokens = hazelcastInstance.getMap(ACCESS_TOKENS)
        def accessToken = Optional.ofNullable(accessTokens.get(username))
            .map { tokenData ->
                accessTokens.delete(username)
                log.debug('Deleted access token for user {}.', username)
                tokenData['token']
            }
        hazelcastInstance.getMap(REFRESH_TOKENS).delete(username)

        accessToken
    }

    void addToBlacklist(String token, long ttlMillis) {
        hazelcastInstance.getMap(ACCESS_TOKENS_BLACKLIST)
                .put(token, true, ttlMillis, TimeUnit.MILLISECONDS)
        log.debug("Access token is blacklisted for {} milliseconds.", ttlMillis)
    }

    boolean isBlacklisted(String token) {
        hazelcastInstance.getMap(ACCESS_TOKENS_BLACKLIST)
                .containsKey(token)
    }

    Optional<String> getRefreshToken(String username) {
        def refreshToken = hazelcastInstance.getMap(REFRESH_TOKENS).get(username)
        Optional.ofNullable(refreshToken)
    }
}