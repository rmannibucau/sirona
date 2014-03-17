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

package org.apache.sirona.cassandra.collector.pathtracking;

import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.exceptions.HInvalidRequestException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.QueryResult;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.sirona.cassandra.DynamicDelegatedSerializer;
import org.apache.sirona.cassandra.collector.CassandraSirona;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.store.tracking.BatchPathTrackingDataStore;
import org.apache.sirona.store.tracking.CollectorPathTrackingDataStore;
import org.apache.sirona.store.tracking.PathTrackingDataStore;
import org.apache.sirona.tracking.PathTrackingEntry;
import org.apache.sirona.tracking.PathTrackingEntryComparator;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.apache.sirona.cassandra.collector.CassandraSirona.*;

/**
 *
 */
public class CassandraPathTrackingDataStore
    extends BatchPathTrackingDataStore
    implements PathTrackingDataStore, CollectorPathTrackingDataStore
{

    private final CassandraSirona cassandra;

    private final Keyspace keyspace;

    private final String family;

    private final String markerFamilly;

    private static final Comparator<PathTrackingEntry> START_TIME_COMPARATOR = new Comparator<PathTrackingEntry>()
    {
        @Override
        public int compare( PathTrackingEntry pathTrackingEntry, PathTrackingEntry pathTrackingEntry2 )
        {
            return new Date( pathTrackingEntry.getStartTime() ) //
                .compareTo( new Date( pathTrackingEntry2.getStartTime() ) );
        }
    };


    public CassandraPathTrackingDataStore()
    {
        this.cassandra = IoCs.findOrCreateInstance( CassandraSirona.class );
        this.keyspace = cassandra.getKeyspace();
        this.family = cassandra.getPathTrackingColumFamily();
        this.markerFamilly = cassandra.getMarkerPathTrackingColumFamily();
    }

    @Override
    public void store( PathTrackingEntry pathTrackingEntry )
    {
        store( Collections.singletonList( pathTrackingEntry ) );
    }

    @Override
    public void store( Collection<PathTrackingEntry> pathTrackingEntries )
    {

        // FIXME find a more efficient way to store such batch of datas

        for ( PathTrackingEntry pathTrackingEntry : pathTrackingEntries )

        {
            final String id = id( pathTrackingEntry );

            try
            {
                HFactory.createMutator( keyspace, StringSerializer.get() )
                    //  values
                    .addInsertion( id, family, column( "trackingId", pathTrackingEntry.getTrackingId() ) ) //
                    .addInsertion( id, family, column( "nodeId", pathTrackingEntry.getNodeId() ) ) //
                    .addInsertion( id, family, column( "className", pathTrackingEntry.getClassName() ) ) //
                    .addInsertion( id, family, column( "methodName", pathTrackingEntry.getMethodName() ) ) //
                    .addInsertion( id, family, column( "startTime", pathTrackingEntry.getStartTime() ) ) //
                    .addInsertion( id, family, column( "executionTime", pathTrackingEntry.getExecutionTime() ) ) //
                    // we force level as long to be able to do slice queries include filtering on level, startTime
                    .addInsertion( id, family, column( "level", Long.valueOf( pathTrackingEntry.getLevel() ) ) ) //
                    .addInsertion( "PATH_TRACKING", markerFamilly, emptyColumn( id ) ) //
                    .execute();
            }
            catch ( HInvalidRequestException e )
            {
                // ignore but log it
                e.printStackTrace();
            }
        }
    }

    protected String id( PathTrackingEntry pathTrackingEntry )
    {
        return cassandra.generateKey( pathTrackingEntry.getTrackingId(), //
                                      pathTrackingEntry.getClassName(),//
                                      pathTrackingEntry.getMethodName(), //
                                      Long.toString( pathTrackingEntry.getStartTime() ),//
                                      pathTrackingEntry.getNodeId(), //
                                      Integer.toString( pathTrackingEntry.getLevel() ) );
    }

    @Override
    public Collection<PathTrackingEntry> retrieve( String trackingId )
    {
        final QueryResult<OrderedRows<String, String, String>> cResult = //
            HFactory.createRangeSlicesQuery( keyspace, //
                                             StringSerializer.get(), //
                                             StringSerializer.get(), //
                                             StringSerializer.get() ) //
                .setColumnNames( "trackingId", "nodeId", "className", "methodName", "startTime", "executionTime",
                                 "level" ) //
                .addEqualsExpression( "trackingId", trackingId ) //
                .setColumnFamily( family ) //
                .execute();

        Set<PathTrackingEntry> entries = new TreeSet<PathTrackingEntry>( START_TIME_COMPARATOR );

        OrderedRows<String, String, String> rows = cResult.get();

        if ( rows == null )
        {
            return entries;
        }

        for ( Row<String, String, String> row : rows.getList() )
        {
            ColumnSlice<String, String> columnSlice = row.getColumnSlice();

            PathTrackingEntry pathTrackingEntry = map( columnSlice );

            entries.add( pathTrackingEntry );
        }

        return entries;
    }

    @Override
    public Collection<String> retrieveTrackingIds( Date startTime, Date endTime )
    {

        final QueryResult<OrderedRows<String, String, Long>> cResult = //
            HFactory.createRangeSlicesQuery( keyspace, //
                                             StringSerializer.get(), //
                                             StringSerializer.get(), //
                                             LongSerializer.get() ) //
                .setColumnNames( "trackingId", "nodeId", "className", "methodName", "startTime", "executionTime",
                                 "level" ) //
                .addEqualsExpression( "level", Long.valueOf( 1 ) ) //
                .addGteExpression( "startTime", startTime.getTime() ).setColumnFamily( family ).execute();

        int size = cResult.get().getList().size();

        Set<String> ids = new HashSet<String>( size );

        OrderedRows<String, String, Long> rows = cResult.get();

        if ( rows == null )
        {
            return ids;
        }

        for ( Row<String, String, Long> row : rows.getList() )
        {
            ColumnSlice<String, Long> columnSlice = row.getColumnSlice();

            PathTrackingEntry pathTrackingEntry = map( columnSlice );

            ids.add( pathTrackingEntry.getTrackingId() );
        }

        return ids;
    }


    /**
     * <b>use with CAUTION as can return a lot of data</b>
     * <p>This method is use for testing purpose</p>
     *
     * @return {@link java.util.List} containing all {@link PathTrackingEntry}
     */
    public Map<String, Set<PathTrackingEntry>> retrieveAll()
    {
        final QueryResult<OrderedRows<String, String, String>> cResult = //
            HFactory.createRangeSlicesQuery( keyspace, //
                                             StringSerializer.get(), //
                                             StringSerializer.get(), //
                                             StringSerializer.get() ) //
                .setColumnFamily( family ) //
                .setRange( null, null, false, Integer.MAX_VALUE ) //
                .execute();

        Map<String, Set<PathTrackingEntry>> entries = new TreeMap<String, Set<PathTrackingEntry>>();

        for ( Row<String, String, String> row : cResult.get().getList() )
        {
            ColumnSlice<String, String> columnSlice = row.getColumnSlice();

            PathTrackingEntry pathTrackingEntry = map( columnSlice );

            String trackingId = pathTrackingEntry.getTrackingId();

            Set<PathTrackingEntry> pathTrackingEntries = entries.get( trackingId );
            if ( pathTrackingEntries == null )
            {
                pathTrackingEntries = new TreeSet<PathTrackingEntry>( PathTrackingEntryComparator.INSTANCE );
            }
            pathTrackingEntries.add( pathTrackingEntry );

            entries.put( trackingId, pathTrackingEntries );

        }

        return entries;
    }

    private PathTrackingEntry map( ColumnSlice<String, ?> columnSlice )
    {
        final DynamicDelegatedSerializer<Object> serializer = new DynamicDelegatedSerializer<Object>();

        String trackingId =
            StringSerializer.get().fromByteBuffer( columnSlice.getColumnByName( "trackingId" ).getValueBytes() );

        String nodeId =
            StringSerializer.get().fromByteBuffer( columnSlice.getColumnByName( "nodeId" ).getValueBytes() );
        String className =
            StringSerializer.get().fromByteBuffer( columnSlice.getColumnByName( "className" ).getValueBytes() );
        String methodName =
            StringSerializer.get().fromByteBuffer( columnSlice.getColumnByName( "methodName" ).getValueBytes() );

        long startTime = getOrDefault( serializer, //
                                       columnSlice.getColumnByName( "startTime" ), //
                                       LongSerializer.get() ).longValue();

        long executionTime = getOrDefault( serializer, //
                                           columnSlice.getColumnByName( "executionTime" ), //
                                           LongSerializer.get() ).longValue();

        int level = getOrDefault( serializer, //
                                  columnSlice.getColumnByName( "level" ), //
                                  LongSerializer.get() ).intValue();

        return new PathTrackingEntry( trackingId, //
                                      nodeId, //
                                      className, //
                                      methodName, //
                                      startTime, //
                                      executionTime, //
                                      level );


    }


    protected void pushEntriesByBatch( Map<String, List<Pointer>> pathTrackingEntries )
    {
        // TODO even if not really used
        /*
        List<PathTrackingEntry> entries = new ArrayList<PathTrackingEntry>(  );

        for ( Map.Entry<String, Set<PathTrackingEntry>> entry : pathTrackingEntries.entrySet()) {
            entries.addAll( entry.getValue() );
        }


        store( entries );
        */
    }

    protected Keyspace getKeyspace()
    {
        return keyspace;
    }

    protected String getFamily()
    {
        return family;
    }
}
