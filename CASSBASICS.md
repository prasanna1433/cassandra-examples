# Apache Cassandra Basics

Reasons to use Apache Cassandra:

* Peer to Peer and not master/slave
* Reliability = Always Available and can tolorate failures
* Scalability = Capacity can be added any time
* Geographical Availability = Data can be put closer to the client

# CQL - Cassandra Query Language

Seems to be similar to SQL but not really we cannot do joins, group by and aggregates.

**Keyspaces** - Top level container similar to database schema and this is where we store the replication factor.

To start cassandra in you local machine navigate to the cassandra folder and type bin/cassandra

```
INFO  [main] 2018-08-07 21:42:19,865 StorageService.java:2292 - Node localhost/127.0.0.1 state jump to NORMAL
```

**Tables** - It is the place were data is stored in Cassandra and we spend most of the time at

##  Basic CQL Commands:

* USE = This statement is used to switch between the container / keyspaces.
* CREATE KEYSPACE killrvideos  WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':'1'};
* CREATE TABLE users ( state text, city text, name text, id int, PRIMARY KEY((state),city,name,id)) WITH CLUSTERING ORDER BY(city DESC, name ASC);
* INSERT INTO table_name VALUES (columns) = This statement is used to insert the data into the tables.
* SELECT columns FROM tables_name WHERE primary key  = This statement is used to retrieve the data from the table.
* COPY table_name (columns) FROM 'table_data.csv' WITH HEADER=true = This statement is used to import/export CSV
* Cassandra has lot of [data types](https://docs.datastax.com/en/cql/3.3/cql/cql_reference/cql_data_types_c.html)

# Partitions:

* **Partition Key:** It is the grouping of data based in some value which can be though of as a ordering scheme in the disk. A partitioner is the one that creates a token for a partition.
* **Clustering Key:** We make the partition key unique by adding few more value to the grouping of data and this also helps in ordering of data in the disk.
* **Primary Key** It is the combination of Partition Key + Clustering Key


# Driver:

* [Driver](https://docs.datastax.com/en/developer/java-driver/3.1/) is used for connecting to the Cassandra and talking to it. Datastax has open sourced the drivers available in various languages.


# Node:

* This is where the data is stored and we need to connect a node to store/retreive the data from/to the disk and the disk can be a SSD/rotational disk
* Node play a vital part in running the cassandra cluster and can take in 3000 - 5000 transaction/second/core
* Data is stored in a node and each node will be talking to other nodes all the time.


# Ring:

* Cassandra like to have more that one node as a part of ring and this allows it to scale.
* Each of the node will take a token range and it is from -2 power 63 to + 2 power 63 - 1.
* All the nodes acts as coordinator to write and read the data to and from the cluster.
* A node in a ring can in any of this four states *Up/Down/Joining/Leaving*
* We can scale the cluster vertically / horizontally.
* Partitioner will determine were to distribute the data and it should be proper so that distribution of data is even in the cluster.
* Cassandra Driver has the important role on choosing the node to talk to and token ranges spread across the ring. Following are few of the policies available to talk to the cluster
	* **TokenAwarePolicy** - driver know the node to send  and read the data from
	* **RoundRobinPolicy** - driver uses round robins to route the request for data and write the data in the ring
	* **DCAwareRoundRobinPolicy** - driver will round robin to route the request for data and write the data in the local datacenter

# Peer to Peer:

* Split brain problem will arise when we are using the leader / follower concept or do sharding of data.
* Coordinator can write to all the replicas asynchronous at the same time.
* Consistency level is used to control how are going to handle the failure of nodes in the cluster and the developers / architects have the control over this setting.


# VNodes:

* Chunk up the distribution of the data around the ring and this can controlled by a setting num_tokens property in cassandra.yaml
* Each node has ranges of data among the node.
* By default 256 nodes per cluster before 3.0 and this is controlled by the num_token settings.


# Gossip:

* Sharing of information about other nodes as a node sees it among the nodes in the Cassandra cluster.
* A node picks one other random node to gossip every specified time.
* Cluster Metadata (Endpoint State, Heartbeat State, Application State) is the information that is being shared among the nodes.
* When the metadata is arrives in the other node it looks at its information and sends what information it sees to the node that initiated gossip.
* Initial node then compares this data with it own and send an acknowledgement to the other one.
* Sync is sent and then first and the second node share the ACKS to get the final updated data.


# Snitch:

* Snitch is used to inform where each node is located resides in the cloud.
* Regular and Cloud Based are the two main types of the snitch.
* Types of Snitch:
   * **SimpleSnitch:** Local system and is not recommended in production environment
   * **Property file Snitch:** We need to store the information about the whole cluster ip:dc:rack in a cassandra-topology.properties file
   * **Gossip File snitch:** it enough if you store the dc:rack information of each node in cassandra-rackdc.properties and is the recommended one in production
   * **Rack Inferring Snitch:** used the ip to determine the dc:rack:node information.
* **Dynamic Snitch:** This is used under the cover by all the snitch's and will determine the load that the cluster is on so that it can redirect the traffic to various nodes.



# Replication:

* **Simple Strategy:** This is the configuration that works if you are in single data center
* **Network Topology Strategy:** This is the configuration that works we have multiple data centers

* RF 1 Each node has different sets of data
* RF 2 Each node has its own and other
* RF 3 Each node has its own and data about other two nodes



# Consistency:

* This controls how we read and write the data into cassandra.
* Each read / write needs acknowledgement from the replicas of the data and we can chose how long to wait before moving on to the next read/write
* **QUORUM** is 51% should respond to teh read/write request
* **EACH_QUORUM** is that we need to wait for 51% across all the datacenter
* **LOCAL_QUORUM** is the we need to wait for 51% in the nearest datacenter
* Elaborate list of consistencies available are found [here](https://docs.datastax.com/en/cassandra/3.0/cassandra/dml/dmlConfigConsistency.html)


# Hinted Handoff:

* When a node is down then the coordinator will hold on to the data for it and once it is back up that data is sent it as a hint
* **ANY** is bad  and it means that hints are enough for teh write request to be acknowledged and
* **ONE** is at least ok because one of the replicas need to have the data and should acknowledge the write, it will be eventually consistent because one node acknowledged that write
* We can control the hinted hand off feature by setting the appropriate parameters in cassandra.yml file


# Read - Repair:

* Keep data in sync among the nodes in the cluster and it is done by doing a check with the other replicas whenever a read is initiated in case of QUORUM
* Coordinator node will do that comparision of data and make the data consistent by sending the latest copy of data to other replicas if the timestamps are different
* nodetool repair will make all the data consistent and we need to run it at least once every grace period


# Read Path:

* **Memtable** is in memory and holds the data for hash of the partition key and we can read the data directly in memory.
* **SSTable** is in disk and contains range of the partition key and the data itself
* **Partition index** is the way to simplify the read od data from SSTable and it is also located in the disk
* **Summary index** as the data is expected to grow really fast in teh cassandra cluster there is another data structure called summary index/partition summary in RAM where we can find the offset approximation for the partition index
* **Key cache** will hold the location to partition of the data that is accessed frequently
* **bloom filter** is the first data structure that you will read and it will give you no when the data is absolutely not present in the SSTable and may be when there is a possibility when the data exists
* If the bloom filter give a may be then we need to go to the key cache, summary index and partition index for fetching the data
* Path to the data on read -> **Memtable - Row Cache(if enabled) - Bloom Filter - Key Cache - Summary Index - Partition Index - SSTable**

Read more about Read Path [here](https://docs.datastax.com/en/cassandra/3.0/cassandra/dml/dmlAboutReads.html)

# Write Path:

* Data written to cassandra goes to the coordinator and will be routed to the appropriate partition
* In the appropriate node it is written to commit logs and memtable first
* When the memtable is full then data is flushed into the disk to SSTable
* Path to write data -> **commit log, memtable -> SSTable**

Read more about the Write Path [here](https://docs.datastax.com/en/cassandra/3.0/cassandra/dml/dmlHowDataWritten.html)

