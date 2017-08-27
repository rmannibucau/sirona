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
package com.github.rmannibucau.sirona.jta;

import com.github.rmannibucau.sirona.Role;
import com.github.rmannibucau.sirona.counters.Unit;
import com.github.rmannibucau.sirona.gauges.Gauge;
import com.github.rmannibucau.sirona.gauges.GaugeFactory;

import java.util.concurrent.atomic.AtomicLong;

public class JTAGauges implements GaugeFactory {
    public static final Role JTA_COMMITED = new Role("jta-commited", Unit.UNARY);
    public static final Role JTA_ROLLBACKED = new Role("jta-rollbacked", Unit.UNARY);
    public static final Role JTA_ACTIVE = new Role("jta-active", Unit.UNARY);

    static final AtomicLong ACTIVE = new AtomicLong(0);
    static final AtomicLong COMMITTED = new AtomicLong(0);
    static final AtomicLong ROLLBACKED = new AtomicLong(0);

    @Override
    public Gauge[] gauges() {
        return new Gauge[]{
            new JTAGauge(JTA_COMMITED, COMMITTED),
            new JTAGauge(JTA_ROLLBACKED, ROLLBACKED),
            new JTAActiveGauge(JTA_ACTIVE, ACTIVE)
        };
    }

    protected static class JTAGauge implements Gauge {
        private final Role role;
        protected final AtomicLong counter;

        protected JTAGauge(final Role role, final AtomicLong counter) {
            this.role = role;
            this.counter = counter;
        }

        @Override
        public Role role() {
            return role;
        }

        @Override
        public double value() {
            return counter.getAndSet(0);
        }
    }

    protected static class JTAActiveGauge extends JTAGauge {
        protected JTAActiveGauge(final Role role, final AtomicLong counter) {
            super(role, counter);
        }

        @Override
        public double value() {
            return counter.get();
        }
    }
}
