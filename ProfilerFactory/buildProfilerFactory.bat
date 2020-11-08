CALL mvn package
CALL docker build . -t dillaz/profilerfactory:latest
CALL docker push dillaz/profilerfactory:latest