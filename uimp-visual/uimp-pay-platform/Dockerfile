FROM pig4cloud/java:8-jre

MAINTAINER wangiegie@gmail.com

ENV TZ=Asia/Shanghai

RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN mkdir -p /uimp-pay-platform

WORKDIR /uimp-pay-platform

EXPOSE 5010

ADD ./target/uimp-pay-platform.jar ./

CMD sleep 180;java -Xms128m -Xmx256m -Djava.security.egd=file:/dev/./urandom -jar uimp-pay-platform.jar
