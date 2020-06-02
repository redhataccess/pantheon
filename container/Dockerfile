FROM registry.access.redhat.com/ubi8/ubi
RUN yum -y install java-1.8.0-openjdk

ENV MONGO_DB="sling"
ENV REPOSITORY="sling/repository"

RUN mkdir -p /opt/pantheon-karaf-dist
RUN mkdir /tmp/archive
WORKDIR /tmp/archive

COPY ../pantheon-karaf-dist/target/pantheon-karaf-dist-*.tar.gz /tmp/archive/pantheon-karaf-dist.tar.gz

RUN tar --strip-components=1 -C /opt/pantheon-karaf-dist -xzf pantheon-karaf-dist.tar.gz; \
    rm -rf /tmp/*

RUN chmod 755 /opt; 
    #sed -i "21s/out/stdout/" /opt/pantheon-karaf-dist/etc/org.ops4j.pax.logging.cfg;

EXPOSE 1099 8181 44444

CMD  ["/opt/pantheon-karaf-dist/bin/karaf", "run"]