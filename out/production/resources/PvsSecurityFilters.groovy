class PvsSecurityFilters {
    def filters = {
        all(controller: '*', action: '*') {
            before = {
                /**
                 * Prevent Cacheable HTTPS Response
                 */
                response.setHeader("Cache-Control", "no-cache, no-store")
                response.setHeader("Pragma", "no-cache")
            }
        }
    }
}
