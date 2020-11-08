package com.profiler.app;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Row;

import com.profiler.connectors.CassandraConnector;
import com.profiler.types.Report;

import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.math.BigInteger;
import java.sql.*;


public class Profiler 
{


    private static final String TABLE_NAME_PROFILE = "demodb.Profile";
    private static final String TABLE_NAME_REPORT = "demodb.Report";
    private static final String TABLE_NAME_QUEUE = "demodb.WorkQueue";
    public static void main( final String[] args ) throws ClassNotFoundException, SQLException
    {

        //como sei o KPI a calcular?
        final String kpi = args[0];
        StopWatch watch = new StopWatch();
        final String O_KPI_E_INFERIOR_AO_VALOR_DE_REFERENCIA = "O valor do "+kpi+" é inferior ao valor de referência: ";
        final String O_KPI_E_SUPERIOR_AO_VALOR_DE_REFERENCIA = "O valor do  "+kpi+" é superior ao valor de referência: ";
        BigInteger valMax =  BigInteger.valueOf(1000);
        BigInteger valMin =  BigInteger.valueOf(0);
        final CassandraConnector dbClient = new CassandraConnector();
        final Session session= dbClient.createConnection();
         watch.start();
        //Get Reportid from Queue
        final StringBuilder queryGetQueue=  new StringBuilder("SELECT * FROM ").append(TABLE_NAME_QUEUE).append(" Where KpiReport ='"+kpi+"'").append(" limit 1 ALLOW FILTERING;");
        final ResultSet reportQueue = session.execute(queryGetQueue.toString());
        System.out.println("Começou com query COM WATCH ="+ queryGetQueue);
        
       final Row reportRow = reportQueue.one();
        if(reportRow == null){
            System.out.println("I dont have work...");
            dbClient.close();
            return;
        }
        final String reportId= reportRow.getString("reportid");
        final Double watchVal= reportRow.getDouble("watch");
        
        System.out.println("OBTIVE O REPORT QUE DEMOROU  ="+ watchVal);

        //Remove da queue
        final StringBuilder queryDeleteQueue=  new StringBuilder("DELETE FROM ")
        .append(TABLE_NAME_QUEUE)
        .append(" Where reportid = '")
        .append(reportId)
        .append("';");

        System.out.println("Delete work item ="+ queryDeleteQueue);
        session.execute(queryDeleteQueue.toString());


        //Get Reportid from Queue
        final StringBuilder queryGetReport =  new StringBuilder("SELECT * FROM ")
        .append(TABLE_NAME_REPORT)
        .append(" Where reportid = '")
        .append(reportId)
        .append("'ALLOW FILTERING;");;

        System.out.println("get reports item ="+ queryGetReport);


        final ResultSet resultReports = session.execute(queryGetReport.toString());

        System.out.println("lets get the reports! ");
        final List<Report> reportList = new ArrayList<Report>();
        resultReports.forEach(r -> {
            reportList.add(new Report(
              r.getUUID("id"), 
              r.getString("KpiReport"),
              r.getVarint("value"), 
              new Timestamp(r.getTimestamp("timestamp").getTime())));
        });
        final int reportCount = reportList.size();

        System.out.println("lets show what we have!! -"+reportCount); 

        //Calculo KPI
        BigInteger  totalValue = new BigInteger("0");

        for (final Report r : reportList) 
        { 
            totalValue= totalValue.add(r.getValue());
        }
   
        totalValue=totalValue.divide(BigInteger.valueOf(reportCount));

        StringBuilder insertProfile = new StringBuilder("INSERT INTO ").append(TABLE_NAME_PROFILE)
            .append("(id, reportid, KPI, value, timestamp) ")
            .append("VALUES (uuid()").append(", '")
            .append(reportId).append("', '")
            .append(kpi).append("', ")
            .append(totalValue).append(", '")
            .append(new Timestamp(System.currentTimeMillis())).append("');");

            System.out.println("create profile ="+ insertProfile);

            session.execute(insertProfile.toString());

            
            Class.forName("com.mysql.jdbc.Driver"); 
            
            final Connection con=DriverManager.getConnection("jdbc:mysql://mysql:3306","root","password");  

            //Inserir Cache
            Statement stmt=con.createStatement();  
            watch.stop();

             insertProfile = new StringBuilder("INSERT INTO ").append("Parametros.ProfileCache")
            .append("(id, reportid, KPI, watch ,value, timestamp) ")
            .append("VALUES ('").append(reportId).append("', '")
            .append(reportId).append("', '")
            .append(kpi).append("', ")
            .append(watch.getTotalTimeSeconds()+watchVal).append(", ")
            .append(totalValue).append(", '")
            .append(new Timestamp(System.currentTimeMillis())).append("');");
            System.out.println("create profile cache="+ insertProfile);

            stmt.executeUpdate(insertProfile.toString());  
     
            //Inserir Notificacao
            stmt=con.createStatement();  
            final java.sql.ResultSet rs=stmt.executeQuery("SELECT valMax, valMin FROM Parametros.Parametro Where kpi ='"+kpi+"';");   
            if(rs.next()){
                valMax = BigInteger.valueOf(rs.getInt("valMax"));
                valMin = BigInteger.valueOf(rs.getInt("valMin"));

           }
           final StringBuilder errorNotif = new StringBuilder("INSERT INTO Parametros.Notification (Error,timestampError)VALUES ('");
           
           if(totalValue.compareTo(valMin)<0){
                errorNotif.append(O_KPI_E_INFERIOR_AO_VALOR_DE_REFERENCIA).append(valMin).append("',NOW());");
                System.out.println("Insert mas é INFERIOR ="+ insertProfile);

        
                stmt.executeUpdate(errorNotif.toString());  
           }



            if(totalValue.compareTo(valMax)>0){
                errorNotif.append(O_KPI_E_SUPERIOR_AO_VALOR_DE_REFERENCIA).append(valMax).append("',NOW());");
                System.out.println("Insert mas é MAXIMO ="+ insertProfile);
                stmt.executeUpdate(errorNotif.toString());  
            }

            
            con.close(); 
            dbClient.close();
    }
}
