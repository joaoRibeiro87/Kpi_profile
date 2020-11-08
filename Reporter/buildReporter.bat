CALL mvn package
CALL docker build . -t dillaz/reporter:latest
CALL docker push dillaz/reporter:latest