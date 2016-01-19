package com.blogchong.spark.mllib.advance.DiscoveryNewWord

import org.apache.spark.{SparkContext, SparkConf}
import scala.util.parsing.json.JSONObject
import scala.collection.JavaConversions._
import org.ansj.app.newWord.LearnTool
import org.ansj.domain.TermNatures
import org.ansj.splitWord.analysis.NlpAnalysis

/**
 * Author:  blogchong
 * Blog:    www.blogchong.com
 * Mailbox: blogchong@163.com
 * Data:    2016/1/10
 * Describe: ansj工具新词发现实验
 */
object AnsjDisWords {
  def main(args: Array[String]) {
    // 设置运行环境
    val conf = new SparkConf().setAppName("新词发现")
    val sc = new SparkContext(conf)

    val inputPath = args(0)
    val outputPath = args(1)

    println("InputPath:" + inputPath)
    println("OutputPath:" + outputPath)

    val list: List[Int] = List(1)

    list.map {
      k =>
        //获取初始数据
        val input = sc.textFile(inputPath)

        println("InputSize:" + input.count())

        if (LTSerializa2.getTool == null) {
          println("learnTool is NULL!")
        } else {
          println("learnTool is not NULL!")
        }

        input.map {
          f =>
            val notes = f.split("\t")
            val noteObj = notes(1).asInstanceOf[JSONObject]
            NlpAnalysis.parse(noteObj.obj.get("title").toString, LTSerializa2.getTool)
            NlpAnalysis.parse(noteObj.obj.get("body").toString, LTSerializa2.getTool)
        }

        val newWords = LTSerializa2.getTool.getTopTree(100, TermNatures.NW)

        if (newWords == null) {
          println("NewWords is NULL!")
        } else {
          println("NewWordsSize:" + newWords.size())
          sc.parallelize(newWords.map(f => f.getKey).toSeq).saveAsTextFile(outputPath)
        }
    }
    sc.stop()
  }

  object LTSerializa2 {
    val learnTool2 = new LearnTool
    def getTool = {
      learnTool2
    }
  }

  class LTSerializa extends java.io.Serializable {
    val learnTool = new LearnTool
  }

}
