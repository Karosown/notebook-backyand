package com.karos.project.common;

public interface RegexValid {
    //定义script的正则表达式，去除js可以防止注入
    String scriptRegex="<script[^>]*?>[\\s\\S]*?<\\/script>";
    //定义style的正则表达式，去除style样式，防止css代码过多时只截取到css样式代码
    String styleRegex="<style[^>]*?>[\\s\\S]*?<\\/style>";
    //定义HTML标签的正则表达式，去除标签，只提取文字内容
    String htmlRegex="<[^>]+>";
    //定义空格,回车,换行符,制表符
    String spaceRegex = "\\s*|\t|\r|\n";
}
