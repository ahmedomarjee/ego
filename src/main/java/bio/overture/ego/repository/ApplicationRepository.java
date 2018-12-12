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

package bio.overture.ego.repository;

import bio.overture.ego.model.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface ApplicationRepository
    extends PagingAndSortingRepository<Application, UUID>, JpaSpecificationExecutor {

  Application findOneByClientIdIgnoreCase(String clientId);

  @Query("select id from Application where concat(clientId,clientSecret)=?1")
  UUID findByBasicToken(String token);

  Application findOneByNameIgnoreCase(String name);

  Application findOneByName(String name);

  Page<Application> findAllByStatusIgnoreCase(String status, Pageable pageable);
}
