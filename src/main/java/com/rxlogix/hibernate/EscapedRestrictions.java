package com.rxlogix.hibernate;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;


public class EscapedRestrictions {
    public static Criterion ilike(String propertyName, String value) {
        return new EscapedILikeExpression(propertyName, value);
    }

    public static Criterion ilike(String propertyName, String value, MatchMode matchMode) {
        return new EscapedILikeExpression(propertyName, value, matchMode);
    }
}
