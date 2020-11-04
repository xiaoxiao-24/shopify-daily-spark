# shopify-daily-spark

Introduction
------------
This spark application is a data pipeline which extracts daily data from AWS S3 and load the result into a SQL instance. 

It can be run with Docker. 

The [source code](https://github.com/xiaoxiao-24/shopify-daily-spark/blob/main/src/main/scala/shopify/daily_shopify.scala) is written in Spark scala. 
It gets the extraction date from the env viariable DATE_CONFIG.

Variables 

*  Credentials :
>> \- Set AWS S3 credentials and bucket 

>> \- Set PostgreSQL credentials and db,table info 

* The extraction date via the env variable DATE_CONFIG.

Configuration of variables

1. can be configured in a config file [application.properties](https://github.com/xiaoxiao-24/shopify-daily-spark/blob/main/src/main/scala/shopify/application.properties.example):

2. can be set directly in [docker-compose.yml](https://github.com/xiaoxiao-24/shopify-daily-spark/blob/main/docker-compose.yml) 

3. can be set in [Dockerfile](https://github.com/xiaoxiao-24/shopify-daily-spark/blob/main/Dockerfile) and rebuild the image.

Build docker image
------------------
Edit [Dockerfile](https://github.com/xiaoxiao-24/shopify-daily-spark/blob/main/Dockerfile) and build image:
> $ docker build -t shopify_daily_spark3.0_hadoop3.2:v1 .
>

Run with docker
---------------
Change the extraction date to the one you what and run:
> $ docker run --rm -t -i --name 1sttry190401 shopify_daily_spark3.0_hadoop3.2:v1
>

Run with docker-compose
-----------------------
Edit [docker_compose.yml](https://github.com/xiaoxiao-24/shopify-daily-spark/blob/main/docker-compose.yml) and run
> $ docker-compose up
>

Run with airflow
----------------
Set variables in dag and run with a dag file 

The docker image [shopify_daily spark pipeline](https://hub.docker.com/repository/docker/xiaoxiaorey/shopify_daily_spark3.0_hadoop3.2) is available on docker hub.
