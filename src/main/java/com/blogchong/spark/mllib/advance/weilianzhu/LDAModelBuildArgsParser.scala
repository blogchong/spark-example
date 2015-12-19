//package com.blogchong.spark.mllib.advance.weilianzhu
//
///**
// *
// */
//class LDAModelBuildArgsParser {
//
//  var dataPath: String = null
//  var modelPath: String = null
//  var wordsPath: String = null
//  var debug: Boolean = false
//  var saveVector: Boolean = false
//  var topicSize: Int = 160
//  var maxIterations: Int = 160
//  var hbaseTableName: String = "spark_ml_lda_model_result"
//
//  def parseArgs(inputArgs: List[String]): Unit = {
//
//    var args = inputArgs
//
//    while (!args.isEmpty) {
//      args match {
//        case ("PdataPath") :: value :: tail =>
//          dataPath = value
//          args = tail
//        case ("PmodelPath") :: value :: tail =>
//          modelPath = value
//          args = tail
//
//        case ("PtopicSize") :: value :: tail =>
//          topicSize = value.toInt
//          args = tail
//
//        case ("PmaxIterations") :: value :: tail =>
//          maxIterations = value.toInt
//          args = tail
//
//        case ("Pdebug") :: value :: tail =>
//          debug = value.toBoolean
//          args = tail
//
//        case ("PwordsPath") :: value :: tail =>
//          wordsPath = value
//          args = tail
//
//        case ("PsaveVector") :: value :: tail =>
//          saveVector = value.toBoolean
//          args = tail
//        case ("PhbaseTableName") :: value :: tail =>
//          hbaseTableName = value
//          args = tail
//        case Nil =>
//
//        case _ =>
//          throw new IllegalArgumentException(getUsageMessage(args))
//      }
//    }
//  }
//
//  def getUsageMessage(unknownParam: List[String] = null): String = {
//    val message = if (unknownParam != null) s"Unknown/unsupported param $unknownParam\n" else ""
//    message +
//      """
//        |Usage: com.letv.batch.LDAModelBuild [options]
//        |Options:
//        |  PdataPath        the location where you put your training documents
//        |  PmodelPath       the location where you save your model
//        |  PwordsPath       the location of dictionary
//        |  PtopicSize       topic size of lda
//        |  PmaxIterations   maxIterations lda should run
//        |  PsaveVector      whether to save word vector and doc vector;default value is false
//        |  PhbaseTableName  hbase table name to save word vector ,doc vector;default value is  spark_ml_lda_model_result
//      """.stripMargin
//  }
//}
