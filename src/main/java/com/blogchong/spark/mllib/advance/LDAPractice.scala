//package com.blogchong.spark.mllib.advance
//
//import org.apache.spark._
//import org.apache.spark.mllib.clustering.{DistributedLDAModel, LDA}
//import org.apache.spark.mllib.linalg.Vectors
//import org.apache.spark.sql.SQLContext
//
///**
//* Author:  blogchong
//* Blog:    www.blogchong.com
//* Mailbox: blogchong@163.com
//* Data:    2015/11/30
//* Describe:
//*/
//class LDAPractice {
//
//  val sc = new SparkContext()
//  val sqlContext = new SQLContext(sc)
//
//  //  val input = sc.textFile("/tmp/upload/temp/vedio-all-tenxun3.txt").map(line => line.split(" ").toSeq)
//  //  val word2vec = new Word2Vec()
//  //  word2vec.setVectorSize(1000)
//  //  val model = word2vec.fit(input)
//  //  val modelPath = "/tmp/upload/temp/v_1000.model"
//  //  model.save(sc,modelPath)
//  //  val dataFrame = sqlContext.read.parquet(modelPath+"/data")
//  //  val dataArray = dataFrame.select("word", "vector")
//  //  dataArray.map(f=>s"${f.getString(0)} ${f.getList[Float](1).toArray.mkString(" ")}").saveAsTextFile("/tmp/upload/temp/v_1000.model.txt")
//
//
//  val tempStopWords = "腾讯 课堂  详情 课程  特色 课程目标".split("\\s+").toSet
//
//  val modelPath = "/tmp/upload/temp/lda_topic_30"
//
//  val keywordsLocal = sc.textFile("/tmp/upload/temp/词库.txt").collect.toSet
//  val keywordsDis = sc.broadcast(keywordsLocal)
//
//  val stopwordsLocal = sc.textFile("/tmp/upload/temp/stopwords_v1.0.txt").collect.toSet ++ tempStopWords
//  val stopwordsDis = sc.broadcast(stopwordsLocal)
//
//  val data = sc.textFile("/tmp/upload/temp/vedio-word-segmentation.txt")
//  val data2 = sc.textFile("/tmp/upload/temp/videoAllinfo-readyhtml-segment.txt")
//
//  val data3 = data.union(data2)
//
//  val docs = data3.zipWithIndex.
//    map(_.swap).
//    map(f => Doc(f._1, f._2.split("\\s+").
//    filter(word => keywordsDis.value.contains(word)))).filter(f => f.sentence.length > 0)
//
//  docs.map(f => s"${f.label}\t${f.sentence.mkString(" ")}").repartition(1).saveAsTextFile("/tmp/upload/temp/index-doc2")
//
//  val wordToLabelLocal = keywordsLocal.zipWithIndex.toMap
//  //docs.flatMap(f => f.sentence).distinct().zipWithIndex.collect().toMap
//  val wordToLabelDis = sc.broadcast(wordToLabelLocal)
//
//  val LabelToWordToDis = sc.broadcast(wordToLabelLocal.map(f => (f._2, f._1)))
//
//  val labelToDfLocal = docs.flatMap(f => f.sentence.distinct).
//    map(f => (wordToLabelDis.value(f), 1)).reduceByKey(_ + _).collect.toMap
//  val labelToDfDis = sc.broadcast(labelToDfLocal)
//  val docSize = sc.broadcast(docs.count())
//
//  def compute(docWordSize: Int, tf: Long, idf: Long, docSize: Long) = {
//    val realIdf = Math.log(docSize / (idf.toDouble + 1))
//    val realTf = tf.toDouble / docWordSize
//    realTf * realIdf
//  }
//
//  val corpus = docs.map {
//    f =>
//    //val sentenceSize = f.sentence.size
//      val docVector = f.sentence.groupBy(f => f).map {
//        k =>
//          val wordLabel = wordToLabelDis.value(k._1)
//          //val tfidf = compute(sentenceSize, k._2.size, labelToDfDis.value(wordLabel), docSize.value)
//          (wordLabel.toInt, k._2.size.toDouble)
//      }.toSeq
//      (f.label, Vectors.sparse(wordToLabelDis.value.size, docVector))
//  }.repartition(20)
//
//
//  // Cluster the documents into three topics using LDA
//  val topicSize = 180
//  val ldaModel = new LDA().setK(topicSize).setMaxIterations(160).run(corpus)
//
//  // Output topics. Each is a distribution over words (matching word count vectors)
//  println("Learned topics (as distributions over vocab of " + ldaModel.vocabSize + " words):")
//
//
//  val topicIndices = ldaModel.describeTopics(maxTermsPerTopic = keywordsLocal.size)
//
//  val topics = topicIndices.map {
//    case (terms, termWeights) =>
//      terms.map(LabelToWordToDis.value(_)).zip(termWeights)
//  }
//
//  topics.zipWithIndex.foreach {
//    case (topic, i) =>
//      println(s"TOPIC $i")
//      topic.foreach {
//        case (term, weight) => println(s"$term\t$weight")
//      }
//      println(s"==========")
//  }
//
//
//
//  // Save and load model.
//  ldaModel.save(sc, modelPath)
//
//  val dldaModel = ldaModel.asInstanceOf[DistributedLDAModel]
//  //dldaModel.topic
//
//  dldaModel.topTopicsPerDocument(topicSize).map {
//    f =>
//      (f._1, f._2 zip f._3)
//  }.map(f => s"${f._1} ${f._2.map(k => k._1 + ":" + k._2).mkString(" ")}").repartition(1).saveAsTextFile("/tmp/upload/temp/lda-document-topic7")
//
//  sc.parallelize(topics.zipWithIndex.map {
//    case (topic, i) =>
//      i + " " + topic.map {
//        case (term, weight) => s"$term:$weight"
//      }.mkString(" ")
//  }).repartition(1).saveAsTextFile("/tmp/upload/temp/lda-topic-term3")
//
//}
