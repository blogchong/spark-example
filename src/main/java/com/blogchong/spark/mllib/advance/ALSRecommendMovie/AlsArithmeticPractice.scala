package com.blogchong.spark.mllib.advance.ALSRecommendMovie

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd._
import scala.io.Source

/**
 * Author:  blogchong
 * Blog:    www.blogchong.com
 * Mailbox: blogchong@163.com
 * Data:    2015/10/30
 * Describe:协同过滤中，基于模型的协同，最小二乘法ALS算法，贴合实践的实例
 */
object AlsArithmeticPractice {
  def main(args: Array[String]) {

    // 屏蔽不必要的日志显示在终端上
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)

    // 设置运行环境
    val conf = new SparkConf().setAppName("ALSPractice")
    val sc = new SparkContext(conf)

    val personalRatingsPath = "/root/spark/spark-1.4.0-bin-hadoop2.6/data/mllib/als2/personalRatings.txt"
    val moviesPath = "hdfs://192.168.5.200:9000/spark/mllib/data/als2/movies.dat"
    val ratingsPath = "hdfs://192.168.5.200:9000/spark/mllib/data/als2/ratings.dat"
    val userPath = "hdfs://192.168.5.200:9000/spark/mllib/data/als2/users.dat"
    val modelPath = "hdfs://192.168.5.200:9000/spark/mllib/result/als2/model"
    val outPath = "hdfs://192.168.5.200:9000/spark/mllib/result/als2/data/recommendations"

    // 装载用户评分数据，该评分由评分器生成，即文件personalRatings.txt
    val myRatings = loadRatings(personalRatingsPath)
    val personalRatingsData = sc.parallelize(myRatings, 1)

    //装载样本评分数据，最后一列Timestamp去除10余数作为key，Rating为值，即(Int, Ratings)
    //输出的结果是一个key-value集合，其中key为时间取余，value是Rating对象
    val ratings = sc.textFile(ratingsPath).map{
      line =>
        val fields = line.split("::")
        (fields(3).toLong %10, Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble))
    }

    //装载电影电影目录对照表(电影ID->电影标题),即输出是一个数组集合
    val movies = sc.textFile(moviesPath).map {
      line =>
        val fields = line.split("::")
        (fields(0).toInt, fields(1))
    }

    //统计有用户数量和电影数量以及用户对电影的评分数目
    val numRatings = ratings.count()
    val numUsers = ratings.map(_._2.user).distinct().count()
    val numMovies = ratings.map(_._2.product).distinct().count()

    println("==================样本数量===================")
    println("NumRatings: [" + numRatings + "]")
    println("NumUsers:   [" + numUsers + "]")
    println("NumMovies:  [" + numMovies + "]")

    //将样本评分表以Key值切分成3个部分,并且数据在计算的过程中会多次用到，所以存入cache
    //-训练(60%，并加入用户评分)
    //-校验(20%)
    //-测试(20%)
    val numPartions = 4
    //通过key(10的余数，均衡分布，所以x._1 < 6基本能够切分出大约60%的数据量)
    val training = ratings.filter(x => x._1 < 6).values
      .union(personalRatingsData).repartition(numPartions).persist()
    val validation = ratings.filter(x => x._1 >=6 && x._1 < 8).values
      .repartition(numPartions).persist()
    val test = ratings.filter(x => x._1 > 8).values.persist()

    //统计各部分的量
    val numTraining = training.count()
    val numValidation = validation.count()
    val numTest = test.count()
    //打印统计信息
    println("==================样本划分===================")
    println("NumTraining:     [" + numTraining + "]")
    println("NumValidation:   [" + numValidation + "]")
    println("NumTest:         [" + numTest + "]")

    //训练不同参数下的模型，并在校验集中验证，获取最佳参数下的模型
    val ranks = List(5, 8, 12, 15)
    val lambdas = List(0.1, 0.5, 5)
    val numIters = List(8, 10, 20)
    //最佳模型变量
    var bestModel: Option[MatrixFactorizationModel] = None
    //最佳校验均根方差
    var bestValidationRmse = Double.MaxValue
    var bestRank = 0
    var bestLambda = -1.0
    var bestNumIter = -1

    var count = 0
    //进行三层循环遍历，找最佳的Rmse值，对应的model
    for (rank <- ranks; lambda <- lambdas; numIter <- numIters) {
      val model = ALS.train(training, rank, numIter, lambda)
      //计算均根方差值，传入的是model以及校验数据
      val validationRmse = computeRmse(model, validation, numValidation)
      count += 1
      println("==============参数尝试次数:[" + count + "]=======================")
      println("RMSE(validation): [" + validationRmse + "]")
      println("rank:             [" + rank + "]")
      println("lambda:           [" + lambda + "]")
      println("numIter:          [" + numIter + "]")

      //选取最佳值，均方根误差越小越OK
      if (validationRmse < bestValidationRmse) {
        bestModel = Some(model)
        bestValidationRmse = validationRmse
        bestLambda = lambda
        bestRank = rank
        bestNumIter = numIter
      }
    }

    //至此，已经选择出均方根误差最小的模型，即最佳模型
    //用最佳模型进行测试集评分预测，并计算和实际评分之间的RMSE值
    val testRmse = computeRmse(bestModel.get, test, numTest)
    println("==============测试集预测==========================")
    println("rank:             [" + bestRank + "]")
    println("lambda:           [" + bestLambda + "]")
    println("numIter:          [" + bestNumIter + "]")
    println("Rmse:             [" + testRmse + "]")

    //创建一个基准衡量标准，并且用最好的模型进行比较
    //获取训练样本+预测样本的rating平均分
    val meanRating = training.union(validation).map(_.rating).mean()
    //计算标准差
    val baseLineRmse = math.sqrt(test.map(x => (meanRating - x.rating) * (meanRating - x.rating)).reduce(_+_)/numTest)
    //改进系数
    val improvement = (baseLineRmse - testRmse) / baseLineRmse * 100
    println("=============模型预测改进系数========================================================")
    println("The best model improves the baseline by " + "%1.2f".format(improvement) + "%.")

    //推荐前十部最感兴趣的电影,注意需要剔除该用户已经评分的电影，即去重
    val myRatedMovieIds = myRatings.map(_.product).toSet

    val candidates = movies.keys.filter(!myRatedMovieIds.contains(_))

    //为用户0推荐十部movies
    val candRDD: RDD[(Int, Int)] = candidates.map((0, _))
    val recommendations:RDD[Rating] = bestModel.get.predict(candRDD) //.collect.sortBy(_.rating).take(10)
    val recommendations_ = recommendations.collect().sortBy(-_.rating).take(10)
    var i = 1

    println("Movies recommended for you:")
    recommendations_.foreach {
      r =>
      println("%2d".format(i) + ": [" + r.product + "]")
      i += 1
    }

    //保存结果
    recommendations.sortBy(-_.rating).saveAsTextFile(outPath)
    //保存模型文件
    bestModel.get.save(sc, modelPath)
    //再次使用模型文件
    //val sameModel = MatrixFactorizationModel.load(sc, modelPath)

    sc.stop()
  }

  /** 装载用户评分文件 personalRatings.txt **/
  def loadRatings(path:String):Seq[Rating] = {
    val lines = Source.fromFile(path).getLines()
    val ratings = lines.map{
      line =>
        val fields = line.split("::")
        Rating(fields(0).toInt,fields(1).toInt,fields(2).toDouble)
    }.filter(_.rating > 0.0)
    if(ratings.isEmpty){
      sys.error("No ratings provided.")
      ratings.toSeq
    }else{
      ratings.toSeq
    }
  }

  //校验集预测数据和实际数据之间的均方根误差
  def computeRmse(model:MatrixFactorizationModel,data:RDD[Rating],n:Long):Double = {

    //调用model的predict预测方法，把预测数据初始化model中，并且生成预测rating
    val predictions:RDD[Rating] = model.predict((data.map(x => (x.user, x.product))))

    //通过join操作，把相同user-product的value合并成一个(double,double)元组，前者为预测值，后者为实际值
    val predictionsAndRatings = predictions.map{
      x => ((x.user, x.product), x.rating)
    }.join(data.map(x => ((x.user, x.product), x.rating))).values

    //均方根误差能够很好的反应出测量的精密度，对于偏离过大或者过小的测量值较为敏感
    //计算过程为观测值与真实值偏差的平方，除于观测次数n，然后再取平方根
    //reduce方法，执行的是值累加操作
    math.sqrt(predictionsAndRatings.map(x => (x._1 - x._2) * (x._1 - x._2)).reduce( _ + _ )/n)

  }

}
