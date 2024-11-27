package com.rxlogix

import com.google.common.io.BaseEncoding
import com.rxlogix.config.SpotfireSession
import com.rxlogix.user.User
import com.rxlogix.util.SecurityUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders

import javax.servlet.http.HttpSession
import java.security.SecureRandom

@Secured(["isAuthenticated()"])
class SpotfireController {

    def clientId
    def clientSecret
    def userService
    def springSecurityService
    def spotfireService

    public static final String CLIENT_ID_PARAMETER = "client_id"
    static final String RETURN_URI_PARAMETER = "return_uri"
    public static final String CSRF_TOKEN_PARAMETER = "csrf_token"

    public static final String CLIENT_SECRET_PARAMETER = 'client_secret'
    def clientSecretsMap = [:]


    @Secured(['ROLE_ADMIN', 'ROLE_DEV'])
    def index() {
        render "This is a great software."
    }

    def auth() {
        def clientId = params[CLIENT_ID_PARAMETER]
        def returnUri = params[RETURN_URI_PARAMETER]
        def csrfToken = params[CSRF_TOKEN_PARAMETER]

        // Store the parameters in the session
        final HttpSession session = request.getSession()

        storeClientId(session, clientId)
        storeReturnUri(session, returnUri)
        storeCsrfToken(session, csrfToken)
        session.setAttribute('spotfire_auth', 'true')

        redirect controller: 'login', action: 'index'
    }

    def validate() {
        def secret = Holders.config.spotfire.token_secret

        def authenticationToken = params['auth_token']
        def decryptedText = SecurityUtil.decrypt(secret, authenticationToken)
        def currentUser = User.findByUsername(decryptedText)
        def user = userService.getUser()
        log.error(user)

        if (!currentUser || springSecurityService.isLoggedIn()) {
            // Invalid authentication token
            sendError(response, 403, "Forbidden")
            return
        }
        AuthenticationEntry authenticationEntry = new AuthenticationEntry('PV', decryptedText)

        // Validate the client secret
        final String clientSecret = decode(request.getParameter(CLIENT_SECRET_PARAMETER))
        final String clientId = authenticationEntry.getClientId()
        if (((clientSecret == null) || !clientSecret.equals(this.clientSecretsMap.get(clientId))) && false) {
            // Invalid client secret
            log.error("Invalid client secret, returning 403 Forbidden")
            sendError(response, 403, "Forbidden")
            return
        }

        // Return the authenticated principal
        log.info("Returning the authenticated principal to service");
        final String username = authenticationEntry.getUsername() + "@SPOTFIRE"
        final String email = currentUser.email
        final String displayName = currentUser.fullName

        Properties props = new Properties()
        props.setProperty("username", username)
        props.setProperty("email", email)
        props.setProperty("display.name", displayName)
        try {
            response.setContentType("text/plain");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            final OutputStream out = response.getOutputStream()
            props.store(out, null)
            return
        } catch (IOException e) {
            log.error("Error returning authenticated username, trying to respond with a 500 Internal Server Error");
            sendError(response, 500, "Internal Server Error")
            return
        }

        redirect controller: 'spotfire', action: 'index'
    }

    @Secured('permitAll')
    def rx_validate() {
        def token = params.get('token')
        log.info("The token to be validate is :${token}")
        SpotfireSession sessionInfo = spotfireService.getSpotfireSessionInfo(token)

        if (sessionInfo && !sessionInfo.deleted) {
            log.info("Session found. The token is valid:${token}")
            render status: 200, contentType: 'application/json',
                    text: '{"status": "Ok", "data": ' + sessionInfo.toJson() + '}'
        } else {
            log.info("Session can not be found. The token is invalid:${token}")
            render status: 404, contentType: 'application/json', text: '{"status": "Failed"}'
        }
    }

    def sendError(response, returnCode, message) {
        render status: returnCode, text: message
    }

    /**
     * Creates a new authentication token, identifying the given username, for the specified client ID.
     *
     * @param clientId the client ID
     * @param username the username
     * @return the new authentication token
     */
    private String createAuthenticationToken(String clientId, String username) {
        final String authenticationToken = new BigInteger(130, new SecureRandom()).toString(32)
        AuthenticationEntry authenticationEntry = new AuthenticationEntry(clientId, username)
        this.authenticationTokensMap.put(authenticationToken, authenticationEntry)
        return authenticationToken
    }

    private String decode(String s) {
        return (s != null) ? new String(BaseEncoding.base64().decode(s), "UTF-8") : null
    }

    /**
     * Stores the specified client ID as a session attribute in the given {@link HttpSession}.
     *
     * @param session the {@code HttpSession} object
     * @param clientId the client ID
     */
    private void storeClientId(HttpSession session, String clientId) {
        if (clientId == null) {
            log.info("Clearing the client ID stored in the session")
        } else {
            log.info("Storing the client ID " + clientId + " in the session")
        }
        session.setAttribute(CLIENT_ID_PARAMETER, clientId)
    }

    /**
     * Returns the request URI that is stored as a session attribute in the specified {@link HttpSession}.
     *
     * @param session the {@code HttpSession} object
     * @return the request URI
     */
    private String getReturnUri(HttpSession session) {
        final String returnUri = (String) session.getAttribute(RETURN_URI_PARAMETER)
        log.info("Retrieving the stored return URI " + returnUri + " from the session")
        returnUri
    }

    /**
     * Stores the specified return URI as a session attribute in the given {@link HttpSession}.
     *
     * @param session the {@code HttpSession} object
     * @param returnUri the return URI
     */
    private void storeReturnUri(HttpSession session, String returnUri) {
        if (returnUri == null) {
            log.info("Clearing the return URI stored in the session")
        } else {
            log.info("Storing the return URI " + returnUri + " in the session")
        }
        session.setAttribute(RETURN_URI_PARAMETER, returnUri)
    }

    /**
     * Returns the CSRF token that is stored as a session attribute in the specified {@link HttpSession}.
     *
     * @param session the {@code HttpSession} object
     * @return the CSRF token
     */
    private String getCsrfToken(HttpSession session) {
        final String csrfToken = (String) session.getAttribute(CSRF_TOKEN_PARAMETER)
        log("Retrieving the stored CSRF token " + csrfToken + " from the session")
        return csrfToken
    }

    /**
     * Stores the specified CSRF token as a session attribute in the given {@link HttpSession}.
     *
     * @param session the {@code HttpSession} object
     * @param csrfToken the CSRF token
     */
    private void storeCsrfToken(HttpSession session, String csrfToken) {
        if (csrfToken == null) {
            log.info("Clearing the CSRF token stored in the session")
        } else {
            log.info("Storing the CSRF token " + csrfToken + " in the session")
        }
        session.setAttribute(CSRF_TOKEN_PARAMETER, csrfToken)
    }
}

/**
 * This class contains authentication information that is used when
 * the authentication filter validates the authentication cookie.
 */
class AuthenticationEntry {

    private final String clientId
    private final String username

    /**
     * Creates a new {@link AuthenticationEntry} instance with the specified properties.
     *
     * @param clientId the client ID
     * @param username the username
     */
    AuthenticationEntry(String clientId, String username) {
        this.clientId = clientId
        this.username = username
    }

    /**
     * Returns the client ID.
     *
     * @return the client ID
     */
    String getClientId() {
        return this.clientId
    }

    /**
     * Returns the username.
     *
     * @return the username
     */
    String getUsername() {
        return this.username
    }

} // AuthenticationEntry




