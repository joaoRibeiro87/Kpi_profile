CALL mvn package
CALL docker build . -t dillaz/profiler:latest
CALL docker push dillaz/profiler:latest