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

package org.overture.ego.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.overture.ego.model.entity.User;
import org.overture.ego.repository.UserRepository;
import org.overture.ego.repository.queryspecification.UserSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.springframework.data.jpa.domain.Specifications.where;

@Slf4j
@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private GroupService groupService;
  @Autowired
  private ApplicationService applicationService;

  public User create(@NonNull User userInfo) {
    return userRepository.save(userInfo);
  }

  public void addUsersToGroups(@NonNull String userId, @NonNull List<String> groupIDs){
    //TODO: change id to string
    val user = userRepository.findOne(Integer.parseInt(userId));
    groupIDs.forEach(grpId -> {
      val group = groupService.get(grpId);
      user.addNewGroup(group);
    });
    userRepository.save(user);
  }

  public void addUsersToApps(@NonNull String userId, @NonNull List<String> appIDs){
    //TODO: change id to string
    val user = userRepository.findOne(Integer.parseInt(userId));
    appIDs.forEach(appId -> {
      val app = applicationService.get(appId);
      user.addNewApplication(app);
    });
    userRepository.save(user);
  }

  public User get(@NonNull String userId) {
    //TODO: change id to string
    return userRepository.findOne(Integer.parseInt(userId));
  }

  public User getByName(@NonNull String userName) {
    return userRepository.findOneByNameIgnoreCase(userName);
  }

  public User update(@NonNull User updatedUserInfo) {
    return userRepository.save(updatedUserInfo);
  }

  public void delete(@NonNull String userId) {
    userRepository.delete(Integer.parseInt(userId));
  }

  public Page<User> listUsers(@NonNull Pageable pageable) {
    return userRepository.findAll(pageable);
  }

  public Page<User> findUsers(@NonNull String query, @NonNull Pageable pageable) {
    if(StringUtils.isEmpty(query)){
      return this.listUsers(pageable);
    }
    return userRepository.findAll(UserSpecification.containsText(query), pageable);
  }

  public void deleteUserFromGroup(@NonNull String userId, @NonNull List<String> groupIDs) {
    //TODO: change id to string
    val user = userRepository.findOne(Integer.parseInt(userId));
    groupIDs.forEach(grpId -> {
      user.removeGroup(Integer.parseInt(grpId));
    });
    userRepository.save(user);
  }

  public void deleteUserFromApp(@NonNull String userId, @NonNull List<String> appIDs) {
    //TODO: change id to string
    val user = userRepository.findOne(Integer.parseInt(userId));
    appIDs.forEach(appId -> {
      user.removeApplication(Integer.parseInt(appId));
    });
    userRepository.save(user);
  }

  public Page<User> findGroupsUsers(@NonNull String groupId, @NonNull Pageable pageable){
    return userRepository.findAll(
            UserSpecification.inGroup(Integer.parseInt(groupId)),
            pageable);
  }

  public Page<User> findGroupsUsers(@NonNull String groupId, @NonNull String query, @NonNull Pageable pageable){
    if(StringUtils.isEmpty(query)){
      return this.findGroupsUsers(groupId, pageable);
    }
    return userRepository.findAll(
            where(UserSpecification.inGroup(Integer.parseInt(groupId)))
                    .and(UserSpecification.containsText(query)),
            pageable);
  }

  public Page<User> findAppsUsers(@NonNull String appId, @NonNull Pageable pageable){
    return userRepository.findAll(
            UserSpecification.ofApplication(Integer.parseInt(appId)),
            pageable);
  }

  public Page<User> findAppsUsers(@NonNull String appId, @NonNull String query, @NonNull Pageable pageable){
    if(StringUtils.isEmpty(query)){
      return this.findAppsUsers(appId,pageable);
    }
    return userRepository.findAll(
            where(UserSpecification.ofApplication(Integer.parseInt(appId)))
                    .and(UserSpecification.containsText(query)),
            pageable);
  }

}
