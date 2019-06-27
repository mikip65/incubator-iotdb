/**
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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.iotdb.db.engine.memtable;

import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;
import org.apache.iotdb.tsfile.utils.Binary;

public interface IWritableMemChunk extends TimeValuePairSorter {

  void putLong(long t, long v);

  void putInt(long t, int v);

  void putFloat(long t, float v);

  void putDouble(long t, double v);

  void putBinary(long t, Binary v);

  void putBoolean(long t, boolean v);

  void write(long insertTime, String insertValue);

  void write(long insertTime, Object insertValue);

//  void reset();

  long count();

  TSDataType getType();

  void setTimeOffset(long offset);

  void releasePrimitiveArrayList();

  DeduplicatedSortedData getDeduplicatedSortedData();
}
