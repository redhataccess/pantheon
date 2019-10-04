#Build stage
FROM registry.access.redhat.com/ubi8/ubi AS builder
RUN yum -v -y install maven java-1.8.0-openjdk-devel
RUN mkdir -p /build && chmod -R 777 /build
WORKDIR /build
ADD ./containerInstall.sh ../mvnw ../mvnw.cmd ../pom.xml /build/
COPY ../frontend /build/frontend
COPY ../src /build/src

RUN mvn clean package
RUN mkdir -p /jar && chmod -R 777 /jar
RUN cp /build/target/pantheon-1.0-SNAPSHOT.jar /jar/


#Final image
FROM registry.access.redhat.com/ubi8/ubi
RUN yum -y install java-1.8.0-openjdk wget

RUN mkdir -p /opt/sling
RUN wget https://repo1.maven.org/maven2/org/apache/sling/org.apache.sling.starter/11/org.apache.sling.starter-11.jar -O /opt/sling/org.apache.sling.starter.jar

WORKDIR /opt/sling/
EXPOSE 8080

RUN mkdir -p /install && chmod -R 777 /install

ENV JAVA_OPTS -Xmx1024m
ENV SLING_OPTS '-Dsling.fileinstall.dir=/install'

COPY --from=builder /build/containerInstall.sh /opt/sling/
RUN chmod +x /opt/sling/containerInstall.sh
COPY --from=builder /jar/pantheon-1.0-SNAPSHOT.jar /

CMD exec ./containerInstall.sh & java $JAVA_OPTS -jar org.apache.sling.starter.jar $SLING_OPTS