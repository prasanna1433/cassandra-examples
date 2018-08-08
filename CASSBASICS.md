# Apache Cassandra Basics

Reasons to use Apache Cassandra:

* Peer to Peer and not master/slave
* Reliability = Always Available and can tolorate failures
* Scalability = Capacity can be added any time
* Geographical Availability = Data can be put closer to the client

# CQL - Cassandra Query Language

Seems to be similar to SQL but not really we cannot do joins, group by and aggregates.

*Keyspaces* - Top level container similar to database schema and this is where we store the replication factor.

To start cassandra in you local machine navigate to the cassandra folder and type bin/cassandra

```
INFO  [main] 2018-08-07 21:42:19,865 StorageService.java:2292 - Node localhost/127.0.0.1 state jump to NORMAL
```

*Tables* - It is the place were data is stored in Cassandra and we spend most of the time at

##  Basic CQL Commands:

* USE = This statement is used to switch between the container / keyspaces.
* CREATE KEYSPACE killrvideos  WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':'1'};
* CREATE TABLE users ( state text, city text, name text, id int, PRIMARY KEY((state),city,name,id)) WITH CLUSTERING ORDER BY(city DESC, name ASC);
* INSERT INTO table_name VALUES (columns) = This statement is used to insert the data into the tables.
* SELECT */columns FROM tables_name WHERE primary key  = This statement is used to retreive the data from the table.
* COPY table_name (columns) FROM 'table_data.csv' WITH HEADER=true = This statement is used to import/export CSV

Data Types:
-> int
-> text

# Partitions:

* Partition Key: It is the grouping of data based in some value. It can be though of as a ordering scheme in the disk. A partitioner is the one that creates a token for a partition.
* Clustering Key: It makes the partition key unique by adding this key in.
* Primary Key = Partition Key + Clustering


# Driver:

* Driver is used for connecting to the Cassandra and talking to it. Datastax has open sourced the drivers available in variour languages


# Node:

* This is where the data is stored and we need to connect the node to store the data in a disk and it can be a SSD/rotational disk
* Node play a vital part in running the cassandra cluster and can take in 3000 - 5000 transaction/second/core
* Data is stored in a node and each node will be talking to other nodes all the time.