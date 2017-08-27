<!---
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
# WebSocket

WebSocket module uses websocket to send data from an instance to a collector.

## Configuration

### Agent/Client

You need `sirona-websocket-client` dependency and to add the following configuration
in your `sirona.properties`:

    com.github.rmannibucau.sirona.store.DataStoreFactory = com.github.rmannibucau.sirona.websocket.client.WebSocketDataStoreFactory
    com.github.rmannibucau.sirona.websocket.client.WebSocketClientBuilder.marker = test
    com.github.rmannibucau.sirona.websocket.client.WebSocketClientBuilder.uri = ws://localhost:1235/test-websocket/
    # optional
    com.github.rmannibucau.sirona.websocket.client.WebSocketClientBuilder.retries = 2
    com.github.rmannibucau.sirona.websocket.client.WebSocketClientBuilder.authorization = BASIC xxxxx

If you want to specify websocket for a subset of data store use the following classes:

* `com.github.rmannibucau.sirona.websocket.client.WebSocketCounterDataStore` for counters
* `com.github.rmannibucau.sirona.websocket.client.WebSocketGaugeDataStore` for gauges
* `com.github.rmannibucau.sirona.websocket.client.WebSocketNodeStatusDataStore` for validation/status

## Server

You need to add `sirona-websocket-server` dependency and configure the collector
in websocket mode (no need of HTTP collector if you don't use it):

    com.github.rmannibucau.sirona.store.status.NodeStatusDataStore = com.github.rmannibucau.sirona.store.status.InMemoryCollectorNodeStatusDataStore
    com.github.rmannibucau.sirona.store.counter.CollectorCounterStore = com.github.rmannibucau.sirona.store.memory.counter.InMemoryCollectorCounterStore
    com.github.rmannibucau.sirona.store.gauge.CollectorGaugeDataStore = com.github.rmannibucau.sirona.store.gauge.DelegatedCollectorGaugeDataStore
    # not yet implemented with websockets so if you use it use http push for path tracking or another one
    com.github.rmannibucau.sirona.store.tracking.PathTrackingDataStore = com.github.rmannibucau.sirona.store.memory.tracking.InMemoryPathTrackingDataStore

### Send Period 

You can also configure the period used to flush periodicly data:

* `com.github.rmannibucau.sirona.websocket.period`: which period to use to push counters data to the server (default to 1mn).

## Limitations (ATM)

You cannot yet use WebSocket for path tracking.

