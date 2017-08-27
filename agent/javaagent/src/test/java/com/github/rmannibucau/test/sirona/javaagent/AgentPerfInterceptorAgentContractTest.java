/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.rmannibucau.test.sirona.javaagent;

import com.github.rmannibucau.sirona.Role;
import com.github.rmannibucau.sirona.counters.Counter;
import com.github.rmannibucau.sirona.javaagent.AgentContext;
import com.github.rmannibucau.sirona.repositories.Repository;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AgentPerfInterceptorAgentContractTest {
    @Test
    public void key() {
        final Counter.Key key = AgentContext.key("key");
        assertEquals(Role.PERFORMANCES, key.getRole());
        assertEquals("key", key.getName());
    }

    @Test
    public void start() {
        final String key = "start";
        AgentContext.listeners(key, new byte[0]);
        final AgentContext context = AgentContext.startOn( null,key,null);
        context.stop(null);
        assertEquals("com.github.rmannibucau.sirona.javaagent.AgentContext", context.getClass().getName());
        assertEquals(1, Repository.INSTANCE.getCounter(AgentContext.key(key)).getHits());
    }

    @Test
    public void attributes() {
        final String key = "ref";
        final Object instance = new Object();
        AgentContext.listeners(key, new byte[0]);
        final AgentContext context = AgentContext.startOn( instance, key, null);
        context.stop(null);
        assertEquals(instance, context.getReference());
        assertEquals(key, context.getKey());
        assertEquals(1, Repository.INSTANCE.getCounter(AgentContext.key(key)).getHits());
    }
}
