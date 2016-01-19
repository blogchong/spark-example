package com.blogchong.spark.mllib.advance.LdaExtractTopics.Check

/**
 * Author:  blogchong
 * Blog:    www.blogchong.com
 * Mailbox: blogchong@163.com
 * Data:    2015/10/23
 * Describe: 处理传参
 */

class PredictsDocTopicsArgsParser {

  var dataPath: String = null
  var modelPath: String = null
  var topicsPath: String = null
  var wordsPath: String = null
  var topicSize: Int = 100

  def parseArgs(inputArgs: List[String]): Unit = {

    var args = inputArgs

    while (!args.isEmpty) {
      args match {
        case ("PdataPath") :: value :: tail =>
          dataPath = value
          println("PdataPath: " + dataPath)
          args = tail
        case ("PmodelPath") :: value :: tail =>
          modelPath = value
          println("PmodelPath: " + modelPath)
          args = tail
        case ("PwordsPath") :: value :: tail =>
          wordsPath = value
          println("PwordsPath: " + wordsPath)
          args = tail
        case ("PtopicSize") :: value :: tail =>
          topicSize = value.toInt
          println("PtopicSize: " + topicSize)
          args = tail
        case ("PtopicsPath") :: value :: tail =>
          topicsPath = value
          println("PtopicsPath: " + topicsPath)
          args = tail
        case Nil =>

        case _ =>
          throw new IllegalArgumentException(getUsageMessage(args))
      }
    }
  }

  def getUsageMessage(unknownParam: List[String] = null): String = {
    val message = if (unknownParam != null) s"Unknown/unsupported param $unknownParam\n" else ""
    message +
      """
        |Usage: com.blogchong.spark.mllib.advance.CSDN.LDAModelBuild [options]
        |Options:
        |  PdataPath        the location where you put your training documents
        |  PmodelPath       the location where you save your model
        |  PtopicSize       topic size of lda
        |  PwordsPath       the location of dictionary
        |  PtopicsPath      the topics of documents
      """.stripMargin
  }
}
