package com.rxlogix.security

import groovy.transform.CompileStatic
import org.springframework.web.filter.GenericFilterBean

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

@CompileStatic
class DummyFilter extends GenericFilterBean {

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
// Do nothing
        chain.doFilter(request, response)
    }
}