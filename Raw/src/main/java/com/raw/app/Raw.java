package com.raw.app;

import com.datastax.driver.core.Session;
import com.raw.connectors.CassandraConnector;

import java.sql.Timestamp;
import java.util.Random;

public class Raw {
    private static final String TABLE_NAME_RAW = "demodb.Raw";

    public static void main(String[] args) {

        CassandraConnector dbClient = new CassandraConnector();
        Session session = dbClient.createConnection();

        System.out.println("VAMOS CRIAR O GRANDE RAW 3.0 ::::::::::");

        Random rand = new Random();
        for (int i = 0; i < 500; ++i) {
            /*
             * StringBuilder queryQeue = new
             * StringBuilder("INSERT INTO ").append(TABLE_NAME_RAW)
             * .append("(id, deviceName, deviceType, bitrate ,ImpactingEvents , TCPLoss , timestamp)"
             * ) .append("VALUES (uuid()").append(", '")
             * .append("device").append(rand.nextInt(1000)).append("', '")
             * .append("Router").append("', ") .append(rand.nextInt(1000)).append(",")
             * .append(rand.nextInt(1000)).append(",")
             * .append(rand.nextInt(1000)).append(",'") .append(new
             * Timestamp(System.currentTimeMillis())).append("');");
             */
            StringBuilder queryQeue = new StringBuilder();

            queryQeue.append("INSERT INTO ").append(TABLE_NAME_RAW)
                    .append("(id, deviceName, deviceType, bitrate ,ImpactingEvents , TCPLoss , timestamp)")
                    .append("VALUES (uuid()").append(", '").append("device").append(rand.nextInt(1000)).append("', '")
                    .append("Router").append("', ").append(rand.nextInt(1000)).append(",").append(rand.nextInt(1000))
                    .append(",").append(rand.nextInt(1000)).append(",'")
                    .append(new Timestamp(System.currentTimeMillis())).append("');");

            System.out.println("Finish job with query: " + queryQeue.toString());
            session.execute(queryQeue.toString());
        }
        dbClient.close();
    }
}
