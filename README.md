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

###2.3 执行代码
* 执行命令`./spark-submit --class com.blogchong.spark.mllib.AlsArithmetic  --master spark://192.168.5.200:7077  --num-executors 2 --driver-memory 124m --executor-memory 124m --total-executor-cores 2  /root/spark/hcy/spark-example.jar`<br>
//需要注意的是，在设置core数以及内存时，最好先参考一下spark-master-id:8080页面中的worker参数，别超过了就行。<br>
* 跑完了，直接到输出文件夹下，找到代码的输出结果即可。<br>

###2.4 附加说明
>在实际的调试过程中，我们会把ALS的几个重要参数，比如迭代次数，比如lambda值等，设置成一个范围，然后进行逐步调优，当MSE值，也就是均根方差值最小时，这个模型即我们需要的训练模型。<br>

##3 协同推荐ALS算法电影推荐实例
###3.1 数据准备
**当前用户(需要给这货做推荐)评分数据(11条)personalRatings.txt，格式  用户ID::电影ID::评分::时间戳**<br>
>0::1::5::1409495135<br>
 0::780::4::1409495135<br>
 0::590::3::1409495135<br>
 0::1216::4::1409495135<br>
 0::648::5::1409495135<br>
 0::344::3::1409495135<br>
 0::165::4::1409495135<br>
 0::153::5::1409495135<br>
 0::597::4::1409495135<br>
 0::1586::5::1409495135<br>
 0::231::5::1409495135<br>

**电影信息数据(3706条)movies.dat，格式: 电影ID::电影名称::类型**<br>
>1::Toy Story (1995)::Animation|Children's|Comedy<br>
2::Jumanji (1995)::Adventure|Children's|Fantasy<br>
3::Grumpier Old Men (1995)::Comedy|Romance<br>
4::Waiting to Exhale (1995)::Comedy|Drama<br>
5::Father of the Bride Part II (1995)::Comedy<br>
6::Heat (1995)::Action|Crime|Thriller<br>
7::Sabrina (1995)::Comedy|Romance<br>
8::Tom and Huck (1995)::Adventure|Children's<br>
9::Sudden Death (1995)::Action<br>

**用户电影评分信息数据(1000209条)ratings.dat，格式: 用户ID::电影名称::评分::时间戳**<br>
>3::260::5::978297512<br>
3::2858::4::978297039<br>
3::3114::3::978298103<br>
3::1049::4::978297805<br>
3::1261::1::978297663<br>
3::552::4::978297837<br>
3::480::4::978297690<br>
4::1265::2::978298316<br>
4::1266::5::978297396<br>
4::733::5::978297757<br>

上传到hdfs的/spark/mllib/data/als2路径中。<br>

###3.2 代码打包
* 在Intellij中，点击file->选择project structure->选择Artifact->添加jar->把乱七八糟的依赖移除->勾选Build on make。<br>
* 点击Build->选择Build Artifact->选择ReBuild，然后在之前填写的路径下找到jar。<br>
* 上传到spark中。<br>

###3.3 执行代码
* 执行命令`./spark-submit --class com.blogchong.spark.mllib.AlsArithmeticPractice  --master spark://192.168.5.200:7077  --num-executors 2 --driver-memory 400m --executor-memory 400m --total-executor-cores 2  /root/spark/hcy/spark-example.jar`<br>
//需要注意的是，在设置core数以及内存时，最好先参考一下spark-master-id:8080页面中的worker参数，别超过了就行。<br>
* 跑完了，直接到输出文件夹下，找到代码的输出结果即可。<br>

###3.4 附加说明
>在调试过程中，把ALS的几个重要参数，比如迭代次数，比如lambda值等，设置成一个范围，然后进行逐步调优，当MSE值，也就是均根方差值最小时，这个模型即我们需要的训练模型。<br>

###3.5 输出结果
>对于每次尝试的结果直接打印，最终给用户0推荐的结果按降序保存在/spark/mllib/result/als2/data/recommendations，模型文件保存在/spark/mllib/result/als2/model。


