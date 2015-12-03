package com.blogchong.util;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Dijkstra.DijkstraSegment;
import com.hankcs.hanlp.seg.NShort.NShortSegment;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.IndexTokenizer;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;

import java.util.List;

/**
 * Author:  blogchong
 * Blog:    www.blogchong.com
 * Mailbox: blogchong@163.com
 * Data:    2015/11/26
 * Describe:TextRank算法，HanLP工具
 */
public class TextRank {

    public static void main(String[] args) {

        String words = "某个正在运行的线程可转变到Non runnable状态，这取决于运行情况。\n" +
                "某个线程还可以一直保持Non runnable状态，直到满足的条件出现。\n" +
                "某个Non runnable状态的线程不能直接跳转到运行状态，而是必须先转变为Runnable状态。\n" +
                "睡眠Sleeping：线程睡眠指定的时间。\n" +
                "I/O阻塞：线程等待，直到阻塞操作的完成。\n" +
                "join阻塞：线程等待，直到另一个线程执行完成。 \n" +
                "等待通知：线程等待另一个线程的通知。\n" +
                "锁机制阻塞：线程等待，直到指定的锁被释放，获得锁。\n" +
                "Java虚拟机JVM根据线程的优先级和调度原则执行线程。";

        //System.out.println(HanLP.segment(words));
        //标准分词
//        List<Term> termList = StandardTokenizer.segment(words);
//        System.out.println(termList);
        //NLP分词
//        List<Term> termList = NLPTokenizer.segment(words);
//        System.out.println(termList);
        //索引分词
//        List<Term> termList = IndexTokenizer.segment(words);
//        for (Term term : termList)
//        {
//            System.out.println(term + " [" + term.offset + ":" + (term.offset + term.word.length()) + "]");
//        }
        //最短路径分词
//        Segment nShortSegment = new NShortSegment().enableCustomDictionary(false).enablePlaceRecognize(true).enableOrganizationRecognize(true);
//        Segment shortestSegment = new DijkstraSegment().enableCustomDictionary(false).enablePlaceRecognize(true).enableOrganizationRecognize(true);
//        String[] testCase = new String[]{
//                "今天，刘志军案的关键人物,山西女商人丁书苗在市二中院出庭受审。",
//                "刘喜杰石国祥会见吴亚琴先进事迹报告团成员",
//        };
//        for (String sentence : testCase)
//        {
//            System.out.println("N-最短分词：" + nShortSegment.seg(sentence) + "\n最短路分词：" + shortestSegment.seg(sentence));
//        }

        //关键词提取
        String test2 = "本文不是介绍静态SQL的入门文章，介绍静态SQL最主要是介绍其中的OPTION(RECOMPILE)查询提示。通过静态SQL可以解决简单的关于动态查询条件的问题。虽然有些情况下这种查询提示并不必要，但是静态SQL中的动态查询条件往往会包含这种提示。对于什么情况下可以不用，可以看上文：T-SQL动态查询（2）——关键字查询\n" +
                "使用带有OPTION(RECOMPILE)的静态SQL有以下优点：\n" +
                "对于中等复杂的搜索条件，可以得到排版紧凑，相对易于维护的代码。\n" +
                "由于查询每次都要重编译，得到的执行计划是针对当前查询条件进行优化的。\n" +
                "权限问题，这种方式在存储过程中总是可行的，也就是说只要用户有权限执行存储过程即可，不需要有直接操作表的权限。\n" +
                "当然，有优点就肯定有缺点：\n" +
                "当需求变得越来越复杂时，语句的复杂度会趋于非线性增长，甚至没有人会想到曾经一个简单的查询会变成如此复杂和难以理解。\n" +
                "如果查询被极其频繁地调用，过度编译、重编译会严重增加服务器的负担甚至导致服务器崩溃。";
        String content = "程序员(英文Programmer)是从事程序开发、维护的专业人员。一般将程序员分为程序设计人员和程序编码人员，但两者的界限并不非常清楚，特别是在中国。软件从业人员分为初级程序员、高级程序员、系统分析员和项目经理四大类。";
        String test3 = "继上篇博文《胖虎谈ImageLoader框架(一)》 带来这篇《胖虎谈ImageLoader框架(二)》，上篇我们简单梳理了一下ImageLoader的3个步骤来异步加载图片，今天我们来详细看一下ImageLoader中加载图片的函数displayImage(…)和其中涉及到的一些ImageLoader框架带给我们的知识点。 \n" +
                "（ps：读此博文前，希望网友已经阅读并理解了《胖虎谈ImageLoader框架(一)》 再阅读此博文。）";
        String test4 = "之前微软从官网撤下了Win10首个重要更新（代号为TH2）的《介质创建工具》，而Windows Update也已经暂停推送，微软官方之前的解释是希望用户通过Windows Update更新。后来微软进一步做出声明，原来是因为用户从Windows10 RTM(Build 10240)升级到Win10 TH2之后系统中的四个关键隐私设置选项会被重置为默认。";
        String test5 = "针对读写操作的互斥锁，它可以分别针对读操作和写操作进行锁定和解锁操作。读写锁遵循的访问控制规则与互斥锁有所不同。\n" +
                "它允许任意读操作同时进行\n" +
                "同一时刻，只允许有一个写操作进行\n" +
                "==============华丽分割线============\n" +
                "并且一个写操作被进行过程中，读操作的进行也是不被允许的\n" +
                "读写锁控制下的多个写操作之间都是互斥的\n" +
                "写操作与读操作之间也都是互斥的\n" +
                "多个读操作之间却不存在互斥关系";
        List<String> keywordList = HanLP.extractKeyword(test5, 10);
        System.out.println(keywordList);

    }

}
