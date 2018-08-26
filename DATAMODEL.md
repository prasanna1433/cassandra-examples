# Apache Cassandra - Data Modeling

Data modeling consists of (query - modeling techniques):

1. Conceptual 
2. Logical
3. Physical

We can follow some proven techniques to build the data model and the objective of using Cassandra is return the result lightening fast.

Problems with using relational databases when the load is high:

1. Single point of failure
2. Scaling complexity
3. Reliability issues
4. Difficult to serve users around the globe

Starting with Cassandra
* Keyspace
* Table
* Data Types
* Copy
* select 


# Data Modeling

* Partition key is used to group data and place all the data in the same node
* clustering key is used to arrange the data in ascending / descending order
* ranger queries can run only in clustering column
* cassandra will not do a read before write when you are asking to insert a data with same primary key it will just insert it adn delete the old record
* cassandra doesn't like joins and we create a tables to satisfy a query
* if the clustering column is a time series data then try to order it by descending order so that you will get the latest data first in the query


# Collections

* SET<TEXT> : This holds only unique entries
* LIST<TEXT>: This can hold duplicates
* MAP<TEXT,INT>: This holds the key value pair and the key is unique
* CREATE TYPE type_name (columns type)
* when you are using the UDT you have to specify it with frozen key word

# Counters

* Since Cassandra is a distributed system we cannot maintain a counter value due to concurrency issue and hence they introduced counter type
* counter is always initialized to 0 and is incremented / decremented from then on
* if one column is counter then all the other columns should be counter or empty


# Sourcing File

* source ./file.cql and all teh commands inside the .cql file is executed

# Denormalization in Cassandra:

* Reason for denormalization in cassandra is that we can gee the results in constant time 
* trading in storage cost which for speed

*Winging the data model is not going to solve the problem that you are trying to solve adn we need to do conceptual data model*

# Conceptual Data Model

* ER model
* **Entity:** Any object that we are planning to track
* **Relationship:** Relationship between entities
* **Cardinality:** how many relationship an entity participates in
* **Attributes:** Features of the entity type
* **Key Attribute:** Uniquely identifies the entity
* **Composite Attribute:** Single attribute can be split into multiple values (eg) full_name
* **Multi - Valued attribute:** Single attribute that has multiple values (eg) tags
* **Weak Entity:** It will not exists with out a main entity type and it is represented by double lined entity type
* **Identifying relationship:** Relationship that connects weak entity type to the main entity type

# Relationship Type

* **One to One relationship type:** in this we can identify the items in the entities that are involved in the relationship unique
* **One to many relationship type:** in this one entity is related to multiple entities in one way and the other way entities can only have one relation to primary entity
* **many to many relationship type:** we should combine the key to get the unique item


# Hierarchy

* If an entity inherits all the attributes from the parent then it in a relationship with its parent
* transitive hierarchy: If a is a parent of b and b is a parent of a then a is a parent of c.
* disjoint covering - the parent should be either of the values of it children
* disjoint not covering  - the parent can be one among the values of its child or can be other


# Application Workflow

* We need to identify the access patterns of an application
* Also find the relationship on how the access points are interacting with each other
* various points of application can be in and the relationship of how to move back and forth
* Usually product owner decides the flow of the application and what are all teh access patterns that exists

# Logical Data Model

* We follow Query Driven Data Modeling
* When we have a solid understand of the Conceptual data model and the acces patterns to the product then the things will fall in place


# Chebotko Diagram

* Graphical representation of Cassandra database schema design
* Documents the logical and physical data model
* It is used to visualize the whole workflow of how things work in cassandra
* In Logical data model we will identify the column name and its properties
* In Physical Data Model will determining the column data type and perform optimization of the table


# Principle of Logical Data Modeling

* **Know your data:** We need to understand the data that is in use thoroughly. Key contains are key as said and affect schema design. We need a primary query to satisfy the query and it should also satisfy the uniqueness.
* **Know your queries:** What users are asking and we need to find the application model
	* **Partition per query* - ideal - constant time look up and the application is happy
	* *More than one Partition per query** - acceptable - less efficient but not that bad
	* **Table Scan** - anti pattern - Linear search is really bad
	* **Multi Table** - anti pattern
* **Nest Data:** We denormalize the table. We need to make use of the clustering column that will speed up your application
* **Duplicate Data:** Better to duplicate to join the data because we always make a trade off between time and space. Join the data by duplicating on write and hance our read is optimal
	* If we are using a base table to get an information and query for most of then result based on the first ones result then it bad instead we can write it all into a single table
	* Percompute query result by creating materialized views of the tables
	* Data Duplication can scale but joins are not
	* Cassandra queries should return result immediately and we shold ne have tables that requires multiple read to satisfy the request



# Mapping Rules for Conceptual to Logical model

* MR1: **Entities and Relationships:** Entity and relationship map to partitions / rows. Attributes map to columns
* MR2: **Equality search attributes:** Equality search attribute will become the partition key and we can also query on equality in clustering column along with range
* MR3: **Inequality search attribute:** When we need a range query then we have to store it in the clustering column and we cant have anything after the range query. If we have an inequality condition then all the ones follow that is inequality
* MR4: **Ordering Attribute:** This is determined by the clustering key and should be decided upfront and how the data should be ordered for the query
* MR5: **Key Attributes:** Primary key should contains the key attribute in the entity in it so that we can query on the data uniquely

# Mapping Patterns


* Semi formal definition of the commonly used patterns in doing logical data model
* Entity Mapping pattern: 
	* query attribute = key attribute: Here depending on the access pattern you will have all the key attribute as partition key or few as partition key and others as clustering key
	* query attribute != key attributes: Here we will add the non key attribute to the partition key and make the other key attributes as clustering / partition key based on the range query constrain
* Relationship Mapping pattern:
	* 1:1: Combine all the fields and chose any key from one of the entity in the relationship. Determine the partition key by the key attribute and the clustering key by the query itself and ordering of the clustering key is determined by the query
	* 1:n: Here based on the query we have the non primary key as the clustering column and then add uniqueness to the key by using the clustering column
	* Static column are the columns that are stored only once in the entire partition and are used when we have some content that is same for the whole partition
	* m:n: In this case when we have a query that satisfies the access pattern and the keys from the both the entities involve in the relationship will be a part of the primary key and query determines what can be apartition key and what can be a clustering column.
	* Partition key is unique then the information that will not change can be added as a static column eg when we have user id as partition key then the first and last name can become a static column and will be stores only once per partition
* Hierarchical Mapping:
	* We add all the column for the hierarchy to the table and have a column that identifies the kind of the hierarchy that is being inherited
	* When the hierarchy i snot disjoin then we have a singkle column or else we have a composite column that can store the set of types

* More on Mapping rules 
	* From our query we have to understand if we are querying for an entity type / relationship and then we need to understand what is the range of queries that we would be doing 
	* We can use a role name when there is more than one relationship to differentiate the relationship
	* When the entity is weak then we have to take the key of the identifying entity


# Physical Data Model

## Analysis and validation
	* Analysis
	    * To identify the partition size ,  nesting data , duplicating data because in real world we know that disk size is limited adn operation not supported by cassandra are inefficient

	* Partition Size: 
        * Number of Values = number of rows * (number of columns - number of primary key - number of static columns) + number of static columns
        * Size of Table = sum of size of partition key column + sum of size of static column + number of rows * sum for each regular column (( sum of the size of the regular column + sum of the size clustering column))+ 8 * number of size of values
        * key of a column in cassandra is the concatination of the clustering column followed by column name of a regular column

	* Data Duplication:
        * By product of modeling principles and we have to control it and make the data consistent across the tables
        * We need to identify the constant copying and unbounded copying
        * Avoid linear/quadratic or any other growth of data apart from constant in cassandra

	* Data Consistency
        * We can use BATCH command and all the command will be consistent
        * Documented plan is needed to maintain the data consistently

	* Client side Joins
        * Data is store in a materialized fashion were the join is done during write
        * We can use the inverted indexes concept to reduce the resource and have the data stores only once. But we need to have referencial integrity for making it to work

	* Lightweight Transactions
        * IF NOT EXISTS / IF is used when we have to check before inserting into cassandra which give an approx ACID constrain
        * This does an compare and set

	* Data Aggregation:
        * Cassandra that will not support any thing other than count
        * counter is the datatype that is given by cassandra
        * We can so the aggregation on the application side and get the value

## Physical Optimization
	* Key Optimization:
        * Unique and minimal is the key in the traditional DB
        * We can select a natural value as a key / surrogate key

	* Table Optimization
        * Splitting Partitions in a Table:
            * If the partition grows really big then we may have to spil the partition by adding a partition key
            * We can cheat in this techniqie by using a bucket column and we will increment it when the size of the partition grow big we change the bucket value and most out of the data stores

        * Vertical Partitioning:
            * Based on the query we can split the table and have some columns moved to another table
            * If replication is problem then we can merge few table into a single table

        * Merging partition into a single partitions
            * In this case we need to make sure that the distribution of the values should be event and hance we can control the column and make it a partition key so that we can have even distribution

        * Adding columns to the table
            * We can have some of the columns added into the table inorder to reduce the time of computation and we can determine the applciation frequency that needs to be updated so that we can reduce the computation when the data is quries for that partition

	* Concurrent Access to data
		* We can use counter or LWT
		* Else we can create another table and aggregate at the application level
## Adding datatype
## CQL tables


