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
package com.github.rmannibucau.test.sirona.javaagent;

import com.github.rmannibucau.sirona.Role;
import com.github.rmannibucau.sirona.counters.Counter;
import com.github.rmannibucau.sirona.javaagent.AgentArgs;
import com.github.rmannibucau.sirona.javaagent.JavaAgentRunner;
import com.github.rmannibucau.sirona.javaagent.listener.CounterListener;
import com.github.rmannibucau.sirona.repositories.Repository;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(JavaAgentRunner.class)
public class SimpleTest {
    @Test
    public void ensureStaticBlocksAreKept() throws Exception {
        assertTrue(ServiceTransform.staticInit);
    }

    @Test
    public void staticMonitoring() {
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$StaticTransform.monitorMe()", 0);
        assertEquals(1, StaticTransform.init);
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$StaticTransform.monitorMe()", 1);
    }

    @Test
    public void staticMethod() {
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$ServiceTransform.staticMethod()", 0);
        ServiceTransform.staticMethod();
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$ServiceTransform.staticMethod()", 1);
    }

    @Test
    public void noReturnWithoutStatic() {
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$ServiceNoStaticTransform.noReturn()", 0);
        new ServiceNoStaticTransform().noReturn();
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$ServiceNoStaticTransform.noReturn()", 1);
    }

    @Test
    public void noReturn() {
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$ServiceTransform.noReturn()", 0);
        new ServiceTransform().noReturn();
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$ServiceTransform.noReturn()", 1);
    }

    @Test
    public void withReturn() {
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$ServiceTransform.withReturn()", 0);
        new ServiceTransform().withReturn();
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$ServiceTransform.withReturn()", 1);
    }

    @Test
    public void nested() {
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$ServiceTransform.nest()", 0);
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$Service2Transform.nested()", 0);
        new ServiceTransform().nest();
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$ServiceTransform.nest()", 1);
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$Service2Transform.nested()", 1);
    }

    @Test
    public void exception() {
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$ServiceTransform.exception()", 0);
        try {
            new ServiceTransform().exception();
            fail();
        } catch (final IllegalArgumentException iae) {
            // OK
        }
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$ServiceTransform.exception()", 1);
        assertException(1, IllegalArgumentException.class);
    }

    @Test
    public void alreadyTryCatch() {
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$ServiceTransform.alreadyTryCatch()", 0);
        new ServiceTransform().alreadyTryCatch();
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$ServiceTransform.alreadyTryCatch()", 1);
        assertException(0, NullPointerException.class);
    }

    @Test
    public void alreadyTryCatchWithReturn() {
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$ServiceTransform.alreadyTryCatchWithReturnPrimitive()", 0);
        assertTrue(new ServiceTransform().alreadyTryCatchWithReturnPrimitive());
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$ServiceTransform.alreadyTryCatchWithReturnPrimitive()", 1);
    }

    @Test
    public void primitive() {
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$ServiceTransform.soPrimitive()", 0);
        ServiceTransform.soPrimitive();
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$ServiceTransform.soPrimitive()", 1);
    }

    @Test
    public void annotations() throws NoSuchMethodException {
        assertTrue(ServiceWithAnnotationTransform.class.getMethod("mtd").getAnnotation(AgentArgs.class) != null);
    }

    @Test
    @AgentArgs(CounterListener.DISABLE_PARAMETER_KEY)
    public void primitiveDisable() {
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$ServiceTransform.soPrimitive()", 0);
        ServiceTransform.soPrimitive();
        assertHits("com.github.rmannibucau.test.sirona.javaagent.SimpleTest$ServiceTransform.soPrimitive()", 0);
    }

    private static void assertException(final int count, final Class<?> exception) {
        int iae = 0;
        for (final Counter c : Repository.INSTANCE.counters()) {
            if (c.getKey().getName().contains(exception.getName())) {
                iae++;
            }
        }
        assertEquals(count, iae);
    }

    private static void assertHits(final String name, final int expected) {
        assertEquals(expected, Repository.INSTANCE.getCounter(new Counter.Key(Role.PERFORMANCES, name)).getHits());
    }

    public static class ServiceTransform {
        public static boolean staticInit = false;

        static {
            staticInit = true;
        }

        public void noReturn() {
            // no-op
        }

        public static int soPrimitive() {
            return 1;
        }

        public static String staticMethod() {
            return "ok";
        }

        public String withReturn() {
            return "ok";
        }

        public String nest() {
            return new Service2Transform().nested();
        }

        public void exception() {
            throw new IllegalArgumentException();
        }

        public void alreadyTryCatch() {
            try {
                throw new NullPointerException();
            } catch (final NullPointerException iae) {
                // no-op
            }
        }

        public boolean alreadyTryCatchWithReturnPrimitive() {
            try {
                return true;
            } catch (final NullPointerException iae) {
                return false;
            }
        }
    }

    public static class ServiceNoStaticTransform {
        public void noReturn() {
            // no-op
        }
    }

    public static class Service2Transform {
        public String nested() {
            return null;
        }
    }

    public static class ServiceWithAnnotationTransform {
        @AgentArgs("")
        public void mtd() {
            // no-op
        }
    }

    public static class StaticTransform {
        private static int init = 0;

        static {
            init = monitorMe();
        }

        public static int monitorMe() {
            return 1;
        }
    }
}
