kubectl exec cassandra-test-cluster-dc1-rack1-0 -c cassandra -- cqlsh -e "CREATE KEYSPACE demodb WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };" cassandra-test-cluster-dc1-nodes

kubectl exec cassandra-test-cluster-dc1-rack1-0 -c cassandra -- cqlsh -e "use demodb;CREATE TABLE Raw(id uuid PRIMARY KEY, deviceName text, deviceType text, bitrate varint,ImpactingEvents varint, TCPLoss varint, TIMESTAMP timestamp);" cassandra-test-cluster-dc1-nodes

kubectl exec cassandra-test-cluster-dc1-rack1-0 -c cassandra -- cqlsh -e "use demodb;CREATE TABLE Report(id uuid , reportid text , deviceName text, deviceType text, KpiReport text ,value varint, TIMESTAMP timestamp,PRIMARY KEY(id, reportid));" cassandra-test-cluster-dc1-nodes

kubectl exec cassandra-test-cluster-dc1-rack1-0 -c cassandra -- cqlsh -e "use demodb;CREATE TABLE WorkQueue(reportid text PRIMARY KEY,  KpiReport text, watch double);" cassandra-test-cluster-dc1-nodes

kubectl exec cassandra-test-cluster-dc1-rack1-0 -c cassandra -- cqlsh -e "use demodb;CREATE TABLE Profile(id uuid PRIMARY KEY, reportid text, KPI text, value varint, TIMESTAMP timestamp);" cassandra-test-cluster-dc1-nodes
