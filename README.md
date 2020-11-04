# shopify-daily-spark

Introduction
============
This spark application is a data pipeline which extracts daily data from AWS S3 and load the result into a SQL instance. 

Variables 

*  Credentials :
>> \- AWS S3 credentials and bucket 

>> \- PostgreSQL credentials and db,table info 

* The extraction date via variable DATE_CONFIG.

Configuration of variables

1. can be configured in a config file [application.properties](https://github.com/xiaoxiao-24/shopify-daily-spark/blob/main/src/main/scala/shopify/application.properties.example):

2. can be set directly in [docker-compose.yml](https://github.com/xiaoxiao-24/shopify-daily-spark/blob/main/docker-compose.yml) 

3. can be set in [Dockerfile](https://github.com/xiaoxiao-24/shopify-daily-spark/blob/main/Dockerfile) and rebuild the image.


How to use
============

with spark-submit
----
Make a jar file from the source code and run with spark-submit:
> ./bin/spark-submit \
--packages org.apache.hadoop:hadoop-aws:3.2.0,org.postgresql:postgresql:9.4.1207,com.typesafe:config:1.2.0 \
--conf spark.hadoop.fs.s3a.endpoint=s3.amazonaws.com \
--conf spark.executor.extraJavaOptions=-Dcom.amazonaws.services.s3.enableV4=true \
--conf spark.driver.extraJavaOptions=-Dcom.amazonaws.services.s3.enableV4=true \
--class shopify.daily_shopify \
/jar/daily_shopify.jar

A [jar](https://spark-jar.s3.eu-north-1.amazonaws.com/daily_shopify.jar) file is accessible [here](https://spark-jar.s3.eu-north-1.amazonaws.com/daily_shopify.jar) on <s3://spark-jar/daily_shopify.jar>


with Docker
------------------
1. Make a [Dockerfile](https://github.com/xiaoxiao-24/shopify-daily-spark/blob/main/Dockerfile):

2. Build image:
> $ docker build -t shopify_daily_spark3.0_hadoop3.2:v1 .
>
3. Run docker image:
> $ docker run --rm -t -i --name 1sttry shopify_daily_spark3.0_hadoop3.2:v1
>
4. Edit [docker_compose.yml](https://github.com/xiaoxiao-24/shopify-daily-spark/blob/main/docker-compose.yml) and run
> $ docker-compose up
>

The docker image [shopify_daily spark pipeline](https://hub.docker.com/repository/docker/xiaoxiaorey/shopify_daily_spark3.0_hadoop3.2) is available on docker hub.

Schedule with airflow
---------------------
Use DockerOperator(or BashOperator).
Example [dag](https://github.com/xiaoxiao-24/shopify-daily-spark/blob/main/test_dag_shopify_spark.py) here is using DockerOperator.

