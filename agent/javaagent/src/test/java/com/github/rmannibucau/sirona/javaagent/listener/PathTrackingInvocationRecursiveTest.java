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
package com.github.rmannibucau.sirona.javaagent.listener;

import com.github.rmannibucau.sirona.configuration.ioc.IoCs;
import com.github.rmannibucau.sirona.javaagent.AgentArgs;
import com.github.rmannibucau.sirona.javaagent.JavaAgentRunner;
import com.github.rmannibucau.sirona.pathtracking.PathTrackingEntry;
import com.github.rmannibucau.sirona.pathtracking.test.ExtendedInMemoryPathTrackingDataStore;
import com.github.rmannibucau.sirona.store.DataStoreFactory;
import com.github.rmannibucau.test.sirona.javaagent.App;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.Set;

/**
 * this test validate we don't fall into a StackOverflow when redirecting System.out and using debug mode
 */
@RunWith(JavaAgentRunner.class)
public class PathTrackingInvocationRecursiveTest
{

    @Test
    @AgentArgs(sysProps = "project.build.directory=${project.build.directory}|sirona.agent.debug=true|com.github.rmannibucau.sirona.configuration.sirona.properties=${project.build.directory}/test-classes/pathtracking/sirona.properties")
    public void simpleTest()
        throws Exception
    {

        App app = new App().redirectStreamout();
        app.beer();

        DataStoreFactory dataStoreFactory = IoCs.findOrCreateInstance( DataStoreFactory.class );

        ExtendedInMemoryPathTrackingDataStore ptds =
            ExtendedInMemoryPathTrackingDataStore.class.cast( dataStoreFactory.getPathTrackingDataStore() );

        Map<String, Set<PathTrackingEntry>> all = ptds.retrieveAll();

        System.out.println( all );

        boolean called = MockPathTrackingInvocationListener.START_PATH_CALLED;

        Assert.assertTrue( called );

        called = MockPathTrackingInvocationListener.END_PATH_CALLED;

        Assert.assertTrue( called );

        Assert.assertTrue( !all.isEmpty() );

        Assert.assertEquals(2, all.size() );

    }

}
