package com.newcode.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/21 11:10
 * @description TODO
 **/
@Component
public class SensitiveFilter {
    private static final Logger logger= LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT="***";

    //根节点
    TrieNode root=new TrieNode();


    //改注解表示这是一个初始化方法,当容器初始化后,这个方法会自动被调用
    @PostConstruct
    public void init(){
        //读文件的字符
        try (
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                 BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));
        ) {
                String keyword;
                while ((keyword=reader.readLine())!=null){
                    //添加到前缀树
                    this.addKeyword(keyword);
                }
        }catch (IOException e){
            logger.error("加载敏感词异常:"+e.getMessage());
        }
    }
    //将一个敏感词添加到前缀树
    private void addKeyword(String keyword){
        TrieNode tem=root;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tem.getSubNode(c);
            if(tem.getSubNode(c)==null){
                subNode = new TrieNode();
                tem.addSubNode(c,subNode);
            }
                tem= subNode;
                //设置结束的标识
                if(i==keyword.length()-1){
                    tem.setKeyWordEnd(true);
                }
        }
    }

    //实现检索敏感词的过程
    //过滤敏感词并返回过滤后的结果
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        //指针1:指向树的节点
        TrieNode tem=root;
        //指针2
        int begin=0;
        //指针3
        int end=0;
        //结果
        StringBuilder stringBuilder=new StringBuilder();

        while (end<text.length()){
            char c = text.charAt(end);
            //跳过符号
            if(isSymbol(c)){
                //若指针1是根节点,将该符号计入结果,让指针2向下走
                if(tem==root){
                    stringBuilder.append(c);
                    begin++;
                }
                //无论符号在开头还是中间,指针3都向下走
                end++;
                continue;
            }
            //字符不是特殊字符
            //检查下级节点
            tem=tem.getSubNode(c);
            if(tem==null){
                //以begin开头的字符串不是敏感词
                stringBuilder.append(text.charAt(begin));
                begin++;
                end=begin;
                //重新指向根节点
                tem=root;
            }else if(!tem.isKeyWordEnd){
                //该字符不是最后一个敏感字符
                end++;
            }else{
                //该字符是最后一个敏感字符:
                stringBuilder.append(REPLACEMENT);
                end++;
                begin=end;
                //重新指向根节点
                tem=root;
            }
        }
        //将最后一批计入结果
        stringBuilder.append(text.substring(begin));
        return stringBuilder.toString();
    }

    //判断是否为符号
    private boolean isSymbol(Character c){
        //是特殊字符的话返回true,普通字符返回false         东亚的文字范围
        return !CharUtils.isAsciiAlphanumeric(c)&&(c<0x2E80||c>0x9FFF);
    }


    //描述前缀树的节点
    private class TrieNode{
        //关键词结束的标识
        private boolean isKeyWordEnd=false;

        //当前节点的子节点(key是下级节点的字符,value是下级节点
        private Map<Character,TrieNode>subNodes=new HashMap<>();


        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        //添加子节点的方法
        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c,node);
        }
        //获取子节点的方法
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }



}
