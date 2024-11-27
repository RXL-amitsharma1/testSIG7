package com.rxlogix.session

import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener

class PVHttpSessionServletListener implements HttpSessionListener {
    @Override
    void sessionCreated(HttpSessionEvent se) {
        se.getSession().getMaxInactiveInterval()
    }

    @Override
    void sessionDestroyed(HttpSessionEvent se) {
        se.getSession().getMaxInactiveInterval()
    }
}