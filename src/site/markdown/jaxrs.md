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
# Goal

JAX-RS module is comparable to Web performance monitoring
excepted it uses JAX-RS mapping instead of raw request url.

For instance you'll get as counter name `/user/{name}` instead of `/user/apache`.

# Installation

JAXRS integration comes with two main flavors: jaxrs2 and cxf.

The first one targets recent implementations and 100% relies on the specification. The second one
is specific to cxf (it targets mainly cxf 2.6.x but should work with more recent releases). This
is mainly for JAX-RS 1.0 servers.

## JAX-RS 2

Just add the `com.github.rmannibucau.sirona.agent.jaxrs.jaxrs2.SironaFeature` to your configuration.

## CXF 2.6

Just add the provider `com.github.rmannibucau.sirona.agent.jaxrs.cxf.CxfJaxRsPerformanceHandler`.
