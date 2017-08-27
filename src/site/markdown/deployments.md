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
# Solutions

Sirona supports several deployments. Basically you can:

* deploy everything locally (agent, reporting)
* deploy agent in "client" JVMs and a remote collector ("server")
* (not yet available - needs a custom persistent store) deploy client JVMs and twi servers: one for the collection and one for the reporting (GUI)
* in agent/collector mode you can either use agent push mecanism or collector pulling

## Everything locally

TBD

<pre class="prettyprint linenums"><![CDATA[
<dependency>
  <groupId>com.github.rmannibucau.sirona</groupId>
  <artifactId>sirona-core</artifactId>
  <version>${sirona.version}</version>
</dependency>
<dependency>
  <groupId>com.github.rmannibucau.sirona</groupId>
  <artifactId>sirona-jdbc</artifactId>
  <version>${sirona.version}</version>
</dependency>
<dependency>
  <groupId>com.github.rmannibucau.sirona</groupId>
  <artifactId>sirona-jpa</artifactId>
  <version>${sirona.version}</version>
</dependency>
<dependency>
  <groupId>com.github.rmannibucau.sirona</groupId>
  <artifactId>sirona-cdi</artifactId>
  <version>${sirona.version}</version>
</dependency>
<dependency>
  <groupId>com.github.rmannibucau.sirona</groupId>
  <artifactId>sirona-jta</artifactId>
  <version>${sirona.version}</version>
</dependency>
<dependency>
  <groupId>com.github.rmannibucau.sirona</groupId>
  <artifactId>sirona-web</artifactId>
  <version>${sirona.version}</version>
</dependency>
<dependency>
  <groupId>com.github.rmannibucau.sirona</groupId>
  <artifactId>sirona-cube</artifactId>
  <version>${sirona.version}</version>
</dependency>
<dependency>
  <groupId>com.github.rmannibucau.sirona</groupId>
  <artifactId>sirona-reporting</artifactId>
  <version>${sirona.version}</version>
  <classifier>classes</classifier>
</dependency>
...
]]></pre>


## Agent/Collector

This part doesn't deal with collector/reporting part, see cassandra doc page for more details on
how to split reporting webapp and collector webapp using cassandra persistence if you need it.

### Push mode

Simply use on agent/client side the cube datastore factory: `com.github.rmannibucau.sirona.cube.CubeDataStoreFactory`.

### Pull mode

First add the pull module and configure the pull datastore factory: `com.github.rmannibucau.sirona.agent.webapp.pull.store.PullDataStoreFactory`.

Note: this mode needs the servlet `com.github.rmannibucau.sirona.agent.webapp.pull.servlet.PullServlet` to be deployed. On
servlet 3.x servers it should be done automatically on `/sirona/pull`.

#### Registration of agents on collectors
##### Automatic registration

In this mode the agent does a request on the collector to register itself. This needs the servlet to be deployed
manually (through web.xml) with the init parameter `com.github.rmannibucau.sirona.pull.url` initialized to the collector url. It
internally relies on `com.github.rmannibucau.sirona.cube.CubeBuilder` config (see cube config).


##### Collector agent aware

You can force the collector to know the agents statically setting either on the collector init parameter (if registered manually)
or collector sirona configuration (properties) the following property:

```
com.github.rmannibucau.sirona.collector.collection.agent-urls = http://agent1,http://agent2....
```
