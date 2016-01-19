package com.blogchong.spark.mllib.base

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}

/**
 * Author:  blogchong
 * Blog:    www.blogchong.com
 * Mailbox: blogchong@163.com
 * Data:    2015/11/23
 * Describe: 特征抽取Word2Vec算法 基础实例
 */
object Word2Vec {

  def main(args: Array[String]) {

    // 屏蔽不必要的日志显示在终端上
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)

    // 设置运行环境
    val conf = new SparkConf().setAppName("ALS")
    val sc = new SparkContext(conf)

    val modelPath = "hdfs://192.168.5.200:9000/spark/mllib/result/feature/word2vec/model"

    val input = sc.textFile("hdfs://192.168.5.200:9000/spark/mllib/data/feature/word2vec2.txt")
      .map(line => line.split(" ").toSeq)

    val word2vec = new Word2Vec()

    val model = word2vec.fit(input)

    val synonyms = model.findSynonyms("as", 40)

    for((synonym, cosineSimilarity) <- synonyms) {
      println(s"输出[$synonym $cosineSimilarity]")
    }

    // Save and load model
    model.save(sc, modelPath)
    //val sameModel = Word2VecModel.load(sc, modelPath)

    sc.stop()
  }

}
