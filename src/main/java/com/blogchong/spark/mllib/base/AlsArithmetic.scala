package com.blogchong.spark.mllib.base

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.recommendation.{ALS, Rating}

/**
 * Author:  blogchong
 * Blog:    www.blogchong.com
 * Mailbox: blogchong@163.com
 * Data:    2015/10/30
 * Describe:协同过滤中，基于模型的协同，最小二乘法ALS算法
 */
object AlsArithmetic {
  def main(args: Array[String]) {

    // 屏蔽不必要的日志显示在终端上
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)

    // 设置运行环境
    val conf = new SparkConf().setAppName("ALS")
    val sc = new SparkContext(conf)

    // 装载数据集，这是一个三列数据，用户ID：电影ID：用户对该电影的评分
    val data = sc.textFile("hdfs://192.168.5.200:9000/spark/mllib/data/als/test.data")
    //进行数组化操作
    val ratings = data.map(_.split(",") match { case Array(user, item, rate) =>
      Rating(user.toInt, item.toInt, rate.toDouble)
    })

    //进行ALS三个重要参数设置
    val rank = 10
    val numIterations = 10
    val lambda = 0.1

    // 模型文件训练
    val model = ALS.train(ratings, rank, numIterations, lambda)

    //评估模型
    val usersProducts = ratings.map{ case Rating(user, product, rate) =>
      (user, product)
    }

    //通过模型RDD进行效果预测
    val predictions =  model.predict(usersProducts).map {
      case Rating(user, product, rate) =>
        ((user, product), rate)
    }

    val ratesAndPreds = ratings.map { case Rating(user, product, rate) =>
      ((user, product), rate)
    }.join(predictions)

    //效果评判，通过计算预测评分的均方误差来衡量
    val MSE = ratesAndPreds.map { case ((user, product), (r1, r2)) =>
      val err = (r1 - r2)
      err * err
    }.mean()
    println("Mean Squared Error = " + MSE)

    //保存模型文件
    val modelPath = "hdfs://192.168.5.200:9000/spark/mllib/result/als"
    model.save(sc, modelPath)
    //使用已经生成的模型文件模型文件
    //val sameModel = MatrixFactorizationModel.load(sc, modelPath)

      sc.stop()
  }
}
