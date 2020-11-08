package com.reporter.connectors;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Cluster.Builder;

public class CassandraConnector {
 
    private Cluster cluster;
 
    private Session session;
 
    public void connect(String node, Integer port) {
        Builder b = Cluster.builder().addContactPoint(node);
        if (port != null) {
            b.withPort(port);
        }
        cluster = b.build();
 
        session = cluster.connect();
    }
 
    public Session getSession() {
        return this.session;
    }
    
    public void close() {
        session.close();
        cluster.close();
    }

    public Session createConnection(){
        connect("cassandra-test-cluster-dc1-nodes", 9042);
        return this.session;
    }
    
}