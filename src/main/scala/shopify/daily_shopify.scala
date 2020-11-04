package shopify

import java.io.FileNotFoundException
import java.util.Properties

import com.amazonaws.services.s3.model.AmazonS3Exception
import org.apache.spark.sql.SparkSession

import scala.io.Source


object daily_shopify {
  def main(args: Array[String]): Unit = {

    val spark: SparkSession = SparkSession.builder().master("local[1]").appName("Shopify_daily").getOrCreate()

    // ----------------------------------------
    // get credentials and info from conf file
    // ----------------------------------------
    val url = getClass.getResource("application.properties")
    val properties: Properties = new Properties()

    if (url != null) {
      val source = Source.fromURL(url)
      properties.load(source.bufferedReader())
    }
    else {
      throw new FileNotFoundException("Properties file cannot be loaded")
    }

    // date of extraction
    var DATE_CONFIG = ""
    if (System.getenv("DATE_CONFIG") != "") {
      DATE_CONFIG = System.getenv("DATE_CONFIG")
    } else {
      DATE_CONFIG = properties.getProperty("DATE_CONFIG")
    }

    // source: s3
    var accessKeyId = ""
    if (System.getenv("AWS_ACCESS_KEY_ID") != "") {
      accessKeyId = System.getenv("AWS_ACCESS_KEY_ID")
    } else {
      accessKeyId = properties.getProperty("AWS_ACCESS_KEY_ID")
    }

    var secretAccessKey = ""
    if (System.getenv("AWS_SECRET_KEY") != "") {
      secretAccessKey = System.getenv("AWS_SECRET_KEY")
    } else {
      secretAccessKey = properties.getProperty("AWS_SECRET_KEY")
    }

    // target: postgres
    var db_type = ""
    if (System.getenv("PG_DB_TYPE") != "") {
      db_type = System.getenv("PG_DB_TYPE")
    } else {
      db_type = properties.getProperty("PG_DB_TYPE")
    }

    var db_ip = ""
    if (System.getenv("PG_DB_IP") != "") {
      db_ip = System.getenv("PG_DB_IP")
    } else {
      db_ip = properties.getProperty("PG_DB_IP")
    }

    var db_port = ""
    if (System.getenv("PG_DB_PORT")!= "") {
      db_port = System.getenv("PG_DB_PORT")
    } else {
      db_port = properties.getProperty("PG_DB_PORT")
    }

    var db_name = ""
    if (System.getenv("PG_DB_NAME")!= "") {
      db_name = System.getenv("PG_DB_NAME")
    } else {
      db_name = properties.getProperty("PG_DB_NAME")
    }

    var db_tbl = ""
    if (System.getenv("PG_DB_TBL") != "") {
      db_tbl = System.getenv("PG_DB_TBL")
    } else {
      db_tbl = properties.getProperty("PG_DB_TBL")
    }

    var db_user = ""
    if (System.getenv("PG_DB_USER") != "") {
      db_user = System.getenv("PG_DB_USER")
    } else {
      db_user = properties.getProperty("PG_DB_USER")
    }

    var db_pwd = ""
    if (System.getenv("PG_DB_PWD") != "") {
      db_pwd = System.getenv("PG_DB_PWD")
    } else {
      db_pwd = properties.getProperty("PG_DB_PWD")
    }

    val db_url = "jdbc:" + db_type + "://" + db_ip + ":" + db_port + "/" + db_name

    spark.sparkContext.hadoopConfiguration.set("fs.s3a.access.key", accessKeyId)
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.secret.key", secretAccessKey)
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.endpoint", "s3.amazonaws.com")

    val path_extract = "s3a://" + properties.getProperty("AWS_BUCKET_NAME") + "/" + DATE_CONFIG + ".csv"

    // --------------------------------------------------------
    // Extract data from S3, transform and load into postgres
    // --------------------------------------------------------

    // check if data of this date already exist
    val query = s"select * from $db_tbl where export_date = '$DATE_CONFIG'"
    val df_shopify_daily = spark.read
      .format("jdbc")
      .option("driver", "org.postgresql.Driver")
      .option("url", db_url)
      .option("dbtable", s"($query) as t")
      .option("user", db_user)
      .option("password", db_pwd)
      .load()

    if (df_shopify_daily.count() == 0) {

      print("\nExtracting s3 data\n")
      try {
        // read data
        print("\nBegin getting source s3 data\n")
        val df_algolia = spark.read.option("header", true).csv(path_extract)
        print("\nSource data frame: \n")
        df_algolia.show(false)

        // filter null
        val df_2 = df_algolia.filter(df_algolia("application_id").isNotNull)
        print("\nData frame removed null rows in column 'application_id': \n")
        //df_2.show(false)

        // create new column base another one
        import org.apache.spark.sql.functions._
        val df_3 = df_2.withColumn("has_specific_prefix", when(col("index_prefix") === "shopify_", false).otherwise(true))
        print("\nData frame added new column 'has_specific_prefix': \n")
        //df_3.show(false)

        // cast datatype
        import org.apache.spark.sql.types._
        val df_result = df_3.withColumn("id", col("id").cast(StringType))
          .withColumn("shop_domain", col("shop_domain").cast(StringType))
          .withColumn("application_id", col("application_id").cast(StringType))
          .withColumn("autocomplete_enabled", col("autocomplete_enabled").cast(BooleanType))
          .withColumn("user_created_at_least_one_qr", col("user_created_at_least_one_qr").cast(BooleanType))
          .withColumn("nbr_merchandised_queries", col("nbr_merchandised_queries").cast(IntegerType))
          .withColumn("nbrs_pinned_items", col("nbrs_pinned_items").cast(StringType))
          .withColumn("showing_logo", col("showing_logo").cast(BooleanType))
          .withColumn("has_changed_sort_orders", col("has_changed_sort_orders").cast(BooleanType))
          .withColumn("analytics_enabled", col("analytics_enabled").cast(BooleanType))
          .withColumn("use_metafields", col("use_metafields").cast(BooleanType))
          .withColumn("nbr_metafields", col("nbr_metafields").cast(FloatType))
          .withColumn("use_default_colors", col("use_default_colors").cast(BooleanType))
          .withColumn("show_products", col("show_products").cast(BooleanType))
          .withColumn("instant_search_enabled", col("instant_search_enabled").cast(BooleanType))
          .withColumn("instant_search_enabled_on_collection", col("instant_search_enabled_on_collection").cast(BooleanType))
          .withColumn("only_using_faceting_on_collection", col("only_using_faceting_on_collection").cast(BooleanType))
          .withColumn("use_merchandising_for_collection", col("use_merchandising_for_collection").cast(BooleanType))
          .withColumn("index_prefix", col("index_prefix").cast(StringType))
          .withColumn("indexing_paused", col("indexing_paused").cast(BooleanType))
          .withColumn("install_channel", col("install_channel").cast(StringType))
          .withColumn("export_date", col("export_date").cast(StringType))
          .withColumn("has_specific_prefix", col("has_specific_prefix").cast(BooleanType))

        print("\nFinal data frame: \n")
        df_result.show(false)

        // load into postgres
        df_result.write
          .format("jdbc")
          .mode("append")
          .option("driver", "org.postgresql.Driver")
          .option("url", db_url) //"jdbc:postgresql://localhost:5432/algolia"
          .option("dbtable", db_tbl)
          .option("user", db_user)
          .option("password", db_pwd)
          .save()
        print("Data of this date " + DATE_CONFIG + " have been inserted.\n")

      } catch {
        case a: AmazonS3Exception => print("\n!!! Got AmazonS3Exception. Problemes in source.\n")
        case _: Throwable => print("\n!!! Got an exception.\n")
      } finally {
        print("\n!!! Stop pipeline.\n")
        spark.stop()
      }
    } else {
      print("\n!!! Data of this date " + DATE_CONFIG + " already exist.\n")
      print("\n!!! There is " + df_shopify_daily.count() + " rows in target table of date " + DATE_CONFIG + ".\n")
    }

    spark.stop()
  }
}
