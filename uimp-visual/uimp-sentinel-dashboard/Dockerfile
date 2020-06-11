FROM pig4cloud/java:8-jre

MAINTAINER wangiegie@gmail.com

ENV TZ=Asia/Shanghai

RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN mkdir -p /uimp-sentinel-dashboard

WORKDIR /uimp-sentinel-dashboard

EXPOSE 5020

ADD ./target/uimp-sentinel-dashboard.jar ./

CMD sleep 120;java -Xms128m -Xmx256m -Djava.security.egd=file:/dev/./urandom -jar uimp-sentinel-dashboard.jar
