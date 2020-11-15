Projecto de demonstração da implementação da arquitetura proposta para a tese "Profiling KPIs in highly scalable networks"

![Grafana KPI](https://i.imgur.com/ago7CSA.png)
![Image of Yaktocat](https://octodex.github.com/images/yaktocat.png)
Dissertação no projecto overleaf: https://www.overleaf.com/read/yvsstzmdrszy

Interface grafica do projecto: http://34.72.14.38:3000/d/ilxKiE3Zk/parametros?orgId=1

Consola do cluster kubernetes na cloud: https://console.cloud.google.com/iam-admin/settings?project=kpiporfiletest&hl=pt-br

Estrutura do projecto :

1 -Base de dados : 
   Cassandra
   
    - Criar statefull set: 
          kubectl apply -f deploy/crds.yaml
          kubectl apply -f deploy/bundle.yaml
          kubectl apply -f examples/example-datacenter-minimal.yaml
   
    - Criação do modelo: 
          DbData.bat
    
    MySql
    
    - Criar statefull set:
           kubectl apply -f https://k8s.io/examples/application/mysql/mysql-pv.yaml
           kubectl apply -f https://k8s.io/examples/application/mysql/mysql-deployment.yaml
     
    - Cliente MySql:
            kubectl run -it --rm --image=mysql:5.6 --restart=Never mysql-client -- mysql -h mysql -ppassword
     
    - Crição do modelo:
       DbDataMysql.sql
     
     
2 -Objectos Kubernetes : 
    
    - Criação do deployer:
           kubectl apply -f ProfilerDeployer.yml
           kubectl apply -f ReporterDeployer.yml
     
    - Exposição do serviço:
           kubectl expose deployment reporterfactory --type=LoadBalancer --name=reporterfactory 
           kubectl expose deployment profilerfactory --type=LoadBalancer --name=profilerfactory
     
    - Atualização da imagem:
           buildProfilerFactory.bat
           buildReporterFactory.bat
           buildRaw.bat
           buildProfiler.bat
           buildReporter.bat
     
3 - User interface:
     Grafana
         kubectl create -f grafana-datasource-config.yaml
         kubectl create -f deployment.yaml
         kubectl expose deployment profilerfactory --type=LoadBalancer --name=grafana
         
     Passos de utilização:
        1 - Criar processo Raw
        2 - Criar CronJob Report e Profile dos KPIs disponiveis
        3 - Atualizar os parametros para cada KPI
     

