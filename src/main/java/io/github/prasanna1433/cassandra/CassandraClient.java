package io.github.prasanna1433.cassandra;

import com.datastax.driver.core.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.log4j.Logger;

public class CassandraClient {

    public static void main(String[] args){
        Logger LOGGER = Logger.getLogger(CassandraClient.class);
        Cluster cluster = null;
        try {
            cluster = Cluster.builder()
                    .addContactPoint("127.0.0.1")
                    .build();
            Session session1 = cluster.connect();

            //Simple Statement Example
            //Simple statement is good when you want the query is not to frequent
            ResultSet rs = session1.execute("select release_version from system.local");
            Row row = rs.one();
            LOGGER.info("Cassandra Release Version "+row.getString("release_version"));

            //Prepare Statement
            //Prepare Statement is used when we are having to do the same operation frequently
            //For the below statement ot run create keyspace and table using the cqlsh connected to your local c* cluster
            //Create a keyspace "example" -> CREATE KEYSPACE example WITH REPLICATION = {'class':'SimpleStrategy','replication_factor':1};
            //and table "student" -> CREATE TABLE student (rollno int, name text, age int, PRIMARY KEY (rollno));
            Session session2=cluster.connect("example");
            PreparedStatement preparedStatement=session2.prepare("insert into student (rollno, name, age) values (?,?,?)");
            Map<String,Integer> studentMap=new HashMap<String, Integer>();
            studentMap.put("Rohit",12);
            studentMap.put("Dave",14);
            studentMap.put("tina",11);
            int rollno=1;
            Iterator it = studentMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                LOGGER.info(new StringBuilder().append("insert student ").append(pair.getKey()).append(" with age ")
                        .append(pair.getValue()).append(" into table student").toString());
                session2.execute(preparedStatement.bind(rollno, pair.getKey(), pair.getValue()));
                rollno++;
                it.remove(); // avoids a ConcurrentModificationException
            }
        } finally {
            if (cluster != null) cluster.close();
        }
    }
}
