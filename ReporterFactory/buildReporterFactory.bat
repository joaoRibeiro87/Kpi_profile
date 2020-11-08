CALL mvn package
CALL docker build . -t dillaz/reporterfactory:latest
CALL docker push dillaz/reporterfactory:latest