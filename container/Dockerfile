FROM registry.access.redhat.com/ubi8/ubi
RUN yum -y install java-1.8.0-openjdk

RUN mkdir -p /opt/sling

COPY ../pantheon-slingstart/target/pantheon-slingstart-*.jar /opt/sling/pantheon.jar

WORKDIR /opt/sling/
EXPOSE 8080
EXPOSE 5005

ENV JAVA_OPTS -Xmx1024m -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
ENV SLING_OPS ''

CMD export COMMIT_HASH=$(cat /opt/sling/commit_hash); java $JAVA_OPTS -jar pantheon.jar $SLING_OPTS
