FROM mysql:8.0.19

MAINTAINER uimp(wangiegie@gmail.com)

ENV TZ=Asia/Shanghai

RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY ./1schema.sql /docker-entrypoint-initdb.d

COPY ./2uimpx.sql /docker-entrypoint-initdb.d

COPY ./3uimpx_ac.sql /docker-entrypoint-initdb.d

COPY ./4uimpx_job.sql /docker-entrypoint-initdb.d

COPY ./5uimpx_mp.sql /docker-entrypoint-initdb.d

COPY ./6uimpx_config.sql /docker-entrypoint-initdb.d

COPY ./7uimpx_pay.sql /docker-entrypoint-initdb.d

COPY ./8uimpx_codegen.sql /docker-entrypoint-initdb.d