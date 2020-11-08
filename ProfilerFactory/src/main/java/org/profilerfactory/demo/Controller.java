package org.profilerfactory.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

//import java.net.InetAddress;
import java.net.UnknownHostException;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.batch.CronJob;
import io.fabric8.kubernetes.api.model.batch.CronJobBuilder;
import io.fabric8.kubernetes.client.*;

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
import org.profilerfactory.types.*;
import org.profilerfactory.connectors.*;

import java.sql.*;

@RestController
public class Controller {
    private static final String TABLE_NAME = "demodb.Profile";
    //private final String version = "2.1";
    private Session session;


    @Value("${spring.application.name}")
    private String appName;

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    //Ping
    @RequestMapping(value = "/profiler/{kpi}", method = GET)
    @ResponseBody
    public String profiler(@PathVariable("kpi") String kpi) throws UnknownHostException, ClassNotFoundException, SQLException {

        StringBuilder stringBuilder = new StringBuilder();
        //stringBuilder.append("Host: ").append(InetAddress.getLocalHost().getHostName()).append("<br/>");
        //stringBuilder.append("appName: ").append(appName).append("<br/>");
        //stringBuilder.append("IP: ").append(InetAddress.getLocalHost().getHostAddress()).append("<br/>");
        //stringBuilder.append("Version: ").append(version).append("<br/>");
        logger.info("Creating job.");

             //Obter Parametro
             int timeProfile=40;
             Class.forName("com.mysql.jdbc.Driver");  
             Connection con=DriverManager.getConnection("jdbc:mysql://mysql:3306","root","password");  
             Statement stmt=con.createStatement();  
             java.sql.ResultSet rs=stmt.executeQuery("SELECT timeProfile FROM Parametros.Parametro Where kpi ='"+kpi+"';");    
             if(rs.next()){
                 timeProfile = rs.getInt("timeProfile");
            }
            con.close(); 
            

        try (final KubernetesClient client = new DefaultKubernetesClient()) {
         final String namespace = "default";
         final CronJob job = new CronJobBuilder()
                 .withApiVersion("batch/v1beta1")
                 .withNewMetadata()
                 .withName("profiler"+kpi)
                 .withLabels(Collections.singletonMap("ProfileChronoJob"+kpi, "ProfileChronoJob"+kpi))
                 .withAnnotations(Collections.singletonMap("ProfileChronoJob"+kpi, "ProfileChronoJob"+kpi))
                 .endMetadata()
                 .withNewSpec()
                 .withSchedule("*/"+timeProfile+" * * * *")
                 .withNewJobTemplate()
                 .withNewSpec()
                 .withNewTemplate()
                 .withNewSpec()
                 .addNewContainer()
                 .withName("profiler"+kpi)
                 .withImage("dillaz/profiler")
                 .withCommand("/bin/sh","-c", "java $JAVA_OPTS -jar maven/*.jar "+kpi)
                 .endContainer()
                 .withRestartPolicy("Never")
                 .endSpec()
                 .endTemplate()
                 .endSpec()
                 .endJobTemplate()
                 .endSpec()
                 .build();

         logger.info("Creating job.");
         client.batch().cronjobs().inNamespace(namespace).create(job);

         logger.info("Job is created, waiting for result...");
         stringBuilder.append("CronJob Reporter criado para o KPI:").append(kpi).append(".<br/> O valor Default de periodicidade é ").append(timeProfile).append(". <br/>");

          final CountDownLatch watchLatch = new CountDownLatch(1);
         try (final Watch ignored = client.pods().inNamespace(namespace).withLabel("job-name").watch(new Watcher<Pod>() {
             @Override
             public void eventReceived(final Action action, Pod pod) {
                 if (pod.getStatus().getPhase().equals("Succeeded")) {
                     logger.info("Job is completed!");
                     logger.info(client.pods().inNamespace(namespace).withName(pod.getMetadata().getName()).getLog());
                     watchLatch.countDown();
                 }
             }

             @Override
             public void onClose(final KubernetesClientException e) {
                logger.info("END JOB CHRONO PROFILER");    
                 
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
         logger.error("Unable to create job", e);
     }
 
        
        return stringBuilder.toString();
    }
    

    @RequestMapping("profiler/change/{kpi}/{parameter}/{value}")
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



       if(parameter.equalsIgnoreCase("timeProfile")){
        try (final KubernetesClient client = new DefaultKubernetesClient()) {
        client.batch().cronjobs().inNamespace("default").withName("profiler"+kpi) .edit()
        .editSpec()
        .withSchedule("*/"+value+" * * * *")
        .endSpec() .done(); } catch (final KubernetesClientException e) {
        stringBuilder.append("Unable to change job - " + e.getStackTrace().toString());
        logger.error("Unable to change job - ", e); }
      }
    }


          //DE TESTE
          @GetMapping("/cassProfilePing/")
          public Profile profileDB(@RequestParam(value = "profileID", defaultValue = "Por favor introduza um valor")String profileID) {
              Profile profile=null;
              
              CassandraConnector client = new CassandraConnector();
              client.connect("cassandra", 9042);
              this.session = client.getSession();
               
              ResultSet result = session.execute("SELECT now() FROM system.local;");
                      
                  List<String> columnNames = 
                  result.getColumnDefinitions().asList().stream()
                  .map(cl -> cl.getName())
                  .collect(Collectors.toList());
      
      
                  profile= new Profile("test db: "+ columnNames.get(0));
      
              return profile;
          }
          
      
          @RequestMapping("/allprofiles/")
          @ResponseBody
          public List<Profile> getAllProfile() throws UnknownHostException {
              StringBuilder sb = 
              new StringBuilder("SELECT * FROM ").append(TABLE_NAME);
         
            String query = sb.toString();
            ResultSet rs = session.execute(query);
         
            List<Profile> profile = new ArrayList<Profile>();
         
            rs.forEach(r -> {
                profile.add(new Profile(
                  r.getUUID("id"), 
                  r.getString("reportid"),
                  r.getString("value"),  
                  r.getInt("bitrate")));
            });
            return profile;
          }  
      
}
  ///PROFILER
   
   // first ping
   // create profile from REPORTER
   // get profile
   // set kpi
   // get kpi value