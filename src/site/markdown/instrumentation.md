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
# Manually (clients)

To handle manually the interception you need to import sirona-aop.
Then you can rely on `com.github.rmannibucau.sirona.aop.SironaProxyFactory`.

`org.apache.commons.proxy.ProxyFactory` key defines the proxy factory to use to create proxies For instance
to use javassist you set it to `org.apache.commons.proxy.factory.javassist.JavassistProxyFactory`
(and you'll include javassist in your application).

Then the API is quite simple:

<pre class="prettyprint linenums"><![CDATA[
final MyClient client = SironaProxyFactory.monitor(MyClient.class, getMyClientInstance());
]]></pre>

# CDI

You just need to decorate your CDI bean/method with the interceptor binding `com.github.rmannibucau.sirona.cdi.Monitored`.

For instance:

<pre class="prettyprint linenums"><![CDATA[
@Monitored
@ApplicationScoped
public class MyMonitoredBean {
    public void myMethod() {
        // ...
    }
}
]]></pre>

Note: in some (old) CDI implementation you'll need to activate the monitoring interceptor: `com.github.rmannibucau.sirona.cdi.SironaInterceptor`.

You can configure it (without adding the `@Monitored` annotation) using `com.github.rmannibucau.sirona.cdi.performance` key. The
value is a list of predicate (`regex:<regex>`, `prefix:<prefix>`, `suffix:<suffix>`).

For instance:

<pre class="prettyprint linenums"><![CDATA[
com.github.rmannibucau.sirona.cdi.performance = prefix:org.superbiz.MyService,regex:.*Bean
]]></pre>

# Spring

Using `com.github.rmannibucau.sirona.spring.BeanNameMonitoringAutoProxyCreator` you can automatically
add monitoring to selected beans.

<pre class="prettyprint linenums"><![CDATA[
<bean class="com.github.rmannibucau.sirona.spring.BeanNameMonitoringAutoProxyCreator">
  <property name="beanNames">
    <list>
      <value>*Service</value>
    </list>
  </property>
</bean>
]]></pre>

An alternative is to use `com.github.rmannibucau.sirona.spring.PointcutMonitoringAutoProxyCreator` which uses
a `org.springframework.aop.Pointcut` to select beans to monitor.

# AspectJ

To use AspectJ weaver (it works with build time enhancement too but it is often less relevant) just configure a custom
concrete aspect defining the pointcut to monitor:

<pre class="prettyprint linenums"><![CDATA[
<aspectj>
  <aspects>
    <concrete-aspect name="org.apache.commons.aspectj.MyMonitoringAspectJ"
                     extends="com.github.rmannibucau.sirona.aspectj.SironaAspect">
      <pointcut name="pointcut" expression="execution(* com.github.rmannibucau.sirona.aspectj.AspectJMonitoringTest$MonitorMe.*(..))"/>
    </concrete-aspect>
  </aspects>

  <weaver>
    <include within="com.github.rmannibucau.sirona.aspectj.AspectJMonitoringTest$MonitorMe"/>
  </weaver>
</aspectj>
]]></pre>

See [AspectJ documentation](http://eclipse.org/aspectj/doc/next/progguide/language-joinPoints.html) for more information.

# Note on interceptor configuration (experimental)

Few global configuration (`sirona.properties`) is available for all interceptors:

* `com.github.rmannibucau.sirona.performance.adaptive`: if this boolean is set to true the following parameters are taken into account
* `com.github.rmannibucau.sirona.performance.threshold`: if > 0 it is the duration under which calls are skipped (no more monitored). Note: the format supports [duration] [TimeUnit name] too. For instance `100 MILLISECONDS` is valid.
* `com.github.rmannibucau.sirona.performance.forced-iteration`: the number of iterations a deactivated interceptor (because of threshold rule) will wait before forcing a measure to see if the monitoring should be activated back.

Note: `threshold` and `forced-iteration` parameters can be specialized appending to `com.github.rmannibucau.sirona.` the method qualified name.

Here a sample of the behavior associated with these properties. Let say you configured `forced-iteration` to 5 and
 `threshold` to 100 milliseconds. If `xxx ms` represent an invocation of xxx milliseconds and `*` represent a call
 which was measured, here is an invocation sequence:

<pre class="prettyprint linenums"><![CDATA[
500 ms*
5 ms*
500 ms
500 ms
500 ms
500 ms
500 ms
20 ms*
200 ms
200 ms
200 ms
200 ms
200 ms
500 ms*
500 ms*
]]></pre>

Note: the idea is to reduce the overhead of the interception. This is pretty efficient in general but particularly with AspectJ.
Note 2: if your invocations are pretty unstable this is not really usable since since you'll not get a good threshold value.

# JavaAgent

## Usage

First add to your JVM the sirona javaagent:

```
-javaagent:/path/to/sirona-javaagent.jar
```

Note: ensuring sirona-core and sirona-aop are no more delivered in your binaries or the container is not mandatory but better.

## Configuration

The javaagent supports "include" configuration:

```
-javaagent:/path/to/sirona-javaagent.jar=includes=XXX
```

The value of `includes` (XXX in previous example) is a comma separated list of predicates (`prefix:org.superbix`, `regex:org.superbiz.*Service`).

Symmetrically `excludes` is supported.

Adding others jars with libs=paths to a directory containing jars

## What does the javaagent

It basically convert the following code:

<pre class="prettyprint linenums"><![CDATA[
public class Foo {
    public void run() {
    }

    public void run2() {
        System.out.println("hello");
    }

    public String out() {
        return "output";
    }

    public void npe() {
        try {
            throw new NullPointerException();
        } catch (final NullPointerException npe) {
            npe.printStackTrace();
        }
    }
}
]]></pre>

in

<pre class="prettyprint linenums"><![CDATA[
package org.apache.test.sirona.javaagent;

import com.github.rmannibucau.sirona.aop.AbstractPerformanceInterceptor;
import com.github.rmannibucau.sirona.counters.Counter;
import com.github.rmannibucau.sirona.javaagent.AgentPerformanceInterceptor;

public class Foo {
    private static final String out_$_$IRONA_$_INTERNAL_$_KEY;
    private static final String npe_$_$IRONA_$_INTERNAL_$_KEY;
    private static final String run_$_$IRONA_$_INTERNAL_$_KEY;
    private static final String run2_$_$IRONA_$_INTERNAL_$_KEY;

    public void run() {
        AbstractPerformanceInterceptor.Context localContext = AgentPerformanceInterceptor.start(run_$_$IRONA_$_INTERNAL_$_KEY, this);
        try {
            run_$_$irona_$_internal_$_original_$_();
            localContext.stop();
        } catch (Throwable localThrowable) {
            localContext.stopWithException(localThrowable);
            throw localThrowable;
        }
    }

    private void run_$_$irona_$_internal_$_original_$_() {
    }

    public void run2() {
        AbstractPerformanceInterceptor.Context localContext = AgentPerformanceInterceptor.start(run2_$_$IRONA_$_INTERNAL_$_KEY, this);
        try {
            run2_$_$irona_$_internal_$_original_$_();
            localContext.stop();
        } catch (Throwable localThrowable) {
            localContext.stopWithException(localThrowable);
            throw localThrowable;
        }
    }

    private void run2_$_$irona_$_internal_$_original_$_() {
        System.out.println("hello");
    }

    public String out() {
        AbstractPerformanceInterceptor.Context localContext = AgentPerformanceInterceptor.start(out_$_$IRONA_$_INTERNAL_$_KEY, this);
        try {
            String str = out_$_$irona_$_internal_$_original_$_();
            localContext.stop();
            return str;
        } catch (Throwable localThrowable) {
            localContext.stopWithException(localThrowable);
            throw localThrowable;
        }
    }

    private String out_$_$irona_$_internal_$_original_$_() {
        return "output";
    }

    public void npe() {
        AbstractPerformanceInterceptor.Context localContext = AgentPerformanceInterceptor.start(npe_$_$IRONA_$_INTERNAL_$_KEY, this);
        try {
            npe_$_$irona_$_internal_$_original_$_();
            localContext.stop();
        } catch (Throwable localThrowable) {
            localContext.stopWithException(localThrowable);
            throw localThrowable;
        }
    }

    private void npe_$_$irona_$_internal_$_original_$_() {
        try {
            throw new NullPointerException();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    private static void _$_$irona_static_merge0() {
        out_$_$IRONA_$_INTERNAL_$_KEY = "org.apache.test.sirona.javaagent.Foo.out";
        npe_$_$IRONA_$_INTERNAL_$_KEY = "org.apache.test.sirona.javaagent.Foo.npe";
        run_$_$IRONA_$_INTERNAL_$_KEY = "org.apache.test.sirona.javaagent.Foo.run";
        run2_$_$IRONA_$_INTERNAL_$_KEY = "org.apache.test.sirona.javaagent.Foo.run2";
    }

    static {
        _$_$irona_static_merge0();
        // if other static blocks it will be added here as _$_$irona_static_merge1(), _$_$irona_static_merge2()...
    }
}
]]></pre>

## Go further with sirona javaagent

Javaagent is extensible using `InvocationListener` API:

<pre class="prettyprint linenums"><![CDATA[
public interface InvocationListener {
    void before(AgentContext context);
    void after(AgentContext context, Object result, Throwable error);
    boolean accept(String key);
}
]]></pre>

* `accept` method allows to filter by key/instance what is intercepted.
* `before` and `after` allows to put code around method executions. `AgentContext` supports contextual data through `put`/`get` methods.

Note on contextual data: key is an int for performances reasons, ensure you don't conflict with another listener. Prefer to use negative int if possible.

`InvocationListeners` can be sorted using `@Order` annotation.

`com.github.rmannibucau.sirona.javaagent.listener.ConfigurableListener` is an utility class to simplify the writing of `InvocationListener` classes.

They are picked using a plain old service provider interface (`META-INF/services/com.github.rmannibucau.sirona.javaagent.spi.InvocationListener`).
