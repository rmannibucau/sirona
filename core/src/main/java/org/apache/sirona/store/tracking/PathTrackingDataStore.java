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
package org.apache.sirona.store.tracking;

import org.apache.sirona.tracking.PathTrackingEntry;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 *
 */
public interface PathTrackingDataStore
{
    void store( PathTrackingEntry pathTrackingEntry );

    /**
     * the result will be orderer by startTime
     *
     * @param trackingId
     * @return {@link List} of {@link org.apache.sirona.tracking.PathTrackingEntry} related to a tracking id
     */
    Collection<PathTrackingEntry> retrieve( String trackingId );

    /**
     * @param startTime
     * @param endTime
     * @return {@link java.lang.String} of all trackingIds available in the system between startTime and endTime
     */
    Collection<String> retrieveTrackingIds( Date startTime, Date endTime );



}