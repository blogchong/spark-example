package com.blogchong.spark.mllib.advance.LdaExtractTopics.Check

import org.apache.spark.mllib.clustering.{LocalLDAModel}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.{SparkConf, SparkContext}
import scala.collection.JavaConversions._
import java.util.Date
import com.blogchong.util.NewTime
import java.util

/**
 * Author:  blogchong
 * Blog:    www.blogchong.com
 * Mailbox: blogchong@163.com
 * Data:    2015/10/23
 * Describe: LDA主题词 新文档主题推测，然后进行校验
 */

object PredictsDocTopics {
  def main(args: Array[String]) = {

    args.map(f => println(f))

    val argsParser = new PredictsDocTopicsArgsParser

    require(args.length >= 3, argsParser.getUsageMessage(null))

    argsParser.parseArgs(args.toList)

    val dataPath = argsParser.dataPath
    val modelPath = argsParser.modelPath
    val wordsPath = argsParser.wordsPath
    val maxWordsTopic = argsParser.topicSize
    val topicsPath = argsParser.topicsPath

    val conf = new SparkConf().setAppName("PredictsDocTopics")
    val sc = new SparkContext(conf)

    //生成对应关系。要求为字典 <label>\t<word> 格式
    val wordToLabelLocal = sc.textFile(wordsPath).map {
      f =>
        val Array(label, word) = f.split("\t")
        (word, label.toInt)
    }.collect.toMap

    //生成对应关系。要求为字典 <label>\t<word> 格式
    val wordToLabelLocal2 = sc.textFile(wordsPath).map {
      f =>
        val Array(label, word) = f.split("\t")
        (label.toInt, word)
    }.collect.toMap

    //将字典广播出去
    val keywordsDis = sc.broadcast(wordToLabelLocal.keys.toSet)
    val wordToLabelDis = sc.broadcast(wordToLabelLocal)
    val wordToLabelDis2 = sc.broadcast(wordToLabelLocal2)

    val dataPathCollections = dataPath.split(",")

    var data = sc.textFile(dataPathCollections(0))

    if (dataPathCollections.length > 1) {
      dataPathCollections.takeRight(dataPathCollections.length - 1).foreach {
        k =>
          data = data.union(sc.textFile(k))
      }
    }

    //存储docs中id与自动index，对应起来
    val mapIdsIndex = new util.HashMap[Long, String]()

    //获取文档编号。每条内容的格式为<id>\t<word>\s<word>.... 其中id为文档的业务编号。我们会再生成一个
    //LDA需要的Long类型编号
    val docs = data.zipWithIndex.map(_.swap).
      map {
      f =>
        val splitters = f._2.split("\t")
        val id = splitters(0)
        val sentence = splitters.takeRight(splitters.length - 1).mkString(" ")
        Doc(f._1, id, sentence.split("\\s+").filter(word => keywordsDis.value.contains(word)))

    }.filter(f => f.sentence.length > 0)

    docs.map{
      f=>
      mapIdsIndex.put(f.label, f.id)
    }

    //获得训练集，仅仅使用词频作为权重。把文档转化为向量
    val corpus = docs.map {
      f =>
        val docVector = f.sentence.groupBy(f => f).map {
          k =>
            val wordLabel = wordToLabelDis.value(k._1)
            (wordLabel.toInt, k._2.size.toDouble)
        }.toSeq
        (f.label, Vectors.sparse(50269, docVector))
    }.repartition(20)

    //加载主题模型
    val ldaModel = LocalLDAModel.load(sc, modelPath)
    val predictsTopics = ldaModel.topicDistributions(corpus)

    //预测新文档的主题分布，并且保存下来
    val dateDate = new Date
    val saveTime = NewTime.dateToString(dateDate, NewTime.`type`)
    predictsTopics.map{
      f =>
        val docIndex = f._1
        val wordArray = ldaModel.topics.multiply(f._2).toArray
        val wordRdd = wordArray.zipWithIndex.sortBy(-_._1).take(maxWordsTopic)
        val topWords = wordRdd.map {
          case (weight, index) =>
            s"${wordToLabelDis2.value(index.toInt)}:${weight}"
        }
        s"${mapIdsIndex.get(docIndex)}\t${topWords.mkString(" ")}"
    }.saveAsTextFile(topicsPath + "/" + saveTime + "/predictsTopics")

    sc.stop()
  }

}

case class Doc(label: Long, id: String, sentence: Array[String])
