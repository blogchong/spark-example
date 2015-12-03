package com.blogchong.spark.mllib.base

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.clustering.{LDA, DistributedLDAModel}
import org.apache.spark.mllib.linalg.Vectors

/**
  * Author:  blogchong
  * Blog:    www.blogchong.com
  * Mailbox: blogchong@163.com
  * Data:    2015/10/30
  * Describe:LDA主题模型基础实例
  */
object LdaArithmetic {
   def main(args: Array[String]) {

     // 屏蔽不必要的日志显示在终端上
     Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
     Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)

     // 设置运行环境
     val conf = new SparkConf().setAppName("LDA")
     val sc = new SparkContext(conf)

     val modelPath = "hdfs://192.168.5.200:9000/spark/mllib/result/lda/model"
     //doc-topic
     val modelPath2 = "hdfs://192.168.5.200:9000/spark/mllib/result/lda/model2"

     //1 加载数据，返回的数据格式为：documents: RDD[(Long, Vector)]
     // 其中：Long为文章ID，Vector为文章分词后的词向量
     // 可以读取指定目录下的数据，通过分词以及数据格式的转换，转换成RDD[(Long, Vector)]即可
     val data = sc.textFile("hdfs://192.168.5.200:9000/spark/mllib/data/sample_lda_data.txt", 1)
     val parsedData = data.map(s => Vectors.dense(s.split(' ').map(_.toDouble)))
     //通过唯一id为文档构建index
     val corpus = parsedData.zipWithIndex.map(_.swap).cache()

     //2 建立模型，设置训练参数，训练模型
     /**
      * k: 主题数，或者聚类中心数
      * DocConcentration：文章分布的超参数(Dirichlet分布的参数)，必需>1.0
      * TopicConcentration：主题分布的超参数(Dirichlet分布的参数)，必需>1.0
      * MaxIterations：迭代次数
      * setSeed：随机种子
      * CheckpointInterval：迭代计算时检查点的间隔
      * Optimizer：优化计算方法，目前支持"em", "online"
      */
     val ldaModel = new LDA().
       setK(3).
       setDocConcentration(5).
       setTopicConcentration(5).
       setMaxIterations(20).
       setSeed(0L).
       setCheckpointInterval(10).
       setOptimizer("em").
       run(corpus)

     //3 模型输出，模型参数输出，结果输出，输出的结果是是针对于每一个分类，对应的特征打分
     // Output topics. Each is a distribution over words (matching word count vectors)
     println("Learned topics (as distributions over vocab of " + ldaModel.vocabSize + " words):")
     val topics = ldaModel.topicsMatrix
     for (topic <- Range(0, 3)) {
       //print(topic + ":")
       val words = for (word <- Range(0, ldaModel.vocabSize)) { " " + topics(word, topic); }
       topic + ":" + words
//       println()
     }

     val dldaModel = ldaModel.asInstanceOf[DistributedLDAModel]
     val tmpLda = dldaModel.topTopicsPerDocument(3).map {
       f =>
         (f._1, f._2 zip f._3)
     }.map(f => s"${f._1} ${f._2.map(k => k._1 + ":" + k._2).mkString(" ")}").repartition(1).saveAsTextFile(modelPath2)

     //保存模型文件
     ldaModel.save(sc, modelPath)
     //再次使用
     //val sameModel = DistributedLDAModel.load(sc, modelPath)

     sc.stop()
   }
 }
