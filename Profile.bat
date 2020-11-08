kubectl exec cassandra-test-cluster-dc1-rack1-0 -c cassandra -- cqlsh -e "use demodb;INSERT INTO Profile (id, value, bitrate) VALUES(uuid(),'Steve',50000);" cassandra-test-cluster-dc1-nodes
kubectl exec cassandra-test-cluster-dc1-rack1-0 -c cassandra -- cqlsh -e "use demodb;INSERT INTO Profile (id, value, bitrate) VALUES(uuid(),'Steve',60000);" cassandra-test-cluster-dc1-nodes
kubectl exec cassandra-test-cluster-dc1-rack1-0 -c cassandra -- cqlsh -e "use demodb;INSERT INTO Profile (id, value, bitrate) VALUES(uuid(),'Steve',70000);" cassandra-test-cluster-dc1-nodes

