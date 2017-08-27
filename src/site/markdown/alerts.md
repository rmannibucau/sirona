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
# AlertListener

`AlertListener` can be registered on `NodeStatusDataStore` either programmatically using
 `addAlerter()`/`removeAlerter()` or through sirona configuration (aka sirona.properties).

## Configuration

The configuration is based on the key: `com.github.rmannibucau.sirona.alerters`. The value is a list of alerter name/alias:

<pre class="prettyprint linenums"><![CDATA[
com.github.rmannibucau.sirona.alerters = mail,webhook
]]></pre>

The class of the alerter is set using `{alerterName}.class`:

<pre class="prettyprint linenums"><![CDATA[
mail.class = com.company.sirona.alerter.MailAlerter
]]></pre>

The alerters are then configured individually using `@AutoSet` feature of sirona:

<pre class="prettyprint linenums"><![CDATA[
# just a sample of an optional config
mail.host = xxxxx
mail.password = ...
mail.protocol = smtp
]]></pre>

## MailAlerter

`sirona-alerter-mail` provides a javamail alerter.

Here is the configuration supposing your alerter is named `mail`:

<pre class="prettyprint linenums"><![CDATA[
mail.class = com.github.rmannibucau.sirona.alerter.mail.MailAlerter
mail.from = me@provider.com
mail.port = 1234
mail.host = provider.com
mail.to = target@provider.com
mail.user = xxx
mail.password = xxx
mail.timeout = 60000
mail.tls = true
mail.auth = true
mail.protocol = smtp
mail.template = ${marker} throw an alert for:\n${resultsCsv}
mail.subjectTemplate = ${marker} throw an alert at ${date}


# true to use the default javamail session with the previous configuration (Session.getDefaultInstance())
mail.useDefault = false
]]></pre>
