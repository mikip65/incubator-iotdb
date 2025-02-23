<!--

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

# 第4章 系统部署与管理

## 系统配置

为方便IoTDB Server的配置与管理，IoTDB Server为用户提供三种配置项，使得用户可以在启动服务器或服务器运行时对其进行配置。

三种配置项的配置文件均位于IoTDB安装目录：`$IOTDB_HOME/conf`文件夹下,其中涉及server配置的共有3个文件，分别为：`iotdb-env.sh`, `tsfile-format.properties`, `iotdb-engine.properties`。用户可以通过更改其中的配置项对系统运行的相关配置项进行配置。

配置文件的说明如下：

* `iotdb-env.sh`：环境配置项的默认配置文件。用户可以在文件中配置JAVA-JVM的相关系统配置项。

* `tsfile-format.properties`：IoTDB文件层系统配置项的默认配置文件。用户可以在文件中配置IoTDB存储时TsFile文件的相关信息，如每次将内存中的数据写入到磁盘时的数据大小(`group_size_in_byte`)，内存中每个列打一次包的大小(`page_size_in_byte`)等。

* `iotdb-engine.properties`：IoTDB引擎层系统配置项的默认配置文件。用户可以在文件中配置IoTDB引擎运行时的相关参数，如JDBC服务监听端口(`rpc_port`)、overflow数据文件存储目录(`overflow_data_dir`)等。

### 环境配置项

环境配置项主要用于对IoTDB Server运行的Java环境相关参数进行配置，如JVM相关配置。IoTDB Server启动时，此部分配置会被传给JVM。用户可以通过查看 `iotdb-env.sh`(或`iotdb-env.bat`)文件查看环境配置项内容。详细配置项说明如下：

* LOCAL\_JMX

|名字|LOCAL\_JMX|
|:---:|:---|
|描述|JMX监控模式，配置为yes表示仅允许本地监控，设置为no的时候表示允许远程监控|
|类型|枚举String : “yes”, “no”|
|默认值|yes|
|改后生效方式|重启服务器生效|


* JMX\_PORT

|名字|JMX\_PORT|
|:---:|:---|
|描述|JMX监听端口。请确认该端口不是系统保留端口并且未被占用。|
|类型|Short Int: [0,65535]|
|默认值|31999|
|改后生效方式|重启服务器生效|

* MAX\_HEAP\_SIZE

|名字|MAX\_HEAP\_SIZE|
|:---:|:---|
|描述|IoTDB启动时能使用的最大堆内存大小。|
|类型|String|
|默认值|取决于操作系统和机器配置。在Linux或MacOS系统下默认为机器内存的四分之一。在Windows系统下，32位系统的默认值是512M，64位系统默认值是2G。|
|改后生效方式|重启服务器生效|

* HEAP\_NEWSIZE

|名字|HEAP\_NEWSIZE|
|:---:|:---|
|描述|IoTDB启动时能使用的最小堆内存大小。|
|类型|String|
|默认值|取决于操作系统和机器配置。在Linux或MacOS系统下默认值为机器CPU核数乘以100M的值与MAX\_HEAP\_SIZE四分之一这二者的最小值。在Windows系统下，32位系统的默认值是512M，64位系统默认值是2G。。|
|改后生效方式|重启服务器生效|

### 系统配置项

系统配置项是IoTDB Server运行的核心配置，它主要用于设置IoTDB Server文件层和引擎层的参数，便于用户根据自身需求调整Server的相关配置，以达到较好的性能表现。系统配置项可分为两大模块：文件层配置项和引擎层配置项。用户可以通过查看`tsfile-format.properties`, `iotdb-engine.properties`,文件查看和修改两种配置项的内容。在0.8.0版本中字符串类型的配置项大小写敏感。

#### 文件层配置

* compressor

|名字|compressor|
|:---:|:---|
|描述|数据压缩方法|
|类型|枚举String : “UNCOMPRESSED”, “SNAPPY”|
|默认值| UNCOMPRESSED |
|改后生效方式|即时生效|

* group\_size\_in\_byte

|名字|group\_size\_in\_byte|
|:---:|:---|
|描述|每次将内存中的数据写入到磁盘时的最大写入字节数|
|类型|Int32|
|默认值| 134217728 |
|改后生效方式|即时生效|

* max\_number\_of\_points\_in\_page

|名字| max\_number\_of\_points\_in\_page |
|:---:|:---|
|描述|一个页中最多包含的数据点（时间戳-值的二元组）数量|
|类型|Int32|
|默认值| 1048576 |
|改后生效方式|即时生效|

* max\_string\_length

|名字| max\_string\_length |
|:---:|:---|
|描述|针对字符串类型的数据，单个字符串最大长度，单位为字符|
|类型|Int32|
|默认值| 128 |
|改后生效方式|即时生效|

* page\_size\_in\_byte

|名字| page\_size\_in\_byte |
|:---:|:---|
|描述|内存中每个列写出时，写成的单页最大的大小，单位为字节|
|类型|Int32|
|默认值| 65536 |
|改后生效方式|即时生效|

* time\_series\_data\_type

|名字| time\_series\_data\_type |
|:---:|:---|
|描述|时间戳数据类型|
|类型|枚举String: "INT32", "INT64"|
|默认值| Int64 |
|改后生效方式|即时生效|

* time\_encoder

|名字| time\_encoder |
|:---:|:---|
|描述| 时间列编码方式|
|类型|枚举String: “TS_2DIFF”,“PLAIN”,“RLE”|
|默认值| TS_2DIFF |
|改后生效方式|即时生效|

* float_precision

|名字| float_precision |
|:---:|:---|
|描述| 浮点数精度，为小数点后数字的位数 |
|类型|Int32|
|默认值| 默认为2位。注意：32位浮点数的十进制精度为7位，64位浮点数的十进制精度为15位。如果设置超过机器精度将没有实际意义。|
|改后生效方式|即时生效|

#### 引擎层配置

* back\_loop\_period

|名字| back\_loop\_period |
|:---:|:---|
|描述| 系统统计量触发统计的频率，单位为秒。|
|类型|Int32|
|默认值| 5 |
|改后生效方式|重启服务器生效|

* data\_dir

|名字| data\_dir |
|:---:|:---|
|描述| IoTDB数据存储路径，默认存放在和bin目录同级的data目录下。相对路径的起始目录与操作系统相关，建议使用绝对路径。|
|类型|String|
|默认值| data |
|改后生效方式|重启服务器生效|

* enable_wal

|名字| enable_wal |
|:---:|:---|
|描述| 是否开启写前日志，默认值为true表示开启，配置成false表示关闭 |
|类型|Bool|
|默认值| true |
|改后生效方式|重启服务器生效|

* fetch_size

|名字| fetch_size |
|:---:|:---|
|描述| 批量读取数据的时候，每一次读取数据的数量。单位为数据条数，即不同时间戳的个数。某次会话中，用户可以在使用时自己设定，此时仅在该次会话中生效。|
|类型|Int32|
|默认值| 10000 |
|改后生效方式|重启服务器生效|

* force\_wal\_period\_in\_ms

|名字| force\_wal\_period\_in\_ms |
|:---:|:---|
|描述| 写前日志定期刷新到磁盘的周期，单位毫秒，有可能丢失至多flush\_wal\_period\_in\_ms毫秒的操作。 |
|类型|Int32|
|默认值| 10 |
|改后生效方式|重启服务器生效|

* flush\_wal\_threshold

|名字| flush\_wal\_threshold |
|:---:|:---|
|描述| 写前日志的条数达到该值之后，刷新到磁盘，有可能丢失至多flush\_wal\_threshold个操作 |
|类型|Int32|
|默认值| 10000 |
|改后生效方式|重启服务器生效|

* merge\_concurrent\_threads

|名字| merge\_concurrent\_threads |
|:---:|:---|
|描述| 乱序数据进行合并的时候最多可以用来进行merge的线程数。值越大，对IO和CPU消耗越多。值越小，当乱序数据过多时，磁盘占用量越大，读取会变慢。 |
|类型|Int32|
|默认值| 0 |
|改后生效方式|重启服务器生效|

* multi\_dir\_strategy

|名字| multi\_dir\_strategy |
|:---:|:---|
|描述| IoTDB在tsfile\_dir中为TsFile选择目录时采用的策略。可使用简单类名或类名全称。系统提供以下三种策略：<br>1. SequenceStrategy：IoTDB按顺序从tsfile\_dir中选择目录，依次遍历tsfile\_dir中的所有目录，并不断轮循；<br>2. MaxDiskUsableSpaceFirstStrategy：IoTDB优先选择tsfile\_dir中对应磁盘空余空间最大的目录；<br>3. MinFolderOccupiedSpaceFirstStrategy：IoTDB优先选择tsfile\_dir中已使用空间最小的目录；<br>4. <UserDfineStrategyPackage>（用户自定义策略）<br>您可以通过以下方法完成用户自定义策略：<br>1. 继承cn.edu.tsinghua.iotdb.conf.directories.strategy.DirectoryStrategy类并实现自身的Strategy方法；<br>2. 将实现的类的完整类名（包名加类名，UserDfineStrategyPackage）填写到该配置项；<br>3. 将该类jar包添加到工程中。|
|类型|String|
|默认值| MaxDiskUsableSpaceFirstStrategy |
|改后生效方式|重启服务器生效|

* rpc_address

|名字| rpc_address |
|:---:|:---|
|描述| |
|类型|String|
|默认值| "0.0.0.0" |
|改后生效方式|重启服务器生效|

* rpc_port

|名字| rpc_port |
|:---:|:---|
|描述|jdbc服务监听端口。请确认该端口不是系统保留端口并且未被占用。|
|类型|Short Int : [0,65535]|
|默认值| 6667 |
|改后生效方式|重启服务器生效||

* time_zone

|名字| time_zone |
|:---:|:---|
|描述| 服务器所处的时区，默认为北京时间（东八区） |
|类型|Time Zone String|
|默认值| +08:00 |
|改后生效方式|重启服务器生效|

* enable\_stat\_monitor

|名字| enable\_stat\_monitor |
|:---:|:---|
|描述| 选择是否启动后台统计功能|
|类型| Boolean |
|默认值| false |
|改后生效方式|重启服务器生效|

* concurrent\_flush\_thread

|名字| concurrent\_flush\_thread |
|:---:|:---|
|描述| 当IoTDB将内存中的数据写入磁盘时，最多启动多少个线程来执行该操作。如果该值小于等于0，那么采用机器所安装的CPU核的数量。默认值为0。|
|类型| Int32 |
|默认值| 0 |
|改后生效方式|重启服务器生效|


* stat\_monitor\_detect\_freq\_in\_second

|名字| stat\_monitor\_detect\_freq\_in\_second |
|:---:|:---|
|描述| 每隔一段时间（以秒为单位）检测当前记录统计量时间范围是否超过stat_monitor_retain_interval，并进行定时清理。|
|类型| Int32 |
|默认值|600 |
|改后生效方式|重启服务器生效|


* stat\_monitor\_retain\_interval\_in\_second

|名字| stat\_monitor\_retain\_interval\_in\_second |
|:---:|:---|
|描述| 系统统计信息的保留时间（以秒为单位），超过保留时间范围的统计数据将被定时清理。|
|类型| Int32 |
|默认值|600 |
|改后生效方式|重启服务器生效|
