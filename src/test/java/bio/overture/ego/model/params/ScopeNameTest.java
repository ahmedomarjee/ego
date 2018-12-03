/*
 * Copyright (c) 2018. The Ontario Institute for Cancer Research. All rights reserved.
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

package bio.overture.ego.model.params;

import bio.overture.ego.model.enums.AccessLevel;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class ScopeNameTest {
  @Test
  public void testRead() {
    val s = new ScopeName("song.READ");
    assertEquals("song", s.getName());
    assertEquals(AccessLevel.READ, s.getAccessLevel());
  }

  @Test
  public void testNamedStudy() {
    val s = new ScopeName("song.ABC.WRITE");
    assertEquals("song.ABC", s.getName());
    assertEquals(AccessLevel.WRITE, s.getAccessLevel());
  }
}