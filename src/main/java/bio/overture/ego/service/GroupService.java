/*
 * Copyright (c) 2017. The Ontario Institute for Cancer Research. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bio.overture.ego.service;

import bio.overture.ego.model.entity.Group;
import bio.overture.ego.model.entity.GroupPermission;
import bio.overture.ego.model.enums.AccessLevel;
import bio.overture.ego.model.exceptions.NotFoundException;
import bio.overture.ego.model.exceptions.PostWithIdentifierException;
import bio.overture.ego.model.params.PolicyIdStringWithAccessLevel;
import bio.overture.ego.model.search.SearchFilter;
import bio.overture.ego.repository.ApplicationRepository;
import bio.overture.ego.repository.GroupRepository;
import bio.overture.ego.repository.UserRepository;
import bio.overture.ego.repository.queryspecification.GroupSpecification;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.util.UUID.fromString;
import static org.springframework.data.jpa.domain.Specifications.where;

@Service
public class GroupService extends AbstractNamedService<Group, UUID> {

  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final ApplicationRepository applicationRepository;
  private final ApplicationService applicationService;
  private final PolicyService policyService;
  private final GroupPermissionService permissionService;

  @Autowired
  public GroupService(
      @NonNull GroupRepository groupRepository,
      @NonNull UserRepository userRepository,
      @NonNull ApplicationRepository applicationRepository,
      @NonNull PolicyService policyService,
      @NonNull ApplicationService applicationService,
      @NonNull GroupPermissionService permissionService) {
    super(Group.class, groupRepository);
    this.groupRepository = groupRepository;
    this.userRepository = userRepository;
    this.applicationRepository = applicationRepository;
    this.applicationService = applicationService;
    this.policyService = policyService;
    this.permissionService = permissionService;
  }

  public Group create(@NonNull Group groupInfo) {
    if (Objects.nonNull(groupInfo.getId())) {
      throw new PostWithIdentifierException();
    }

    return getRepository().save(groupInfo);
  }

  public Group addAppsToGroup(@NonNull String grpId, @NonNull List<String> appIDs) {
    val group = getById(fromString(grpId));
    appIDs.forEach(
        appId -> {
          val app = applicationService.get(appId);
          group.getApplications().add(app);
        });
    return getRepository().save(group);
  }

  public Group addUsersToGroup(@NonNull String grpId, @NonNull List<String> userIds) {
    val group = getById(fromString(grpId));
    userIds.forEach(
        userId -> {
          val user =
              userRepository
                  .findById(fromString(userId))
                  .orElseThrow(
                      () ->
                          new NotFoundException(
                              String.format("Could not find User with ID: %s", userId)));
          group.getUsers().add(user);
          user.getGroups().add(group);
        });
    return groupRepository.save(group);
  }

  public Group addGroupPermissions(
      @NonNull String groupId, @NonNull List<PolicyIdStringWithAccessLevel> permissions) {
    val group = getById(fromString(groupId));
    permissions.forEach(
        permission -> {
          val policy = policyService.get(permission.getPolicyId());
          val mask = AccessLevel.fromValue(permission.getMask());
          val gp = new GroupPermission();
          gp.setPolicy(policy);
          gp.setAccessLevel(mask);
          gp.setOwner(group);
          group.getPermissions()
              .add(gp);
        });
    return getRepository().save(group);
  }

  public Group get(@NonNull String groupId) {
    return getById(fromString(groupId));
  }

  public Group update(@NonNull Group other) {
    val existingGroup = getById(other.getId());

    val updatedGroup =
        Group.builder()
            .id(existingGroup.getId())
            .name(other.getName())
            .description(other.getDescription())
            .status(other.getStatus())
            .applications(existingGroup.getApplications())
            .users(existingGroup.getUsers())
            .build();

    return groupRepository.save(updatedGroup);
  }

  // TODO - this was the original update - will use an improved version of this for the PATCH
  public Group partialUpdate(@NonNull Group other) {
    val existingGroup = getById(other.getId());

    val builder =
        Group.builder()
            .id(other.getId())
            .name(other.getName())
            .description(other.getDescription())
            .status(other.getStatus());

    if (other.getApplications() != null) {
      builder.applications(other.getApplications());
    } else {
      builder.applications(existingGroup.getApplications());
    }

    if (other.getUsers() != null) {
      builder.users(other.getUsers());
    } else {
      builder.users(existingGroup.getUsers());
    }

    val updatedGroup = builder.build();

    return groupRepository.save(updatedGroup);
  }

  public Page<Group> listGroups(@NonNull List<SearchFilter> filters, @NonNull Pageable pageable) {
    return groupRepository.findAll(GroupSpecification.filterBy(filters), pageable);
  }

  public Page<GroupPermission> getGroupPermissions(
      @NonNull String groupId, @NonNull Pageable pageable) {
    val groupPermissions = ImmutableList.copyOf(getById(fromString(groupId)).getPermissions());
    return new PageImpl<>(groupPermissions, pageable, groupPermissions.size());
  }

  public Page<Group> findGroups(
      @NonNull String query, @NonNull List<SearchFilter> filters, @NonNull Pageable pageable) {
    return groupRepository.findAll(
        where(GroupSpecification.containsText(query)).and(GroupSpecification.filterBy(filters)),
        pageable);
  }

  public Page<Group> findUserGroups(
      @NonNull String userId, @NonNull List<SearchFilter> filters, @NonNull Pageable pageable) {
    return groupRepository.findAll(
        where(GroupSpecification.containsUser(fromString(userId)))
            .and(GroupSpecification.filterBy(filters)),
        pageable);
  }

  public Page<Group> findUserGroups(
      @NonNull String userId,
      @NonNull String query,
      @NonNull List<SearchFilter> filters,
      @NonNull Pageable pageable) {
    return groupRepository.findAll(
        where(GroupSpecification.containsUser(fromString(userId)))
            .and(GroupSpecification.containsText(query))
            .and(GroupSpecification.filterBy(filters)),
        pageable);
  }

  public Page<Group> findApplicationGroups(
      @NonNull String appId, @NonNull List<SearchFilter> filters, @NonNull Pageable pageable) {
    return groupRepository.findAll(
        where(GroupSpecification.containsApplication(fromString(appId)))
            .and(GroupSpecification.filterBy(filters)),
        pageable);
  }

  public Page<Group> findApplicationGroups(
      @NonNull String appId,
      @NonNull String query,
      @NonNull List<SearchFilter> filters,
      @NonNull Pageable pageable) {
    return groupRepository.findAll(
        where(GroupSpecification.containsApplication(fromString(appId)))
            .and(GroupSpecification.containsText(query))
            .and(GroupSpecification.filterBy(filters)),
        pageable);
  }

  public void deleteAppsFromGroup(@NonNull String grpId, @NonNull List<String> appIDs) {
    val group = getById(fromString(grpId));
    // TODO - Properly handle invalid IDs here
    appIDs.forEach(
        appId -> {
          val app =
              applicationRepository
                  .findById(fromString(appId))
                  .orElseThrow(
                      () ->
                          new NotFoundException(
                              String.format("Could not find Application with ID: %s", appId)));
          group.getApplications().remove(app);
          app.getGroups().remove(group);
        });
    groupRepository.save(group);
  }

  public void deleteGroupPermissions(@NonNull String userId, @NonNull List<String> permissionsIds) {
    val group = getById(fromString(userId));
    permissionsIds.forEach(
        permissionsId -> {
          group.getPermissions().remove(permissionService.get(permissionsId));
        });
    groupRepository.save(group);
  }

  public void delete(String id) {
    delete(fromString(id));
  }
}
