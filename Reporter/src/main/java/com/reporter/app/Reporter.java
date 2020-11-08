package com.reporter.app;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.utils.UUIDs;
import com.reporter.connectors.CassandraConnector;
import com.reporter.types.Report;

import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.sql.*;

public class Reporter {
    private static final String TABLE_NAME_REPORT = "demodb.Report";
    private static final String TABLE_NAME_RAW = "demodb.Raw";
    private static final String TABLE_NAME_QUEUE = "demodb.WorkQueue";

    public static void main(String[] args) throws ClassNotFoundException, SQLException
    {
        //como sei o KPI a calcular?
        String kpi = args[0];
        int timewindowReport=30;
        CassandraConnector dbClient = new CassandraConnector();
        Session session= dbClient.createConnection();


        UUID timeBasedUuid = UUIDs.timeBased();
        String reportID = timeBasedUuid.toString();

        System.out.println("VAMOS CRIAR O REPORT COM WATCH:::::::::: "+reportID );

        StopWatch watch = new StopWatch();
        watch.start();
        Class.forName("com.mysql.jdbc.Driver");  
       /* Connection con=DriverManager.getConnection("jdbc:mysql://mysql:3306","root","password");  
        Statement stmt=con.createStatement();  
        java.sql.ResultSet rs=stmt.executeQuery("SELECT timewindowReport FROM Parametros.Parametro Where kpi ='"+kpi+"';");  
        System.out.println("VAMOS BUSCAR o TImeWindow SELECT timewindowReport FROM Parametros.Parametro Where kpi ='"+kpi+"'; ");
        if(rs.next()){
            timewindowReport = rs.getInt("timewindowReport");
       }
       con.close();  */
       System.out.println("VOU BUSCAR OS ULTIMOS "+timewindowReport+" Minutos");

        long now = System.currentTimeMillis();
        Timestamp limitdate = new Timestamp(now - (timewindowReport * 60 * 1000));
        StringBuilder query=  new StringBuilder("SELECT * FROM ").append(TABLE_NAME_RAW);
        query = query.append(" WHERE timestamp >='"+limitdate.toString()+"' ALLOW FILTERING;");

        System.out.println("Começou com query ="+ query);
        ResultSet resultReports = session.execute(query.toString());
        
        System.out.println("lets get the reports! ");
        List<Report> reportList = new ArrayList<Report>();
        resultReports.forEach(r -> {
            System.out.println(r.getUUID("id"));
            System.out.println(r.getString("deviceName"));
            System.out.println(r.getString("deviceType"));
            System.out.println(kpi);
            System.out.println(r.getTimestamp("timestamp"));


            reportList.add(new Report(
              r.getUUID("id"), 
              r.getString("deviceName"),  
              r.getString("deviceType"), 
              kpi, 
              r.getVarint(kpi), 
              new Timestamp(r.getTimestamp("timestamp").getTime())));
        }
        
        
        );
        
        if(reportList.isEmpty()){
            System.out.println("I dont have work...");
            dbClient.close();
            return;
        }

      /*  System.out.println("lets show what we have!! -"+reportList.size());
        reportList.forEach(r -> {
              System.out.println( "###########"+r.getId()+" - "+
              r.getDeviceName()+" - "+ 
              r.getDeviceType()+" - "+ 
              r.getKpiReport()+" - "+
              r.getValue()+" - "+
              r.getTimestamp().toString());
        });*/
        System.out.println("lets create a BIG INSERT!");
        

        reportList.forEach(r -> {

            StringBuilder queryReport = new StringBuilder();
            queryReport.append("INSERT INTO ").append(TABLE_NAME_REPORT)
            .append("(id, reportid, deviceName, deviceType, KpiReport, value, timestamp) ")
            .append("VALUES (uuid()").append(", '")
            .append(reportID).append("', '")
            .append(r.getDeviceName()).append("', '")
            .append(r.getDeviceType()).append("', '")
            .append(r.getKpiReport()).append("', ")
            .append(r.getValue()).append(", '")
            .append(r.getTimestamp()).append("');");


            System.out.println("BIG INSERT:"+queryReport.toString());
            session.execute(queryReport.toString());

        });


        System.out.println("DONE! :"+reportID);
        System.out.println("Added to queue :"+reportID);
        watch.stop();
        StringBuilder queryQeue = new StringBuilder("INSERT INTO ").append(TABLE_NAME_QUEUE)
        .append("(reportid, KpiReport, watch)")
        .append("VALUES ('").append(reportID).append("','"+kpi+"',"+ watch.getTotalTimeSeconds()+");");
        session.execute(queryQeue.toString());
    
        System.out.println("Finish job with query: "+queryQeue.toString());

        dbClient.close();
    }
}
