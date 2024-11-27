package com.rxlogix.hibernate;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.IlikeExpression;
import org.hibernate.criterion.MatchMode;

public class EscapedILikeExpression extends IlikeExpression {

    public static final String ESCAPE_CHAR = "!";
    public static final String ESCAPE_CHAR_QUERY = " ESCAPE '" + EscapedILikeExpression.ESCAPE_CHAR + "'";

    public EscapedILikeExpression(String propertyName, Object value) {
        super(propertyName, value);
    }

    public EscapedILikeExpression(String propertyName, String value, MatchMode matchMode) {
        super(propertyName, value, matchMode);
    }

    public static String escapeString(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("!", ESCAPE_CHAR + "!").replaceAll("_", ESCAPE_CHAR + "_").replaceAll("%", ESCAPE_CHAR + "%");
    }

    public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery)
            throws HibernateException {
        String sql = super.toSqlString(criteria, criteriaQuery);
        sql = sql + ESCAPE_CHAR_QUERY;
        return sql;
    }

}
