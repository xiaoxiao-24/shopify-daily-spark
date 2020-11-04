FROM ubuntu:latest
WORKDIR /jar
RUN apt-get update
RUN apt-get install -y openjdk-8-jdk
RUN apt-get update
RUN apt-get install git -y
RUN apt-get update
RUN apt-get install wget -y
RUN wget "https://downloads.apache.org/spark/spark-3.0.1/spark-3.0.1-bin-hadoop3.2.tgz"
RUN tar -xzvf spark-3.0.1-bin-hadoop3.2.tgz
RUN rm spark-3.0.1-bin-hadoop3.2.tgz
ADD ./out/artifacts/daily_shopify_jar/daily_shopify.jar /jar
ENV DATE_CONFIG="2019-04-02"
ENV AWS_ACCESS_KEY_ID=""
ENV AWS_SECRET_KEY=""
ENV AWS_BUCKET_NAME=""
ENV PG_DB_TYPE=""
ENV PG_DB_IP=""
ENV PG_DB_PORT=""
ENV PG_DB_NAME=""
ENV PG_DB_TBL=""
ENV PG_DB_USER=""
ENV PG_DB_PWD=""
CMD ./spark-3.0.1-bin-hadoop3.2/bin/spark-submit \
--packages org.apache.hadoop:hadoop-aws:3.2.0,org.postgresql:postgresql:9.4.1207,com.typesafe:config:1.2.0 \
--conf spark.hadoop.fs.s3a.endpoint=s3.amazonaws.com \
--conf spark.executor.extraJavaOptions=-Dcom.amazonaws.services.s3.enableV4=true \
--conf spark.driver.extraJavaOptions=-Dcom.amazonaws.services.s3.enableV4=true \
--class shopify.daily_shopify \
/jar/daily_shopify.jar