package org.reporterfactory.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.net.InetAddress;
import java.net.UnknownHostException;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.batch.CronJob;
import io.fabric8.kubernetes.api.model.batch.CronJobBuilder;
import io.fabric8.kubernetes.client.*;
import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import org.reporterfactory.types.*;
import org.reporterfactory.connectors.*;

@RestController
public class Controller {
    @Value("${spring.application.name}")

    private static final String TABLE_NAME = "demodb.Report";
    private final String version = "2.0";
    private Session session;

    private String appName;

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    public Controller() {

    }
    //Nota de todo: 
    
    //Raw 
    // 1) Adicionar valor raw para 3 KPIs  x


    // Report Factory
    // 1) Adicionar ao url o kpi a desplutar x
    // 2) Despultar o chrono do kpi x
    // 3) Change do chrono KPI x

    /// Report
    // Mudar a tabela para estar nome do valor e o valor x
    // adicionar à queue um valor para o KPI

    // Profile Factory
    // 1) Adicionar ao url o kpi a desplutar 
    // 2) Despultar o chrono do kpi
    // 3) Change do chrono KPI

     /// Profile
    // Mudar a tabela para estar nome do valor e o valor x
    // Obter da queue o report do kpi certo
    // Fazer o calculo para aquele kpi
    // Criar profile so do kpi certo
    // Verificar o boundry daquele kpi
    // Mudar mesagem de notif

    @RequestMapping(value = "/reporter/{kpi}", method = GET)
    @ResponseBody
    public String reporter(@PathVariable("kpi") String kpi) throws UnknownHostException, ClassNotFoundException, SQLException { 
        int timeReport=99;
        final StringBuilder stringBuilder = new StringBuilder();
        //stringBuilder.append("Host: ").append(InetAddress.getLocalHost().getHostName()).append("<br/>");
        //stringBuilder.append("appName: ").append(appName).append("<br/>");
        //stringBuilder.append("IP: ").append(InetAddress.getLocalHost().getHostAddress()).append("<br/>");
        //stringBuilder.append("Version: ").append(version).append("<br/>");


        // Obter parametro
            logger.info("Creating job.");
                Class.forName("com.mysql.jdbc.Driver");  
                Connection con=DriverManager.getConnection("jdbc:mysql://mysql:3306","root","password");  
                Statement stmt=con.createStatement();  
                java.sql.ResultSet rs=stmt.executeQuery("SELECT timeReport FROM Parametros.Parametro Where kpi ='"+kpi+"';");  
                if(rs.next()){
                timeReport = rs.getInt("timeReport");
            }
            con.close(); 

        // Criar Job
        try (final KubernetesClient client = new DefaultKubernetesClient()) {
            final String namespace = "default";
            final CronJob job = new CronJobBuilder().withApiVersion("batch/v1beta1").withNewMetadata()
                    .withName("reporter"+kpi)
                    .withLabels(Collections.singletonMap("ReportChronoJob"+kpi, "ReportChronoJob"+kpi))
                    .withAnnotations(Collections
                    .singletonMap("ReportChronoJob"+kpi, "ReportChronoJob"+kpi))
                    .endMetadata()
                    .withNewSpec()
                    .withSchedule("*/"+timeReport+" * * * *")
                    .withNewJobTemplate()
                    .withNewSpec()
                    .withNewTemplate()
                    .withNewSpec()
                    .addNewContainer()
                    .withName("reporter"+kpi)
                    .withImage("dillaz/reporter")
                    .withCommand("/bin/sh","-c", "java $JAVA_OPTS -jar maven/*.jar "+kpi)
                    .endContainer()
                    .withRestartPolicy("Never").endSpec().endTemplate().endSpec().endJobTemplate().endSpec().build();

            logger.info("Creating job.");
            client.batch().cronjobs().inNamespace(namespace).create(job);
            logger.info("Job is created, waiting for result...");
            stringBuilder.append("CronJob Reporter criado para o KPI:").append(kpi).append(". O valor Default de periodicidade é ").append(timeReport).append(". <br/>");

            final CountDownLatch watchLatch = new CountDownLatch(1);
            try (final Watch ignored = client.pods().inNamespace(namespace).withLabel("job-name")
                    .watch(new Watcher<Pod>() {
                        @Override
                        public void eventReceived(final Action action, Pod pod) {
                            if (pod.getStatus().getPhase().equals("Succeeded")) {
                                logger.info("Job is completed!");
                                logger.info(client.pods().inNamespace(namespace).withName(pod.getMetadata().getName())
                                        .getLog());
                                watchLatch.countDown();
                            }
                        }

                        @Override
                        public void onClose(final KubernetesClientException e) {
                            logger.info("END JOB CHRONO REPORTER");

                        }
                    })) {
                watchLatch.await(2, TimeUnit.MINUTES);
            } catch (final KubernetesClientException | InterruptedException e) {
                //stringBuilder.append(e.getStackTrace().toString());
                stringBuilder.append("Erro a criar o worker");
                logger.error("Could not watch pod", e);
            }
        } catch (final KubernetesClientException e) {
            //stringBuilder.append(e.getStackTrace().toString());
            stringBuilder.append("Erro a criar o Job. O job  para o KPI "+kpi+" já existe");
        }

        return stringBuilder.toString();
    }

    @RequestMapping("/raw/")
    @ResponseBody
    public String raw() throws UnknownHostException {
        StringBuilder stringBuilder = new StringBuilder();
        logger.info("Raw Ligado!");

        try (final KubernetesClient client = new DefaultKubernetesClient()) {
            final String namespace = "default";
            final CronJob job = new CronJobBuilder().withApiVersion("batch/v1beta1").withNewMetadata().withName("raw")
                    .withLabels(Collections.singletonMap("raw", "raw"))
                    .withAnnotations(Collections.singletonMap("raw", "raw")).endMetadata().withNewSpec()
                    .withSchedule("*/99 * * * *").withNewJobTemplate().withNewSpec().withNewTemplate().withNewSpec()
                    .addNewContainer().withName("raw").withImage("dillaz/raw").endContainer().withRestartPolicy("Never")
                    .endSpec().endTemplate().endSpec().endJobTemplate().endSpec().build();

            logger.info("Creating job.");
            client.batch().cronjobs().inNamespace(namespace).create(job);
            stringBuilder.append("Raw Ligado!").append(". O valor Default de criação é 99. <br/>");
            logger.info("Job is created, waiting for result...");

            final CountDownLatch watchLatch = new CountDownLatch(1);
            try (final Watch ignored = client.pods().inNamespace(namespace).withLabel("job-name")
                    .watch(new Watcher<Pod>() {
                        @Override
                        public void eventReceived(final Action action, Pod pod) {
                            if (pod.getStatus().getPhase().equals("Succeeded")) {
                                logger.info("Job is completed!");
                                logger.info(client.pods().inNamespace(namespace).withName(pod.getMetadata().getName())
                                        .getLog());
                                watchLatch.countDown();
                            }
                        }

                        @Override
                        public void onClose(final KubernetesClientException e) {
                            logger.info("END JOB CHRONO RAW");

                        }
                    })) {
                watchLatch.await(2, TimeUnit.MINUTES);
            } catch (final KubernetesClientException | InterruptedException e) {
                stringBuilder.append("Erro a criar o worker!");
                logger.error("Could not watch pod", e);
            }
        } catch (final KubernetesClientException e) {
            //stringBuilder.append(e.getStackTrace().toString());
            stringBuilder.append("Job Raw já existe!");
            logger.error("Unable to create job", e);
        }

        return stringBuilder.toString();
    }

    @RequestMapping(value = "/reporter/change/{kpi}/{parameter}/{value}", method = GET)
    @ResponseBody
    public void change(@PathVariable("kpi") String kpi, @PathVariable("parameter") String parameter, @PathVariable("value") Integer value) throws UnknownHostException, ClassNotFoundException, SQLException {
  
          final StringBuilder stringBuilder = new StringBuilder();

          System.out.println("##################### KPI " + kpi);
          System.out.println("##################### Parameter " + parameter);
          System.out.println("##################### Value " + value);

          Class.forName("com.mysql.jdbc.Driver");  
          Connection con=DriverManager.getConnection("jdbc:mysql://mysql:3306","root","password");  
          Statement stmt=con.createStatement();  
          stmt.executeUpdate("UPDATE Parametros.Parametro SET "+parameter+" = "+value+" Where kpi ='"+kpi+"';");  
        
         con.close(); 
    
        if(parameter.equalsIgnoreCase("timeReport")){
          try (final KubernetesClient client = new DefaultKubernetesClient()) {
          client.batch().cronjobs().inNamespace("default").withName("reporter"+kpi) .edit()
          .editSpec()
          .withSchedule("*/"+value+" * * * *")
          .endSpec() .done(); } catch (final KubernetesClientException e) {
          stringBuilder.append(e.getStackTrace().toString());
          logger.error("Unable to change job - ", e); }
        }
           

    }

    //DE TESTE
    @GetMapping("/sqlReportPing/")
    public Report sqlReportPing() throws UnknownHostException, ClassNotFoundException, SQLException {
    Report report=null;
    Class.forName("com.mysql.jdbc.Driver");  
    Connection con=DriverManager.getConnection("jdbc:mysql://mysql:3306","root","password");  
    Statement stmt=con.createStatement();  
    java.sql.ResultSet rs=stmt.executeQuery("SELECT CURRENT_USER();");  
    while(rs.next())  
 
    report= new Report("test db sql: "+ rs.getString(1));

    con.close();  

    return report;
    }

      //DE TESTE
    @GetMapping("/cassReportPing/")
    public Report cassReportPing() throws UnknownHostException{
        Report report=null;
        
        CassandraConnector dbClient;
        dbClient = new CassandraConnector();
        this.session= dbClient.createConnection();

        ResultSet result = session.execute("SELECT now() FROM system.local;");
                
            List<String> columnNames = 
            result.getColumnDefinitions().asList().stream()
            .map(cl -> cl.getName())
            .collect(Collectors.toList());

            report= new Report("test db: "+ columnNames.get(0));


         return report;
    }
   
      //Retorna Json de todos os reports
    @RequestMapping("/allreports/")
    @ResponseBody
    public List<Report> getAllReport() throws UnknownHostException {
        String query;
        StringBuilder sb=  new StringBuilder("SELECT * FROM ").append(TABLE_NAME);
        
        CassandraConnector dbClient;
        dbClient = new CassandraConnector();
        session= dbClient.createConnection();
        
        logger.info("---Show all values---");

        query = sb.toString();
        logger.info("I will run -> " + query);


      ResultSet rs = session.execute(query);
   
      List<Report> report = new ArrayList<Report>();
        
      logger.info("Lets show results!");

      rs.forEach(r -> {
          report.add(new Report(
            r.getUUID("id"), 
            r.getString("deviceName"),  
            r.getString("deviceType"), 
            r.getVarint("bitrate"), 
            new Timestamp(r.getTimestamp("timestamp").getTime())));
      });

      return report; 
    }

}
