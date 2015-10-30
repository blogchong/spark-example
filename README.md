#这是一个Spark MLlib实例
##1 K-meams实例
###1.1 数据准备
准备好如下数据：
>0.0 0.0 0.0 1
>0.1 0.1 0.1 1
>0.2 0.2 0.2 1
>9.0 9.0 9.0 0
>9.1 9.1 9.1 0
>9.2 9.2 9.2 0
>0.3 0.2 0.2 1
>9.1 9.5 9.1 0
>0.2 0.2 0.2 1
>0.1 0.2 0.2 1
>8.9 9.5 9.1 0
命名为kmeans_data.txt，且上传到hdfs的/spark/mllib/data/路径中。

###1.2 代码打包
(1)在Intellij中，点击file->选择project structure->选择Artifact->添加jar->把乱七八糟的依赖移除->勾选Build on make。
(2)点击Build->选择Build Artifact->选择ReBuild，然后在之前填写的路径下找到jar。
(3)上传到spark中。

###1.3 执行代码
(1)执行命令`./spark-submit --class com.blogchong.spark.mllib.Kmeans  --master spark://192.168.5.200:7077  --num-executors 2 --driver-memory 124m --executor-memory 124m --total-executor-cores 2  /root/spark/hcy/spark-example.jar`
>需要注意的是，在设置core数以及内存时，最好先参考一下spark-master-id:8080页面中的worker参数，别超过了就行。
(2)跑完了，直接到输出文件夹下，找到代码的输出结果即可。

