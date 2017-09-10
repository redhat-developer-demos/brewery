FROM jboss/base-jdk:8

MAINTAINER "Kamesh Sampath<kamesh.sampath@hotmail.com>"

USER root

ENV ACTIVEMQ_ARTEMIS_VERSION 2.1.0
ENV ARTEMIS_HOME /opt/apache-artemis-2.1.0
ENV BROKERS_HOME /opt/brokers
ENV BROKER_NAME mybroker
ENV ARTEMIS_USER jboss
ENV ARTEMIS_PASSWORD jboss

RUN  yum -y install wget curl \
     && yum -y clean all \
     && cd /opt \
     && wget -q https://repository.apache.org/content/repositories/releases/org/apache/activemq/apache-artemis/${ACTIVEMQ_ARTEMIS_VERSION}/apache-artemis-${ACTIVEMQ_ARTEMIS_VERSION}-bin.tar.gz && \
     wget -q https://repository.apache.org/content/repositories/releases/org/apache/activemq/apache-artemis/${ACTIVEMQ_ARTEMIS_VERSION}/apache-artemis-${ACTIVEMQ_ARTEMIS_VERSION}-bin.tar.gz.asc && \
     wget -q http://apache.org/dist/activemq/KEYS && \
     gpg --import KEYS && \
     gpg apache-artemis-${ACTIVEMQ_ARTEMIS_VERSION}-bin.tar.gz.asc && \
     tar xfz apache-artemis-${ACTIVEMQ_ARTEMIS_VERSION}-bin.tar.gz && \
     mkdir -p /opt/brokers &&  \
     ${ARTEMIS_HOME}/bin/artemis create ${BROKERS_HOME}/${BROKER_NAME} \
       --home ${ARTEMIS_HOME} \
       --user ${ARTEMIS_USER} \
       --password ${ARTEMIS_PASSWORD} \
       --allow-anonymous

COPY run-broker.sh container-limits java-default-options ${BROKERS_HOME}/


RUN chmod 755 ${BROKERS_HOME}/run-broker.sh  ${BROKERS_HOME}/java-default-options ${BROKERS_HOME}/container-limits \
    && chown -R jboss ${BROKERS_HOME} \
    && usermod -g root -G `id -g jboss` jboss \
    && chmod -R "g+rwX" ${BROKERS_HOME} \
    && chown -R jboss:root ${BROKERS_HOME}


EXPOSE 8161 61616 5445 5672 1883 61613

#TODO volumes

USER jboss

CMD [ "/bin/sh","-c", "${BROKERS_HOME}/run-broker.sh" ]