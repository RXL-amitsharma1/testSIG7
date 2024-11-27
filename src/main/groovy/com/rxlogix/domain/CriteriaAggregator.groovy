package com.rxlogix.domain

import grails.gorm.CriteriaBuilder
import grails.orm.HibernateCriteriaBuilder
import grails.util.Environment

// Aggregate query criteria for a Domain Class
// Example: def qa = new CriteriaAggregator(MyDomainClass)
//    qa.addCriteria { idEq(12345L) }
//     def results = qa.get()
public class CriteriaAggregator {
    private Class forClass
    private List<Closure> criteriaClosures

    // forClass should be a Grails DomainClass; but since Grails injects rather than inherits I can't specify the type better than "Class"
    public CriteriaAggregator(Class forClass) {
        this.forClass = forClass; criteriaClosures = new ArrayList<Closure>(10)
    }

    // criteriaClosure is the exact same type of closure you'd pass to DomainClass.withCriteria(criteriaClosure)
    public void addCriteria(Closure criteriaClosure) { criteriaClosures << criteriaClosure }

    public long count() { return runQuery('get') { projections { rowCount() } } }

    public def get(Closure additionalCriteria = null) {
        return runQuery('get', additionalCriteria)
    }

    // Query must return only a single row
    public def list(Closure additionalCriteria = null) {
        return runQuery('list', additionalCriteria)
    }


    // TODO this following if condition is for a grails issue
    private def runQuery(String method, Closure additionalCriteria = null) {
        if (Environment.current == Environment.TEST) {
            CriteriaBuilder criteriaBuilder = forClass.createCriteria()
            def critClosures = criteriaClosures
            // Bizarre that criteriaClosures won't evaluate properly inside the "$method" closure, but it won't so this works around that issue

            criteriaBuilder."$method" {
                critClosures.each { closure -> closure.delegate = criteriaBuilder; closure() }
                if (additionalCriteria) {
                    additionalCriteria.delegate = criteriaBuilder; additionalCriteria()
                }
            }
        } else {
            HibernateCriteriaBuilder criteriaBuilder = forClass.createCriteria()
            def critClosures = criteriaClosures
            // Bizarre that criteriaClosures won't evaluate properly inside the "$method" closure, but it won't so this works around that issue

            criteriaBuilder."$method" {
                critClosures.each { closure -> closure.delegate = criteriaBuilder; closure() }
                if (additionalCriteria) {
                    additionalCriteria.delegate = criteriaBuilder; additionalCriteria()
                }
            }
        }
    }
}