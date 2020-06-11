FROM pig4cloud/java:8-jre

MAINTAINER wangiegie@gmail.com

ENV TZ=Asia/Shanghai

RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN mkdir -p /uimp-daemon-elastic-job

WORKDIR /uimp-daemon-elastic-job

EXPOSE 5002

ADD ./target/uimp-daemon-elastic-job.jar ./

CMD sleep 120;java -Xms128m -Xmx256m -Djava.security.egd=file:/dev/./urandom -jar uimp-daemon-elastic-job.jar
