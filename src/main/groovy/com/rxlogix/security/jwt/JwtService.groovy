package com.rxlogix.security.jwt

import com.rxlogix.user.User
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.UnsupportedJwtException

import java.time.Instant
import java.time.Duration

@Slf4j
class JwtService {
    public static final String CLAIM_AUTHORITIES = 'authr'
    public static final String CLAIM_USER_ID = 'uid'
    public static final String CLAIM_FULL_NAME = 'flnm'
    private final String secretKey
    private final Duration tokenTtl
    private final String issuer
    private final JwtRepository jwtRepository

    JwtService(String secretKey, Duration tokenTtl, String issuer, JwtRepository jwtRepository) {
        this.secretKey = secretKey
        this.tokenTtl = tokenTtl
        this.issuer = issuer
        this.jwtRepository = jwtRepository
    }

    AccessToken getAccessToken(String username, Collection<String> authorities, Map<String, Object> claims) {
        jwtRepository.get(username)
                .orElseGet { -> createToken(username, authorities, claims) }
    }

    @Transactional(readOnly = true)
    AccessToken refreshAccessToken(String refreshToken) {
        def decodedToken = validate(refreshToken)
        def username = decodedToken.body.getSubject()
        jwtRepository.getRefreshToken(username)
            .filter { token -> token == refreshToken}
            .orElseThrow { -> new JwtException('Unknown refresh token')}

        def user = User.findByUsername(username)
        def authorities = user.getAuthorities().collect{ role -> role.authority }
        def claims = [(CLAIM_USER_ID): user.id, (CLAIM_FULL_NAME): user.fullName]
        invalidateCurrentToken(username)
        createToken(username, authorities, claims)
    }

    private AccessToken createToken(String username, Collection<String> authorities, Map<String, Object> claims) {
        log.info("Creating JWT for {} with authorities: {}.", username, authorities)
        def currentTime = Instant.now()
        def expiration = currentTime + tokenTtl

        def accessToken = Jwts.builder()
                .setSubject(username)
                .addClaims(claims + [(CLAIM_AUTHORITIES): authorities.join(',')])
                .setIssuedAt(Date.from(currentTime))
                .setExpiration(Date.from(expiration))
                .setIssuer(issuer)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact()
        def refreshToken = createRefreshToken(username, currentTime)
        def token = new AccessToken(accessToken: accessToken, refreshToken: refreshToken, expiresAt: expiration.epochSecond)

        jwtRepository.save(username, token)
    }

    private String createRefreshToken(String username, Instant issuedAt) {
        Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(issuedAt + tokenTtl.multipliedBy(2)))
                .setIssuer(issuer)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact()
    }

    void invalidateCurrentToken(String username) {
        jwtRepository.delete(username)
                .ifPresent(this.&addToBlacklist)
    }

    private void addToBlacklist(String token) {
        def claims = parse(token).body
        def remainingTtl = claims.getExpiration().time - new Date().time
        if (remainingTtl > 0) {
            jwtRepository.addToBlacklist(token, remainingTtl)
            log.info('Access token for user {} invalidated.', claims.getSubject())
        }
    }

    Jws<Claims> validate(String token) {
        if (!token) {
            throw new JwtException('JWT is null.')
        }

        if (jwtRepository.isBlacklisted(token)) {
            throw new JwtException('Access token is blocked.')
        }

        def decodedToken = parse(token)
        if ('none' == decodedToken.getHeader().getAlgorithm()) {
            throw new UnsupportedJwtException('Unsigned Claims JWTs are not supported.')
        }

        def body = decodedToken.body
        if (issuer != body.getIssuer()) {
            throw new JwtException("Wrong issuer: ${body.getIssuer()}.")
        }

        if (body.getExpiration().before(new Date())) {
            throw new ExpiredJwtException(decodedToken.getHeader(), body, 'Access token is expired.')
        }
        decodedToken
    }

    private Jws<Claims> parse(String token) {
        Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
    }
}