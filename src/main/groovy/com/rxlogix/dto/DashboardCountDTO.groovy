package com.rxlogix.dto

import groovy.transform.ToString

@ToString
class DashboardCountDTO {
    Map<Long, Map<String, Integer>> userDispCountsMap = [:]
    Map<Long, Map<String, Integer>> groupDispCountsMap = [:]
    Map<Long, Map<String, Integer>> userDueDateCountsMap = [:]
    Map<Long, Map<String, Integer>> groupDueDateCountsMap = [:]
    Map<Long, Map<String, Integer>> execDispCountMap = [:]
    Map<Long, Map<String, Integer>> prevDispCountMap = [:]
    Map<Long, Map<String, Integer>> prevGroupDispCountMap = [:]
    String dispCountKey
    String dueDateCountKey
    String groupDispCountKey
    String groupDueDateCountKey
}
