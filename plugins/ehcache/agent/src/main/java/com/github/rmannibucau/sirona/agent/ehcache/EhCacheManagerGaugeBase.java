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
package com.github.rmannibucau.sirona.agent.ehcache;

import net.sf.ehcache.CacheManager;
import com.github.rmannibucau.sirona.Role;
import com.github.rmannibucau.sirona.counters.Unit;
import com.github.rmannibucau.sirona.gauges.Gauge;

public abstract class EhCacheManagerGaugeBase implements Gauge {
    private final Role role;
    protected final CacheManager manager;

    public EhCacheManagerGaugeBase(final String gauge, final CacheManager cacheManager) {
        this.role = new Role("ehcache-" + cacheManager.getName() + "-" + gauge, Unit.UNARY);
        this.manager = cacheManager;
    }

    @Override
    public Role role() {
        return role;
    }
}
