/*
 * Copyright (c) 2019. The Ontario Institute for Cancer Research. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package bio.overture.ego.controller;

import static org.assertj.core.api.Assertions.assertThat;

import bio.overture.ego.AuthorizationServiceMain;
import bio.overture.ego.model.entity.Application;
import bio.overture.ego.model.enums.ApplicationType;
import bio.overture.ego.service.ApplicationService;
import bio.overture.ego.utils.EntityGenerator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = AuthorizationServiceMain.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationControllerTest extends AbstractControllerTest {

  private static boolean hasRunEntitySetup = false;

  /** Dependencies */
  @Autowired private EntityGenerator entityGenerator;

  @Autowired private ApplicationService applicationService;

  @Override
  protected void beforeTest() {
    // Initial setup of entities (run once
    if (!hasRunEntitySetup) {
      entityGenerator.setupTestUsers();
      entityGenerator.setupTestApplications();
      entityGenerator.setupTestGroups();
      hasRunEntitySetup = true;
    }
  }

  @Test
  @SneakyThrows
  public void addApplication_Success() {
    val app =
        Application.builder()
            .name("addApplication_Success")
            .clientId("addApplication_Success")
            .clientSecret("addApplication_Success")
            .redirectUri("http://example.com")
            .status("Approved")
            .applicationType(ApplicationType.CLIENT)
            .build();

    val response = initStringRequest().endpoint("/applications").body(app).post();

    val responseStatus = response.getStatusCode();
    assertThat(responseStatus).isEqualTo(HttpStatus.OK);
    val responseJson = MAPPER.readTree(response.getBody());
    assertThat(responseJson.get("name").asText()).isEqualTo("addApplication_Success");
  }

  @Test
  @SneakyThrows
  public void addDuplicateApplication_Conflict() {
    val app1 =
        Application.builder()
            .name("addDuplicateApplication")
            .clientId("addDuplicateApplication")
            .clientSecret("addDuplicateApplication")
            .redirectUri("http://example.com")
            .status("Approved")
            .applicationType(ApplicationType.CLIENT)
            .build();

    val app2 =
        Application.builder()
            .name("addDuplicateApplication")
            .clientId("addDuplicateApplication")
            .clientSecret("addDuplicateApplication")
            .redirectUri("http://example.com")
            .status("Approved")
            .applicationType(ApplicationType.CLIENT)
            .build();

    val response1 = initStringRequest().endpoint("/applications").body(app1).post();

    val responseStatus1 = response1.getStatusCode();
    assertThat(responseStatus1).isEqualTo(HttpStatus.OK);

    val response2 = initStringRequest().endpoint("/applications").body(app2).post();
    val responseStatus2 = response2.getStatusCode();
    assertThat(responseStatus2).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  @SneakyThrows
  public void getApplication_Success() {
    val applicationId = applicationService.getByClientId("111111").getId();
    val response = initStringRequest().endpoint("/applications/%s", applicationId).get();

    val responseStatus = response.getStatusCode();
    val responseJson = MAPPER.readTree(response.getBody());

    assertThat(responseStatus).isEqualTo(HttpStatus.OK);
    assertThat(responseJson.get("name").asText()).isEqualTo("Application 111111");
    assertThat(responseJson.get("applicationType").asText()).isEqualTo("CLIENT");
  }
}
