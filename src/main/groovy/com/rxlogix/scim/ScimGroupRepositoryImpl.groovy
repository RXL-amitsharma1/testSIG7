package com.rxlogix.scim

import com.rxlogix.CRUDService
import com.rxlogix.SeedDataService
import com.rxlogix.enums.GroupType
import com.rxlogix.user.User
import com.rxlogix.user.Group
import com.rxlogix.user.UserGroupMapping
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import grails.plugins.scim.exceptions.InvalidRequestDataException
import grails.plugins.scim.resources.operations.PatchRequest
import grails.plugins.scim.repositories.ScimResourceRepository
import grails.plugins.scim.exceptions.ResourceConflictException
import grails.plugins.scim.exceptions.ResourceNotFoundException
import grails.plugins.scim.messages.ListResponse
import grails.plugins.scim.resources.Meta
import grails.plugins.scim.resources.ScimGroup
import grails.plugins.scim.resources.ScimUser
import groovy.util.logging.Slf4j
import org.apache.commons.lang.StringUtils



@Transactional
@Slf4j
class ScimGroupRepositoryImpl implements ScimResourceRepository<ScimGroup> {

    def CRUDService
    boolean allowExistingGroupMigrate = false

    private static final String GROUP_RESOURCE_TYPE = 'Group'
    private static final Integer MAX_RECORDS = 20

    private static final Map<String, String> propertiesMap = [displayName: 'name']


    @ReadOnly
    @Override
    ScimGroup get(String scimId, String excludedAttributes) {
        Group userGroup = scimId ? Group.findByScimIdAndIsActive(scimId, true) : null
        if (!userGroup) {
            throw new ResourceNotFoundException("Group Resource not found by id $scimId . Also its is deleted ${userGroup?.isDeleted}")
        }
        log.debug("Group get for ${userGroup.name} for scimId $scimId")
        return getFrom(userGroup, excludedAttributes)
    }

    @Override
    synchronized ScimGroup save(ScimGroup scimGroup) {
        if (!scimGroup.displayName) {
            throw new InvalidRequestDataException('Invalid incoming request as no group displayName for create')
        }
        Group existing = Group.findByNameIlikeAndIsActive(scimGroup.displayName, true)
        if (existing && (!allowExistingGroupMigrate || existing.scimId)) {
            throw new ResourceConflictException("Group resource already exist with userName $scimGroup.displayName")
        }
        if(existing){
            log.info("Upgrading existing group with Scim integration for : ${scimGroup.displayName}")
            existing.scimId = UUID.randomUUID().toString()
            existing.modifiedBy = SeedDataService.USERNAME
            existing.isActive = true
            CRUDService.save(existing)
            existing.save(flush: true, failOnError: true)
            return getFrom(existing)
        }
        Group userGroup = null
        userGroup = new Group(scimId: UUID.randomUUID().toString(), createdBy: SeedDataService.USERNAME, groupType: GroupType.USER_GROUP)
        log.debug("Group save for ${scimGroup.displayName}")
        copyGroup(scimGroup, userGroup)
        CRUDService.save(userGroup)
        userGroup.save(flush: true, failOnError: true)

        scimGroup.members.each {
            if(!it.id){
                log.warn("Group request save user not found as id is blank for ${scimGroup.displayName}")
                return
            }
            User user = User.findByScimId(it.id)
            if (user)
                UserGroupMapping.create(userGroup, user)
            else
                log.warn("User not found with scimId: $it.id for adding to group ${userGroup.name}")
        }
        userGroup.save(flush: true, failOnError: true)
        return getFrom(userGroup)
    }

    @Override
    ScimGroup update(ScimGroup scimGroup) {
        Group userGroup = scimGroup.id ? Group.findByScimIdAndIsActive(scimGroup.id, true) : null
        if (!userGroup) {
            throw new ResourceNotFoundException("Group Resource not found by id ${scimGroup?.id}")
        }
        if (!scimGroup.displayName) {
            throw new InvalidRequestDataException('Invalid incoming request as no group displayName for update')
        }
        userGroup.lock()
        copyGroup(scimGroup, userGroup)
        UserGroupMapping.removeAll(userGroup)
        CRUDService.update(userGroup)
        userGroup.save(flush: true, failOnError: true)
        scimGroup.members.each {
            if (!it.id) {
                log.warn("Group request save user not found as id is blank for ${scimGroup.displayName}")
                return
            }
            User user = User.findByScimId(it.id)
            if (user)
                UserGroupMapping.create(userGroup, user)
        }
        userGroup.save(flush: true, failOnError: true)
        return getFrom(userGroup)
    }

    @Override
    ScimGroup patch(PatchRequest patch) {
        Group userGroup = patch.id ? Group.findByScimIdAndIsActive(patch.id, true) : null
        if (!userGroup) {
            throw new ResourceNotFoundException("Group Resource not found by id $patch?.id")
        }
        userGroup.lock()
        log.debug("Group Patch for ${userGroup.name}")
        patch.Operations.each {
            switch (it.op?.toLowerCase()) {
                case 'replace':
                    if (!it.path) {
                        it.value?.each { obj ->
                            updateProperties(userGroup, obj.key, obj.value)
                        }
                    } else if (it.path.startsWith('members') && it.path.endsWith('.id')) {
                        if (!it.value) {
                            log.warn("Group request patch user not found as id is blank for ${userGroup.name}")
                            return
                        }
                        User user = User.findByScimId(it.value)
                        if (user && !UserGroupMapping.exists(userGroup.id, user.id)) {
                            UserGroupMapping.create(userGroup, user)
                        }
                    } else if (it.path in propertiesMap.keySet()) {
                        userGroup[getGroupPropertyFromScim(it.path)] = it.value
                    }
                    CRUDService.update(userGroup)
                    break
                case 'add':
                    if (!(it.value instanceof Collection)) {
                        log.warn("Invalid value of members to add : ${it.value}")
                        return
                    }
                    it.value?.each {
                        if (!it.value) {
                            log.warn("Group request patch user not found as id is blank for ${userGroup.name}")
                            return
                        }
                        User user = User.findByScimId(it.value)
                        if (user && !UserGroupMapping.exists(userGroup.id, user.id)) {
                            UserGroupMapping.create(userGroup, user)
                        }
                    }
                    break;
                case 'remove':
                    if (it.path == 'members' && !it.value) {
                        UserGroupMapping.removeAll(userGroup)
                    } else if (it.path && it.path ==~ /members\[value eq "[\w|\d|\-]*"]/) {
                        User user = User.findByScimId(it.path.replace('members[value eq "', '').replace('"]', ''))
                        if (user) {
                            UserGroupMapping.remove(userGroup, user)
                        }
                    } else {
                        it.value?.each {
                            if (!it.value) {
                                log.warn("Group request patch user not found as id is blank for ${userGroup.name}")
                                return
                            }
                            User user = User.findByScimId(it.value)
                            UserGroupMapping.remove(userGroup, user)
                        }
                    }
                    break;
            }
        }
        userGroup.save(flush: true, failOnError: true)
        return getFrom(userGroup)
    }

    @Override
    void delete(String id) {
        Group userGroup = id ? Group.findByScimIdAndIsActive(id, true) : null
        if (!userGroup) {
            throw new ResourceNotFoundException("Group Resource not found by id $id")
        }
        userGroup.lock()
        log.debug("Group Delete for ${userGroup.name}")
        userGroup.isActive = false
        userGroup.save(flush: true, failOnError: true)
    }

    @ReadOnly
    @Override
    ListResponse findAll(String filter, Integer count, Integer startIndex, String excludedAttributes) {
        startIndex = startIndex ?: 1
        log.debug("Group search for filter ${filter}")
        String displayName = StringUtils.substringAfter(filter, 'displayName eq')?.replaceAll('"', '')?.trim()
        List<Group> groupList = Group.createCriteria().list(max: count ?: MAX_RECORDS, offset: (startIndex - 1)) {
            eq('isActive', true)
            if (displayName) {
                ilike('name', displayName)
            }
            if (allowExistingGroupMigrate) {
                isNotNull('scimId')
            }
            order("name", "asc")
        }
        ListResponse listResponse = new ListResponse(startIndex: startIndex, itemsPerPage: count ?: MAX_RECORDS, totalResults: groupList.totalCount)
        listResponse.Resources = groupList.collect { getFrom(it, excludedAttributes) }
        return listResponse
    }

    private void copyGroup(ScimGroup source, Group destination) {
        destination.with {
            modifiedBy = SeedDataService.USERNAME
            name = source.displayName
        }
    }

    private ScimGroup getFrom(Group group, String excludedAttributes = null) {
        ScimGroup scimGroup = new ScimGroup(id: group.scimId, externalId: group.id)
        scimGroup.with {
            displayName = group.name
            meta = new Meta(created: group.dateCreated, lastModified: group.lastUpdated, resourceType: GROUP_RESOURCE_TYPE, location: scimGroup.id)
            members = UserGroupMapping.findAllByGroup(group)*.user.collect {
                new ScimUser(externalId: it.id, id: it.scimId, userName: it.username, schemas: null)
            }
        }
        if (excludedAttributes) {
            excludedAttributes.split(',').each {
                scimGroup.setProperty(it, null)
                //Making it null so that when response render that attribute won't render
            }
        }
        return scimGroup
    }

    private String getGroupPropertyFromScim(String property) {
        propertiesMap.get(property)
    }

    private void updateProperties(Group userGroup, String path, def value) {
        userGroup[getGroupPropertyFromScim(path)] = value
    }


}