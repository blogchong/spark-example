package com.blogchong.spark.mllib.newWord

import org.apache.spark.{SparkContext, SparkConf}
import scala.util.parsing.json.JSONObject
import org.ansj.splitWord.analysis.{ToAnalysis, BaseAnalysis, NlpAnalysis}
import org.ansj.domain.Term
import scala.collection.JavaConversions._
import net.sf.json
import java.util.Date
import com.blogchong.util.NewTime
import java.util.regex.Pattern
import org.ansj.library.UserDefineLibrary
import java.util

/**
 * Author:  blogchong
 * Blog:    www.blogchong.com
 * Mailbox: blogchong@163.com
 * Data:    2016/1/12
 * Describe: NGram算法
 */
object NGramSpark {
  def main(args: Array[String]) {

    // 设置运行环境
    val conf = new SparkConf().setAppName("ansj分词，存储起来")
    val sc = new SparkContext(conf)

    val userDicPath = args(0)
    val inputPath = args(1)
    val outputPath = args(2)

    println("UserDicPath:" + userDicPath)
    println("InputPath:" + inputPath)
    println("OutputPath:" + outputPath)

    //输入
    val input = sc.textFile(inputPath)
    println("InputSize:" + input.count())

    //初始化用户字典

    println("UserDicSize:" + input.count())
    val userDic = sc.textFile(userDicPath).map {
      f=>
        val notes = f.split("\t")
        if (notes.size == 3){
          notes(0)
        }
    }.collect.toSet

    val output = input.map {
      f =>
        val notes = f.split("\t")
        val id = notes(0)
        val noteObj = net.sf.json.JSONObject.fromObject(notes(1))

        val title = noteObj.get("title").toString
        val body = noteObj.get("body").toString

        val titleParse= ToAnalysis.parse(title)
        val bodyParse = ToAnalysis.parse(body)

        val titleWords = titleParse.map{
          f=>
            replaceStr(f.getName)
        }.filter(f=>f.length > 0)

        val bodyWords = bodyParse.map{
          f=>
            replaceStr(f.getName)
        }.filter(f=>f.length > 0).union(titleWords).mkString(",")

        s"${id}\t${bodyWords}"
    }

    val dateDate = new Date
    val saveTime = NewTime.dateToString(dateDate, NewTime.`type`)

    //存储基础分词之后的结果
    output.saveAsTextFile(outputPath + "/" + saveTime + "/splitwords")

    //进行词合并
    val mergeWordTFMap = new util.HashMap[String, Int]()
    val mergeWordDMap = new util.HashMap[String, Int]()
    val mergeWordTFDMap = new util.HashMap[String, Int]()

    val que_2: util.Queue[String] = new util.LinkedList[String]
    output.map{
      f=>
        //用于记录一篇文档中所有的新词
        val newWordSet = new util.HashSet[String]()
        val words = f.split("\t")(1).split(",")
        words.map{
          j=>
            que_2.offer(j)

            //合并词，并记录词频
            if (que_2.size() == 2) {
              val newWord = que_2.mkString("")
              //判断是否在基本字典中
              if (!userDic.contains(newWord)) {
                if (mergeWordTFMap.containsKey(newWord)) {
                  mergeWordTFMap.put(newWord, mergeWordTFMap.get(newWord) + 1)
                } else {
                  mergeWordTFMap.put(newWord, 1)
                }
                //加入单篇文档新词set
                newWordSet.add(newWord)
              }
            }

            //移除队列尾部词
            if (que_2.size() > 2) {
              que_2.poll()
            }
        }

        //计算文档频
        newWordSet.map{
          f=>
            if (mergeWordDMap.containsKey(f)) {
              mergeWordDMap.put(f, mergeWordDMap.get(f) + 1)
            } else {
              mergeWordDMap.put(f, 1)
            }
        }
    }

    //进行TF词频，文档排序输出
    sc.parallelize(mergeWordTFMap.toSeq.sortBy{case(word,freq) => freq}).saveAsTextFile(outputPath + "/" + saveTime + "/tfNewWord")
    sc.parallelize(mergeWordDMap.toSeq.sortBy{case(word,freq) => freq}).saveAsTextFile(outputPath + "/" + saveTime + "/dNewWord")

    //进行平均词频计算
    mergeWordDMap.map{
      f=>
        if (mergeWordTFMap.containsKey(f._1) && f._2 != 0) {
          mergeWordTFDMap.put(f._1, mergeWordTFMap.get(f._1) / f._2)
        }
    }

    //进行平均文档频计算存储
    sc.parallelize(mergeWordTFDMap.toSeq.sortBy{case(word, freq) => freq}).saveAsTextFile(outputPath + "/" + saveTime + "/tfdNewWord")

    sc.stop()
  }

  //特殊字符去除
  def replaceStr(str: String) = {
    val regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]"
    val pattern = Pattern.compile(regEx)
    val matcher = pattern.matcher(str)
    matcher.replaceAll("").trim
  }

}
