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

package org.apache.commons.monitoring.repositories;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StopWatch;
import org.apache.commons.monitoring.Monitor.Key;

/**
 * Implementation of the Repository that can be used to configure custom
 * {@link StopWatch} or {@link Monitor} implementation.
 * <p>
 * Uses reflection to invoke constructor on the specified implementation classes.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class ConfigurableImplementationsRepository
    extends ObservableRepository
{
    private Constructor<? extends StopWatch> stopWatchConstructor;

    private Constructor<? extends Monitor> monitorConstructor;

    /**
     * Constructor
     * @param stopWatchImplementation the StopWatch implementation to use
     * @param monitorImplementation the Monitor implementation to use
     */
    public ConfigurableImplementationsRepository( Class<? extends StopWatch> stopWatchImplementation,
                                                  Class<? extends Monitor> monitorImplementation )
    {
        super();
        setStopWatchImplementation( stopWatchImplementation );
        setMonitorImplementation( monitorImplementation );
    }

    protected void setStopWatchImplementation( Class<? extends StopWatch> implementation )
    {
        try
        {
            stopWatchConstructor = implementation.getConstructor( Monitor.class );
        }
        catch ( Exception e )
        {
            throw new IllegalStateException( "Invalid StopWatch implemenation. Constructor <init>(Monitor) required" );
        }
    }

    protected void setMonitorImplementation( Class<? extends Monitor> implementation )
    {
        try
        {
            monitorConstructor = implementation.getConstructor( Monitor.Key.class );
        }
        catch ( Exception e )
        {
            throw new IllegalStateException(
                "Invalid StopWatch implemenation. Constructor <init>(Monitor.Key) required" );
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.repositories.AbstractRepository#newMonitorInstance(org.apache.commons.monitoring.Monitor.Key)
     */
    @Override
    protected Monitor newMonitorInstance( Key key )
    {
        try
        {
            return monitorConstructor.newInstance( key );
        }
        catch ( InvocationTargetException e )
        {
            throw new IllegalStateException( "Failed to invoke configured stopWatchConstructor ", e.getTargetException() );
        }
        catch ( Exception e )
        {
            throw new IllegalStateException( "Invalid stopWatchConstructor configured in repository "
                + stopWatchConstructor );
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.Repository#start(org.apache.commons.monitoring.Monitor)
     */
    public StopWatch start( Monitor monitor )
    {
        try
        {
            return stopWatchConstructor.newInstance( monitor );
        }
        catch ( InvocationTargetException e )
        {
            throw new IllegalStateException( "Failed to invoke configured stopWatchConstructor ", e.getTargetException() );
        }
        catch ( Exception e )
        {
            throw new IllegalStateException( "Invalid stopWatchConstructor configured in repository "
                + stopWatchConstructor, e );
        }
    }
}