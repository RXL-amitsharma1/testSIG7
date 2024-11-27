package com.reports

import grails.gorm.validation.ConstrainedProperty

class GormTagLib {
    static namespace = 'gorm'

    /**
     * Returns the max length of a field for a given domain.
     * If both 'maxSize' and 'size' are used, returns the lesser.  If neither is found, returns empty string.
     *
     * @attr clazz REQUIRED The full path of class name of the domain object being referenced
     * @attr field REQUIRED The field for which to look up the max length
     */
    def maxLength = { attrs ->
        String className = attrs.clazz
        String field = attrs.field

        if (!className || !field) {
            throwTagError("Tag [maxLength] is missing required attribute [clazz] and/or [field].")
        }

        Class clazz

        try {
            clazz = Class.forName(className, true, Thread.currentThread().getContextClassLoader())

        } catch (ClassNotFoundException e) {
            throwTagError("Tag [limit] could not find a class with name [$className]. Be sure to include the package name.")
        }

        if (!clazz.newInstance().hasProperty(field)) {
            throwTagError("Tag [maxLength] could not find field [$field] on class [$className].")
        }

        ConstrainedProperty fieldConstraint = clazz.constrainedProperties."$field"

        // The field exists but does not have any constraints, so return empty string
        if (!fieldConstraint) {
            out << ''

            return
        }

        Integer maxSize = fieldConstraint.getAppliedConstraint('maxSize')?.maxSize
        Integer size = fieldConstraint.getAppliedConstraint('size')?.range?.to

        if (maxSize && size) {
            out << Math.min(maxSize, size)

            return
        }

        out << (maxSize ?: size ?: '').toString()
    }
}
