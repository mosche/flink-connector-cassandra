/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.connector.cassandra.source.reader;

import org.apache.flink.api.connector.source.SourceReader;
import org.apache.flink.api.connector.source.SourceReaderContext;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.base.source.reader.SingleThreadMultiplexSourceReaderBase;
import org.apache.flink.connector.cassandra.source.split.CassandraSplit;
import org.apache.flink.streaming.connectors.cassandra.ClusterBuilder;
import org.apache.flink.streaming.connectors.cassandra.MapperOptions;

import java.util.Map;

/**
 * Cassandra {@link SourceReader} that reads one {@link CassandraSplit} using a single thread.
 *
 * @param <OUT> the type of elements produced by the source
 */
public class CassandraSourceReader<OUT>
        extends SingleThreadMultiplexSourceReaderBase<
                CassandraRow,
                OUT,
                CassandraSplit,
                CassandraSplit> { // no need for a mutable CassandraSplit type as the CassandraSplit
    // is atomically processed. The splits processing advancement is tracked by
    // CassandraSplitReader#unprocessedSplits

    public CassandraSourceReader(
            SourceReaderContext context,
            ClusterBuilder clusterBuilder,
            Class<OUT> pojoClass,
            String query,
            MapperOptions mapperOptions) {
        super(
                () -> new CassandraSplitReader(clusterBuilder, query),
                new CassandraRecordEmitter<>(pojoClass, clusterBuilder, mapperOptions),
                new Configuration(),
                context);
    }

    @Override
    protected void onSplitFinished(Map<String, CassandraSplit> finishedSplitIds) {
        // nothing to do
    }

    @Override
    protected CassandraSplit initializedState(CassandraSplit cassandraSplit) {
        return cassandraSplit;
    }

    @Override
    protected CassandraSplit toSplitType(String splitId, CassandraSplit cassandraSplit) {
        return cassandraSplit;
    }
}
