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
package com.github.rmannibucau.sirona.store.status;

import com.github.rmannibucau.sirona.alert.AlertListener;
import com.github.rmannibucau.sirona.alert.AlerterSupport;
import com.github.rmannibucau.sirona.configuration.Configuration;
import com.github.rmannibucau.sirona.configuration.ioc.Created;
import com.github.rmannibucau.sirona.configuration.ioc.Destroying;
import com.github.rmannibucau.sirona.status.NodeStatus;
import com.github.rmannibucau.sirona.status.NodeStatusReporter;
import com.github.rmannibucau.sirona.store.BatchFuture;
import com.github.rmannibucau.sirona.util.DaemonThreadFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Collections.singletonMap;

public class PeriodicNodeStatusDataStore implements NodeStatusDataStore {
    private static final Logger LOGGER = Logger.getLogger(PeriodicNodeStatusDataStore.class.getName());

    private final AtomicReference<BatchFuture> scheduledTask = new AtomicReference<BatchFuture>();
    protected final AtomicReference<NodeStatus> status = new AtomicReference<NodeStatus>();
    protected final HashMap<String, NodeStatus> statusAsMap = new HashMap<String, NodeStatus>();
    protected final NodeStatusReporter nodeStatusReporter;
    protected final AlerterSupport listeners = new AlerterSupport();

    public PeriodicNodeStatusDataStore() {
        nodeStatusReporter = newNodeStatusReporter();
    }

    @Created
    public void run() {
        reload();
    }

    protected NodeStatusReporter newNodeStatusReporter() {
        return new NodeStatusReporter();
    }

    @Destroying
    public void shutdown() {
        final BatchFuture task = scheduledTask.get();
        if (task != null) {
            task.done();
            scheduledTask.set(null);
        }
        status.set(null);
    }

    @Override
    public synchronized void reset() {
        shutdown();
        reload();
    }

    public void addAlerter(final AlertListener listener) {
        listeners.addAlerter(listener);
    }

    public void removeAlerter(final AlertListener listener) {
        listeners.removeAlerter(listener);
    }

    private void reload() {
        final String name = getClass().getSimpleName().toLowerCase(Locale.ENGLISH).replace("nodestatusdatastore", "");
        final long period = getPeriod(name);
        if (period < 0) {
            return;
        }

        final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory(name + "-status-schedule-"));
        final ScheduledFuture<?> future = ses.scheduleAtFixedRate(new ReportStatusTask(), period, period, TimeUnit.MILLISECONDS);
        scheduledTask.set(new BatchFuture(ses, future));
    }

    protected int getPeriod(final String name) {
        return Configuration.getInteger(Configuration.CONFIG_PROPERTY_PREFIX + name + ".status.period",
            Configuration.getInteger(Configuration.CONFIG_PROPERTY_PREFIX + name + ".period", 60000));
    }

    protected void reportStatus(final NodeStatus nodeStatus) {
        // no-op
    }

    @Override
    public Map<String, NodeStatus> statuses() {
        if (status.get() != null) {
            statusAsMap.put("local", status.get());
        } else {
            statusAsMap.clear();
        }
        return statusAsMap;
    }

    protected void periodicTask() {
        final NodeStatus nodeStatus = nodeStatusReporter.computeStatus();
        try {
            status.set(nodeStatus);
            reportStatus(nodeStatus);
            listeners.notify(singletonMap("local", nodeStatus));
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private class ReportStatusTask implements Runnable {
        @Override
        public void run() {
            PeriodicNodeStatusDataStore.this.periodicTask();
        }
    }
}
