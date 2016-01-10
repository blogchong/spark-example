package com.blogchong.spark.mllib.newWord

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
 * Describe: 薪酬发现
 */
object GetNewWord {
  def main(args: Array[String]) {
    // 设置运行环境
    val conf = new SparkConf().setAppName("新词发现")
    val sc = new SparkContext(conf)

    val inputPath = args(0)
    val outputPath = args(1)

    println("InputPath:" + inputPath)
    println("OutputPath:" + outputPath)

    //发现工具初始化
    val learnTool: LearnTool = new LTSerializa().learnTool
    //获取初始数据
    val input = sc.textFile(inputPath)

    println("InputSize:" + input.count())

    if (learnTool == null) {
      println("learnTool is NULL!")
    } else {
      println("learnTool is not NULL!")
    }

    input.map {
      f =>
        val notes = f.split("\t")
        val noteObj = notes(1).asInstanceOf[JSONObject]
        NlpAnalysis.parse(noteObj.obj.get("title").toString, learnTool)
        NlpAnalysis.parse(noteObj.obj.get("body").toString, learnTool)
    }

    val newWords = learnTool.getTopTree(10000, TermNatures.NW)

    if (newWords == null) {
      println("NewWords is NULL!")
    } else {
      println("NewWordsSize:" + newWords.size())
      sc.parallelize(newWords.map(f => f.getKey).toSeq).saveAsTextFile(outputPath)
    }
    sc.stop()
  }

  class LTSerializa extends java.io.Serializable {
    def learnTool = new LearnTool
  }

}
