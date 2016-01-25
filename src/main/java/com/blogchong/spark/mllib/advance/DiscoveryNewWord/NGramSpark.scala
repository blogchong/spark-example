package com.blogchong.spark.mllib.advance.DiscoveryNewWord

import org.apache.spark.{SparkContext, SparkConf}
import org.ansj.splitWord.analysis.{ToAnalysis}
import scala.collection.JavaConversions._
import java.util.Date
import com.blogchong.util.{CharUtil, NewTime}
import java.util.regex.Pattern
import java.util
import org.apache.log4j.Logger

/**
 * Author:  blogchong
 * Blog:    www.blogchong.com
 * Mailbox: blogchong@163.com
 * Data:    2016/1/12
 * Describe: NGram算法 - 新词发现实验
 */
object NGramSpark {

  val logger = Logger.getLogger(NGramSpark.getClass)

  def main(args: Array[String]) {

    // 设置运行环境
    val conf = new SparkConf().setAppName("ansj分词，新词发现")
    val sc = new SparkContext(conf)

    val userDicPath = args(0)
    val inputPath = args(1)
    val outputPath = args(2)

    println("=============>UserDicPath:" + userDicPath)
    println("=============>InputPath:" + inputPath)
    println("=============>OutputPath:" + outputPath)

    //输入
    val input = sc.textFile(inputPath).collect()
    println("=============>InputSize:" + input.size)

    //初始化用户字典
    val userDic = sc.textFile(userDicPath).map {
      f =>
        val notes = f.split("\t")
        if (notes.size == 3) {
          notes(0)
        }
    }.collect.toSet

    //设置TF/D阈值
    val tdfThreshold = 2.5

    println("=============>UserDicSize:" + userDic.size)

    val mergeWordTFDMap = new util.HashMap[String, Double]()

    //进行词合并
    val mergeWordTFMap = new util.HashMap[String, Int]()
    val mergeWordTF2WordsMap = new util.HashMap[String, String]()
    val mergeWordDMap = new util.HashMap[String, Int]()
    val wordMap = new util.HashMap[String, Int]()

    val que_2: util.Queue[String] = new util.LinkedList[String]
    val que_3: util.Queue[String] = new util.LinkedList[String]

    var count = 0
    //统计总词数
    var wordCount = 0
    val output = input.map {
      f =>
        val newWordSet = new util.HashSet[String]()
        val notes = f.split("\t")
        val id = notes(0)
        val noteObj = net.sf.json.JSONObject.fromObject(notes(1))

        val title = noteObj.get("title").toString
        val body = noteObj.get("body").toString

        val titleParse = ToAnalysis.parse(title)
        val bodyParse = ToAnalysis.parse(body)

        val titleWords = titleParse.map {
          f =>
            replaceStr(f.getName.toLowerCase.trim)
        }.filter(f => f.length > 0)

        val bodyWords = bodyParse.map {
          f =>
            replaceStr(f.getName.toLowerCase.trim).trim
        }.filter(f => f.length > 0).union(titleWords)

        //进行单个字词统计
        bodyWords.foreach {
          f =>
            wordCount = wordCount + 1
            if (wordMap.containsKey(f)) {
              wordMap.put(f, wordMap.get(f) + 1)
            } else {
              wordMap.put(f, 1)
            }
        }

        //做两词合并
        bodyWords.foreach {
          j =>
            que_2.offer(j)
            //先做2词合并实验，并记录词频，目前只考虑词频  文档频以及 平均文档频
            if (que_2.size() == 2) {
              val newWord2Words = joinWord(que_2, 2)
              val newWord = newWord2Words._1
              //判断是否在基本字典中
              if (!userDic.contains(newWord) && newWord.trim.size != 0) {
                //存储组合词和字词关系
                mergeWordTF2WordsMap.put(newWord2Words._1, newWord2Words._2)
                //存储词个数
                if (mergeWordTFMap.containsKey(newWord)) {
                  mergeWordTFMap.put(newWord, mergeWordTFMap.get(newWord) + 1)
                } else {
                  mergeWordTFMap.put(newWord, 1)
                }
                //加入单篇文档新词set
                newWordSet.add(newWord)
                wordCount = wordCount + 1
              }
            } else if (que_2.size() > 2) {
              //先移除之前的
              que_2.poll()
              val newWord2Words = joinWord(que_2, 2)
              val newWord = newWord2Words._1
              //判断是否在基本字典中
              if (!userDic.contains(newWord) && newWord.trim.size != 0) {
                //存储组合词和字词关系
                mergeWordTF2WordsMap.put(newWord2Words._1, newWord2Words._2)
                //存储词个数
                if (mergeWordTFMap.containsKey(newWord)) {
                  mergeWordTFMap.put(newWord, mergeWordTFMap.get(newWord) + 1)
                } else {
                  mergeWordTFMap.put(newWord, 1)
                }
                //加入单篇文档新词set
                newWordSet.add(newWord)
                wordCount = wordCount + 1
              }
            }
        }

        //做三词合并
        bodyWords.foreach {
          j =>
            que_3.offer(j)
            //先做2词合并实验，并记录词频，目前只考虑词频  文档频以及 平均文档频
            if (que_3.size() == 3) {
              val newWord2Words = joinWord(que_3, 3)
              val newWord = newWord2Words._1
              //判断是否在基本字典中
              if (!userDic.contains(newWord) && newWord.trim.size != 0) {
//                //存储组合词和字词关系
                mergeWordTF2WordsMap.put(newWord2Words._1, newWord2Words._2)
                //存储词个数
                if (mergeWordTFMap.containsKey(newWord)) {
                  mergeWordTFMap.put(newWord, mergeWordTFMap.get(newWord) + 1)
                } else {
                  mergeWordTFMap.put(newWord, 1)
                }
                //加入单篇文档新词set
                newWordSet.add(newWord)
                wordCount = wordCount + 1
              }
            } else if (que_3.size() > 3) {
              //先移除之前的
              que_3.poll()
              val newWord2Words = joinWord(que_3, 3)
              val newWord = newWord2Words._1
              //判断是否在基本字典中
              if (!userDic.contains(newWord) && newWord.trim.size != 0) {
                if (mergeWordTFMap.containsKey(newWord)) {
                  mergeWordTFMap.put(newWord, mergeWordTFMap.get(newWord) + 1)
                } else {
                  mergeWordTFMap.put(newWord, 1)
                }
                //加入单篇文档新词set
                newWordSet.add(newWord)
                wordCount = wordCount + 1
              }
            }
        }

        //计算文档频
        newWordSet.foreach {
          f =>
            if (mergeWordDMap.containsKey(f)) {
              mergeWordDMap.put(f, mergeWordDMap.get(f) + 1)
            } else {
              mergeWordDMap.put(f, 1)
            }
        }

        if (count % 5000 == 0) {
          println("Begin--------------------------------------------------------")
          println("=============>count:" + count)
          println("=============>mergeWordTFMapSize:" + mergeWordTFMap.size())
          println("=============>mergeWordDMapSize:" + mergeWordDMap.size())
          println("End--------------------------------------------------------")
        }

        count = count + 1
        s"${id}\t${bodyWords.mkString(",")}"
    }

    println("=============>OutputSize:" + output.size)

    val dateDate = new Date
    val saveTime = NewTime.dateToString(dateDate, NewTime.`type`)

    //存储基础分词之后的结果
//    sc.parallelize(output.toSeq).saveAsTextFile(outputPath + "/" + saveTime + "/splitwords")

    //进行TF词频，文档排序输出
    val scTFMap = mergeWordTFMap.toSeq.sortBy {
      case (word, freq) => freq
    }.filter(_._2 > 1)
    println("=============>新词词频MergeWordTFMapSize:" + scTFMap.size())
//    sc.parallelize(scTFMap).saveAsTextFile(outputPath + "/" + saveTime + "/tfNewWord")

    //进行文档频存储
    val scDMap = mergeWordDMap.toSeq.sortBy {
      case (word, freq) => freq
    }.filter(_._2 > 1)
    println("=============>新词文档频MergeWordDMapSize:" + scDMap.size())
//    sc.parallelize(scDMap).saveAsTextFile(outputPath + "/" + saveTime + "/dNewWord")

    //进行平均词频计算
    scDMap.foreach {
      f =>
        if (mergeWordTFMap.containsKey(f._1) && f._2 != 0) {
          mergeWordTFDMap.put(f._1, mergeWordTFMap.get(f._1).toDouble / f._2.toDouble)
        }
    }

    //进行平均文档频计算存储
    val scTFDMap = mergeWordTFDMap.toSeq.sortBy {
      case (word, freq) => freq
    }.filter(_._1.split("\\s").size == 1).filter(_._2 >= tdfThreshold)
    println("=============>新词平均词频(过阈值过纯英文\\s组合之后)MergeWordTFDMapSize:" + scTFDMap.size())
    sc.parallelize(scTFDMap).saveAsTextFile(outputPath + "/" + saveTime + "/tfdNewWord")

    //////////////////////////////////计算凝固度////////////////////////////////////////////////////////////
    sc.parallelize(wordMap.toSeq).saveAsTextFile(outputPath + "/" + saveTime + "/wordMap")

    println("=============>词总量wordCount:" + wordCount)

    //计算单个字词的概率
    val wordRateMap = new util.HashMap[String, Double]()
    wordMap.foreach {
      f =>
        wordRateMap.put(f._1, f._2.toDouble / wordCount.toDouble)
    }
    println("=============>全量子词概率WordRateMapSize:" + wordRateMap.size())
    sc.parallelize(wordRateMap.toSeq.sortBy{
      case (word, freq) => freq
    }).saveAsTextFile(outputPath + "/" + saveTime + "/wordRateMap")

    //计算组合词概率，只计算过了TFD阈值的词
    val wordTfdRateMap = new util.HashMap[String, Double]()
    scTFDMap.foreach {
      f =>
        wordTfdRateMap.put(f._1, f._2.toDouble / wordCount.toDouble)
    }
    println("=============>新词组合概率(过了TFD阈值)WordTfdRateMapSize:" + wordTfdRateMap.size())
    sc.parallelize(wordTfdRateMap.toSeq.sortBy{
      case (word, freq) => freq
    }).saveAsTextFile(outputPath + "/" + saveTime + "/wordTfdRateMap")

    //计算组合词概率和 字词乘积和
    sc.parallelize(mergeWordTF2WordsMap.toSeq).saveAsTextFile(outputPath + "/" + saveTime + "/mergeWordTF2WordsMap")
    val proRate = new util.HashMap[String, Double]()
    wordTfdRateMap.foreach{
      f=>
        var rate : Double = 1.0
        mergeWordTF2WordsMap.get(f._1).split("\t").foreach{
          g =>
            rate = rate * wordRateMap.get(g)
        }

        proRate.put(f._1, wordTfdRateMap.get(f._1) / rate)
    }
    println("=============>计算组合词概率和子词乘积比值ProRateSize:" + proRate.size())

    //保存概率比
    sc.parallelize(proRate.toSeq.sortBy{
      case(word, freq) => freq
    }).saveAsTextFile(outputPath + "/" + saveTime + "/proRate")

    sc.stop()
  }

  //合并新词
  def joinWord(que: util.Queue[String], num: Int): (String, String) = {
    val que_ret: util.Queue[String] = que
    if (num == 2) {
      val word1 = que_ret.poll()
      val word2 = que_ret.poll()
      if ((CharUtil.isNumeric(word1) || CharUtil.isNumeric(word2))
        || (word1.equals("的") || word2.equals("的"))) {
        ("", word1 + "\t" + word2)
      } else if ((word1.equals("-") || (word2.equals("-"))) || (word1.equals("_") || word2.equals("_"))) {
        ("", word1 + "\t" + word2)
      } else {
        if (!CharUtil.isChinese(word1) && !CharUtil.isChinese(word2)) {
          (word1 + " " + word2, word1 + "\t" + word2)
        } else {
          (word1 + word2, word1 + "\t" + word2)
        }
      }
    } else if (num == 3) {
      val word1 = que_ret.poll()
      val word2 = que_ret.poll()
      val word3 = que_ret.poll()
      if ((CharUtil.isNumeric(word1) || CharUtil.isNumeric(word2) || CharUtil.isNumeric(word3))
        || (word1.equals("的") || word2.equals("的") || word3.equals("的"))) {
        ("", word1 + "\t" + word2 + "\t" + word3)
      } else if (word1.equals("-") || word1.equals("_") || word3.equals("-") || word3.equals("_")) {
        ("", word1 + "\t" + word2 + "\t" + word3)
      } else if (!CharUtil.isChinese(word1) && !CharUtil.isChinese(word2) && !CharUtil.isChinese(word3)
        && (word2.equals("-") || word2.equals("_"))) {
        (word1 + word2 + word3, word1 + "\t" + word2 + "\t" + word3)
      } else if (!CharUtil.isChinese(word1) && !CharUtil.isChinese(word2) && !CharUtil.isChinese(word3)
        && (!word2.equals("-") && !word2.equals("_"))) {
        (word1 + " " + word2 + " " + word3, word1 + "\t" + word2 + "\t" + word3)
      } else {
        (word1 + word2 + word3, word1 + "\t" + word2 + "\t" + word3)
      }
    } else {
      (que_ret.mkString(""), que.mkString("\t"))
    }

  }

  //特殊字符去除
  def replaceStr(str: String) = {
    if (!CharUtil.isChinese(str) && !str.equals("-") && !str.equals("_") && str.size <= 1) {
      ""
    } else {
      val regEx = "[`~!@#$%^&*()+=|{}':→;ˇ'\\\\,\\[\\].<>/?~！≤@#￥%《》ø━……&*（）——+|{}【】‘；：”“’。，、？]"
      val pattern = Pattern.compile(regEx)
      val matcher = pattern.matcher(str)
      matcher.replaceAll("").trim
    }
  }

}
