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
package com.github.rmannibucau.sirona.plugin.hazelcast.gui;

import com.github.rmannibucau.sirona.counters.Unit;
import com.github.rmannibucau.sirona.reporting.web.plugin.api.MapBuilder;
import com.github.rmannibucau.sirona.reporting.web.plugin.api.Regex;
import com.github.rmannibucau.sirona.reporting.web.plugin.api.Template;
import com.github.rmannibucau.sirona.repositories.Repositories;
import com.github.rmannibucau.sirona.repositories.Repository;

import static com.github.rmannibucau.sirona.reporting.web.plugin.api.graph.Graphs.generateReport;

public class HazelcastEndpoints {
    @Regex
    public Template home() {
        return new Template("hazelcast/home.vm");
    }

    @Regex("/partitions")
    public Template partitions() {
        return hazelcastTemplate("Partition number", "partitions");
    }

    @Regex("/members")
    public Template members() {
        return hazelcastTemplate("Members number", "members");
    }

    @Regex("/([^/]*)/([0-9]*)/([0-9]*)")
    public String jsonDetail(final String role, final long start, final long end) {
        return generateReport(role, Repository.INSTANCE.findGaugeRole(role), start, end);
    }

    private static Template hazelcastTemplate(final String title, final String name) {
        return new Template("hazelcast/gauges.vm",
            new MapBuilder<String, Object>()
                .set ("title", title)
                .set("members", Repositories.names(Repositories.findByPrefixAndUnit("hazelcast-" + name + "-", Unit.UNARY)))
                .build());
    }
}
