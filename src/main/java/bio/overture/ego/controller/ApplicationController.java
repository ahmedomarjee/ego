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

package bio.overture.ego.controller;

import static bio.overture.ego.controller.resolver.PageableResolver.LIMIT;
import static bio.overture.ego.controller.resolver.PageableResolver.OFFSET;
import static bio.overture.ego.controller.resolver.PageableResolver.SORT;
import static bio.overture.ego.controller.resolver.PageableResolver.SORTORDER;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import bio.overture.ego.model.dto.CreateApplicationRequest;
import bio.overture.ego.model.dto.PageDTO;
import bio.overture.ego.model.dto.UpdateApplicationRequest;
import bio.overture.ego.model.entity.Application;
import bio.overture.ego.model.entity.Group;
import bio.overture.ego.model.entity.User;
import bio.overture.ego.model.enums.Fields;
import bio.overture.ego.model.search.Filters;
import bio.overture.ego.model.search.SearchFilter;
import bio.overture.ego.security.AdminScoped;
import bio.overture.ego.service.ApplicationService;
import bio.overture.ego.service.GroupService;
import bio.overture.ego.service.UserService;
import bio.overture.ego.view.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@Slf4j
@RestController
@RequestMapping("/applications")
public class ApplicationController {

  /** Dependencies */
  private final ApplicationService applicationService;

  private final GroupService groupService;
  private final UserService userService;

  @Autowired
  public ApplicationController(
      @NonNull ApplicationService applicationService,
      @NonNull GroupService groupService,
      @NonNull UserService userService) {
    this.applicationService = applicationService;
    this.groupService = groupService;
    this.userService = userService;
  }

  @AdminScoped
  @RequestMapping(method = GET, value = "")
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = LIMIT,
        required = false,
        dataType = "string",
        paramType = "query",
        value = "Number of results to retrieve"),
    @ApiImplicitParam(
        name = OFFSET,
        required = false,
        dataType = "string",
        paramType = "query",
        value = "Index of first result to retrieve"),
    @ApiImplicitParam(
        name = SORT,
        required = false,
        dataType = "string",
        paramType = "query",
        value = "Field to sort on"),
    @ApiImplicitParam(
        name = SORTORDER,
        required = false,
        dataType = "string",
        paramType = "query",
        value = "Sorting order: ASC|DESC. Default order: DESC"),
  })
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Page Applications")})
  @JsonView(Views.REST.class)
  public @ResponseBody PageDTO<Application> findApplications(
      @RequestHeader(value = AUTHORIZATION, required = true) final String accessToken,
      @RequestParam(value = "query", required = false) String query,
      @ApiIgnore @Filters List<SearchFilter> filters,
      Pageable pageable) {
    if (isEmpty(query)) {
      return new PageDTO<>(applicationService.listApps(filters, pageable));
    } else {
      return new PageDTO<>(applicationService.findApps(query, filters, pageable));
    }
  }

  @AdminScoped
  @RequestMapping(method = POST, value = "")
  @ApiResponses(
      value = {@ApiResponse(code = 200, message = "New Application", response = Application.class)})
  public @ResponseBody Application createApplication(
      @RequestHeader(value = AUTHORIZATION, required = true) final String accessToken,
      @RequestBody(required = true) CreateApplicationRequest request) {
    return applicationService.create(request);
  }

  @AdminScoped
  @RequestMapping(method = GET, value = "/{id}")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Application Details", response = Application.class)
      })
  @JsonView(Views.REST.class)
  public @ResponseBody Application getApplication(
      @RequestHeader(value = AUTHORIZATION, required = true) final String accessToken,
      @PathVariable(value = "id", required = true) UUID id) {
    return applicationService.getById(id);
  }

  @AdminScoped
  @RequestMapping(method = PUT, value = "/{id}")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Updated application info", response = Application.class)
      })
  public @ResponseBody Application updateApplication(
      @RequestHeader(value = AUTHORIZATION, required = true) final String accessToken,
      @PathVariable(name = "id", required = true) UUID id,
      @RequestBody(required = true) UpdateApplicationRequest updateRequest) {
    return applicationService.partialUpdate(id, updateRequest);
  }

  @AdminScoped
  @RequestMapping(method = DELETE, value = "/{id}")
  @ResponseStatus(value = HttpStatus.OK)
  public void deleteApplication(
      @RequestHeader(value = AUTHORIZATION, required = true) final String accessToken,
      @PathVariable(value = "id", required = true) UUID id) {
    applicationService.delete(id);
  }

  @AdminScoped
  @RequestMapping(method = GET, value = "/{id}/users")
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = LIMIT,
        required = false,
        dataType = "string",
        paramType = "query",
        value = "Number of results to retrieve"),
    @ApiImplicitParam(
        name = OFFSET,
        required = false,
        dataType = "string",
        paramType = "query",
        value = "Index of first result to retrieve"),
    @ApiImplicitParam(
        name = Fields.ID,
        required = false,
        dataType = "string",
        paramType = "path",
        value = "Search for ids containing this text"),
    @ApiImplicitParam(
        name = SORT,
        required = false,
        dataType = "string",
        paramType = "query",
        value = "Field to sort on"),
    @ApiImplicitParam(
        name = SORTORDER,
        required = false,
        dataType = "string",
        paramType = "query",
        value = "Sorting order: ASC|DESC. Default order: DESC"),
  })
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Page Users for an Application")})
  @JsonView(Views.REST.class)
  public @ResponseBody PageDTO<User> getUsersForApplication(
      @RequestHeader(value = AUTHORIZATION, required = true) final String accessToken,
      @PathVariable(value = "id", required = true) UUID id,
      @RequestParam(value = "query", required = false) String query,
      @ApiIgnore @Filters List<SearchFilter> filters,
      Pageable pageable) {
    if (isEmpty(query)) {
      return new PageDTO<>(userService.findUsersForApplication(id, filters, pageable));
    } else {
      return new PageDTO<>(userService.findUsersForApplication(id, query, filters, pageable));
    }
  }

  @AdminScoped
  @RequestMapping(method = GET, value = "/{id}/groups")
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = LIMIT,
        required = false,
        dataType = "string",
        paramType = "query",
        value = "Number of results to retrieve"),
    @ApiImplicitParam(
        name = OFFSET,
        required = false,
        dataType = "string",
        paramType = "query",
        value = "Index of first result to retrieve"),
    @ApiImplicitParam(
        name = Fields.ID,
        required = false,
        dataType = "string",
        paramType = "path",
        value = "Search for ids containing this text"),
    @ApiImplicitParam(
        name = SORT,
        required = false,
        dataType = "string",
        paramType = "query",
        value = "Field to sort on"),
    @ApiImplicitParam(
        name = SORTORDER,
        required = false,
        dataType = "string",
        paramType = "query",
        value = "Sorting order: ASC|DESC. Default order: DESC"),
  })
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Page Groups for an Application")})
  @JsonView(Views.REST.class)
  public @ResponseBody PageDTO<Group> getGroupsForApplication(
      @RequestHeader(value = AUTHORIZATION, required = true) final String accessToken,
      @PathVariable(value = "id", required = true) UUID id,
      @RequestParam(value = "query", required = false) String query,
      @ApiIgnore @Filters List<SearchFilter> filters,
      Pageable pageable) {
    if (isEmpty(query)) {
      return new PageDTO<>(groupService.findGroupsForApplication(id, filters, pageable));
    } else {
      return new PageDTO<>(groupService.findGroupsForApplication(id, query, filters, pageable));
    }
  }
}
