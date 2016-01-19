//package com.blogchong.spark.mllib.advance.weilianzhu
//
//import org.apache.spark.mllib.clustering.{DistributedLDAModel, LDA}
//import org.apache.spark.mllib.linalg.Vectors
//import org.apache.spark.{SparkConf, SparkContext}
//import scala.collection.mutable
//import scala.collection.mutable.ArrayBuffer
//
///**
// *
// */
//object LDAModelBuild {
//  def main(args: Array[String]) = {
//    val argsParser = new LDAModelBuildArgsParser
//
//    require(args.length >= 3, argsParser.getUsageMessage(null))
//
//    argsParser.parseArgs(args.toList)
//
//    val dataPath = argsParser.dataPath
//    val wordsPath = argsParser.wordsPath
//    val modelPath = argsParser.modelPath
//    val debug = argsParser.debug
//
//    val conf = new SparkConf().setAppName("LDAModelBuild")
//    if (debug) {
//      conf.setMaster("local[2]")
//    }
//    val sc = new SparkContext(conf)
//
//    //生成对应关系。要求为字典 <label>\t<word> 格式
//    val wordToLabelLocal = sc.textFile(wordsPath).map { f =>
//      val Array(label, word) = f.split("\t")
//      (word, label.toInt)
//    }.collect.toMap
//
//    //将字典广播出去
//    val keywordsDis = sc.broadcast(wordToLabelLocal.keys.toSet)
//    val wordToLabelDis = sc.broadcast(wordToLabelLocal)
//    val labelToWordToDis = sc.broadcast(wordToLabelLocal.map(f => (f._2, f._1)).toMap)
//
//
//    val dataPathCollections = dataPath.split(",")
//
//    var data = sc.textFile(if (debug) dataPathCollections(0) + "_sample" else dataPathCollections(0))
//
//    if (dataPathCollections.length > 1) {
//      dataPathCollections.takeRight(dataPathCollections.length - 1).foreach { k =>
//        data = data.union(sc.textFile(k))
//      }
//    }
//
//    //获取文档编号。每条内容的格式为<id>\t<word>\s<word>.... 其中id为文档的业务编号。我们会再生成一个
//    //LDA需要的Long类型编号，并且对应
//    val docs = data.zipWithIndex.map(_.swap).
//      map { f =>
//      val splitters = f._2.split("\t")
//      val id = splitters(0)
//      val sentence = splitters.takeRight(splitters.length - 1).mkString(" ")
//      Doc(f._1, id, sentence.split("\\s+").filter(word => keywordsDis.value.contains(word)))
//
//    }.filter(f => f.sentence.length > 0)
//
//
//    //获得训练集，仅仅使用词频作为权重。把文档转化为向量
//    val corpus = docs.map { f =>
//      val docVector = f.sentence.groupBy(f => f).map {
//        k =>
//          val wordLabel = wordToLabelDis.value(k._1)
//          (wordLabel.toInt, k._2.size.toDouble)
//      }.toSeq
//      (f.label, Vectors.sparse(wordToLabelDis.value.size, docVector))
//    }.repartition(20)
//
//    //主题模型训练
//    val topicSize = argsParser.topicSize
//    val ldaModel = new LDA().setK(topicSize).setMaxIterations(argsParser.maxIterations)
//      .run(corpus).asInstanceOf[DistributedLDAModel]
//
//    //保存模型
////    val saveTime = new DateTime().toString("yyyy-MM-dd-HH-mm-ss")
//    val saveTime = "yyyy-MM-dd-HH-mm-ss"
//
//    ldaModel.save(sc, modelPath + "/" + saveTime + "/model")
//
//    //存储文档数字编号和id的对应关系
//    docs.map(f => s"${f.label},${f.id}").saveAsTextFile(modelPath + "/" + saveTime + "/docLabelToId")
//
//
//
//    if (argsParser.saveVector) {
//      val hbaseTable = argsParser.hbaseTableName
//
//      val docLabelToId = docs.map(f => (f.label, f.id)).collect().toMap
//
//      //存储内容的主题分布
//      ldaModel.topTopicsPerDocument(topicSize).map { f =>
//        (f._1, f._2 zip f._3)
//      }.foreach { f =>
//        if (debug) {
//          println(s"${docLabelToId(f._1)},${f._2.map(k => k._1 + ":" + k._2).mkString(" ")}")
//        } else {
//          BaseSimpleHBaseClient.put(hbaseTable,
//            docLabelToId(f._1), f._2.map(k => k._1 + ":" + k._2).mkString(" "))
//        }
//      }
//
//      //存储词的主题分布
//      val mmm = new mutable.HashMap[String, mutable.ArrayBuffer[(Int, Double)]]()
//      val topicIndices = ldaModel.describeTopics(maxTermsPerTopic = wordToLabelLocal.size)
//      topicIndices.map { case (terms, termWeights) =>
//        terms.map(labelToWordToDis.value(_)).zip(termWeights)
//      }.zipWithIndex.map { case (topic, i) =>
//        i + " " + topic.map { case (term, weight) => s"$term:$weight" }.mkString(" ")
//      }.foreach { f =>
//        val line = f.asInstanceOf[String].split("\\s+")
//        val topic = line(0).toInt
//
//        line.takeRight(line.length - 1).map { f =>
//          val Array(t, w) = f.split(":")
//          val item = (topic, w.toDouble)
//          if (!mmm.contains(t)) {
//            mmm(t) = new ArrayBuffer[(Int, Double)]()
//          }
//          mmm(t) += item
//        }
//      }
//
//      mmm.foreach { f =>
//        val item = f._2.sortBy(k => k._1).map(k => s"${k._1}:${k._2}").mkString(" ")
//        if (debug) {
//          println(s"${f._1},${item}")
//        } else {
//          BaseSimpleHBaseClient.put(hbaseTable,
//            f._1, item
//          )
//        }
//      }
//
//    }
//
//    sc.stop()
//  }
//
//
//}
//
//case class Doc(label: Long, id: String, sentence: Array[String])
