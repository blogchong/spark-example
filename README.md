#这是一个Spark MLlib实例
##1 K-meams实例
###1.1 数据准备
**准备好如下数据：**<br>
>0.0 0.0 0.0<br>
0.1 0.1 0.1<br>
0.2 0.2 0.2<br>
9.0 9.0 9.0<br>
9.1 9.1 9.1<br>
9.2 9.2 9.2<br>
0.3 0.2 0.2<br>
9.1 9.5 9.1<br>
0.2 0.2 0.2<br>
0.1 0.2 0.2<br>
8.9 9.5 9.1<br>

命名为kmeans_data.txt，且上传到hdfs的/spark/mllib/data/路径中。<br>

###1.2 代码打包
* 在Intellij中，点击file->选择project structure->选择Artifact->添加jar->把乱七八糟的依赖移除->勾选Build on make。<br>
* 点击Build->选择Build Artifact->选择ReBuild，然后在之前填写的路径下找到jar。<br>
* 上传到spark中。<br>

###1.3 执行代码
* 执行命令`./spark-submit --class com.blogchong.spark.mllib.Kmeans  --master spark://192.168.5.200:7077  --num-executors 2 --driver-memory 124m --executor-memory 124m --total-executor-cores 2  /root/spark/hcy/spark-example.jar`<br>
//需要注意的是，在设置core数以及内存时，最好先参考一下spark-master-id:8080页面中的worker参数，别超过了就行。<br>
* 跑完了，直接到输出文件夹下，找到代码的输出结果即可。<br>

##2 协同推荐ALS算法实例
###2.1 数据准备
**用户评分数据，格式: 用户ID,电影ID,评分**<br>
>1,1,5.0<br>
 1,2,1.0<br>
 1,3,5.0<br>
 1,4,1.0<br>
 2,1,5.0<br>
 2,2,1.0<br>
 2,3,5.0<br>
 2,4,1.0<br>
 3,1,1.0<br>
 3,2,5.0<br>
 3,3,1.0<br>
 3,4,5.0<br>
 4,1,1.0<br>
 4,2,5.0<br>
 4,3,1.0<br>
 4,4,5.0<br>

上传到hdfs的/spark/mllib/data/als路径中。<br>

###2.2 代码打包
* 在Intellij中，点击file->选择project structure->选择Artifact->添加jar->把乱七八糟的依赖移除->勾选Build on make。<br>
* 点击Build->选择Build Artifact->选择ReBuild，然后在之前填写的路径下找到jar。<br>
* 上传到spark中。<br>

###1.3 执行代码
* 执行命令`./spark-submit --class com.blogchong.spark.mllib.AlsArithmetic  --master spark://192.168.5.200:7077  --num-executors 2 --driver-memory 124m --executor-memory 124m --total-executor-cores 2  /root/spark/hcy/spark-example.jar`<br>
//需要注意的是，在设置core数以及内存时，最好先参考一下spark-master-id:8080页面中的worker参数，别超过了就行。<br>
* 跑完了，直接到输出文件夹下，找到代码的输出结果即可。<br>
