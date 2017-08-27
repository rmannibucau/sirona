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
## Web module

Reporting module provides web listener/filter to monitor servlet containers.

## Installation

Add sirona-web to your webapp.

## Monitor requests

Simply add the filter `com.github.rmannibucau.sirona.web.servlet.SironaFilter`:

<pre class="prettyprint linenums"><![CDATA[
<filter>
    <filter-name>monitoring-request</filter-name>
    <filter-class>com.github.rmannibucau.sirona.web.servlet.SironaFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>monitoring-request</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
]]></pre>

Note: in a servlet 3 container you can simply configure `com.github.rmannibucau.sirona.web.monitored-urls` to the
servlet pattern you want to match. If you want to register the `SironaFilter` yourself just set the
init parameter `com.github.rmannibucau.sirona.web.activated` to false.

## Monitor sessions

Simply add the listener `com.github.rmannibucau.sirona.web.session.SironaSessionListener`:

<pre class="prettyprint linenums"><![CDATA[
<listener>
  <listener-class>com.github.rmannibucau.sirona.web.session.SironaSessionListener</listener-class>
</listener>
]]></pre>

Note: in a servlet 3 container and if `com.github.rmannibucau.sirona.web.activated` is not set to false it is added by default.

## Monitor JSP

JSP can be monitored through standard web monitoring but this includes more than only the JSP.
To be more specific you can use JSP monitoring.

For Servlet 3.0 containers just add the init parameter or sirona property:

<pre class="prettyprint linenums"><![CDATA[
com.github.rmannibucau.sirona.web.jsp.activated = true
]]></pre>

or in web.xml

<pre class="prettyprint linenums"><![CDATA[
<context-param>
    <param-name>com.github.rmannibucau.sirona.web.jsp.activated</param-name>
    <param-value>true</param-value>
</context-param>
]]></pre>

This will use `PageContext` to monitor the JSP rendering (so from the JSP servlet and not from a filter).

For Servlet 2.5 containers just declare the `Filter` `com.github.rmannibucau.sirona.web.lifecycle.LazyJspMonitoringFilterActivator`:

<pre class="prettyprint linenums"><![CDATA[
<filter>
    <filter-name>sirona-jsp-activator</filter-name>
    <filter-class>com.github.rmannibucau.sirona.web.lifecycle.LazyJspMonitoringFilterActivator</filter-class>
</filter>
<filter-mapping>
    <filter-name>sirona-jsp-activator</filter-name>
    <!--
    Any url which will use a jsp.
    If you use req.getRequestDispatcher("....jsp").forward(req, resp);
    ensure to match the original pattern (* in the worse case).
    This filter has an overhead almost null (single test).
    -->
    <url-pattern>*.jsp</url-pattern>
</filter-mapping>
]]></pre>

Note: this monitoring wraps default `javax.servlet.jsp.JspFactory` and therefore
sirona needs to be shared accross webapps using `JspFactory` (if you have a single webapp or an ear no issue).

## Accessing results

If you installed the reporting webapp you should be able to get the result under the report tab.