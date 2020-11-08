kubectl exec cassandra-test-cluster-dc1-rack1-0 -c cassandra -- cqlsh -e "TRUNCATE demodb.Raw;" cassandra-test-cluster-dc1-nodes

kubectl exec cassandra-test-cluster-dc1-rack1-0 -c cassandra -- cqlsh -e "TRUNCATE demodb.Report;" cassandra-test-cluster-dc1-nodes

kubectl exec cassandra-test-cluster-dc1-rack1-0 -c cassandra -- cqlsh -e "TRUNCATE demodb.WorkQueue;" cassandra-test-cluster-dc1-nodes

kubectl exec cassandra-test-cluster-dc1-rack1-0 -c cassandra -- cqlsh -e "TRUNCATE demodb.Profile;" cassandra-test-cluster-dc1-nodes