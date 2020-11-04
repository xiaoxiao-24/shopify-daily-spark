from airflow import DAG
from airflow.operators.docker_operator import DockerOperator
from datetime import datetime, timedelta

default_args = {
    'owner': 'xiaoxiao',
    'depends_on_past': False,
    'start_date': datetime(2019, 4, 1),
    'email': ['airflow@airflow.com'],
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
    'end_date': datetime(2019, 4, 8),
}

dag = DAG('xx_test_shopify_sparkjob_1st', default_args=default_args, schedule_interval='0 2 * * *')

t1 = DockerOperator(
                task_id='docker_command',
                image='xiaoxiaorey/shopify_daily_spark3.0_hadoop3.2:v2.1',
                api_version='auto',
                auto_remove=True,
                environment={ 
                    'DATE_CONFIG': "{{ ds }}" ,
                    'AWS_ACCESS_KEY_ID': "",
                    'AWS_SECRET_KEY': "",
                    'AWS_BUCKET_NAME': "",
                    'PG_DB_TYPE': "",
                    "PG_DB_IP": "",
                    'PG_DB_PORT': "",
                    'PG_DB_NAME': "",
                    'PG_DB_TBL': "",
                    'PG_DB_USER': "",
                    'PG_DB_PWD': ""
                },
                network_mode="bridge",
                dag = dag
)

t1

if __name__ == '__main__':
    dag.cli()

