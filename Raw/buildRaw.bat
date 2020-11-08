CALL mvn package
CALL docker build . -t dillaz/raw:latest
CALL docker push dillaz/raw:latest