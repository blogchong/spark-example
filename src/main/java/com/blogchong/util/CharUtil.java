package com.blogchong.util;

/**
 * Author:  blogchong
 * Blog:    www.blogchong.com
 * Mailbox: blogchong@163.com
 * Data:    2015/11/6
 * Describe:字符通用工具类
 */
public class CharUtil {

    public static void main(String[] args) {

        String str = "0改000123";
        if (isNumeric(str)) {
            System.out.println("ok");
            System.out.println("Num1: " + countChineseNum(str));
            System.out.println("Num2: " + str.length());
        } else {
            System.out.println("no");
            System.out.println("Num: " + str.length());
        }

    }

    //判断字符串有几个中文
    public static int countChineseNum(String str) {
        String regex = "[\u4e00-\u9fff]";
        int count = (" " + str + " ").split (regex).length - 1;
        return count;
    }

    // 判断一个字符串是否含有中文
    public static boolean isChinese(String str) {
        if (str == null) return false;
        for (char c : str.toCharArray()) {
            if (isChinese(c))
                return true;
            // 有一个中文字符就返回
        }
        return false;
    }
    public static boolean isChinese(char c) {
        // 根据字节码判断
        return c >= 0x4E00 &&  c <= 0x9FA5;
    }

    //判断字符串纯数字
    public static boolean isNumeric(String str){
        for (int i = 0; i < str.length(); i++){
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }

}
