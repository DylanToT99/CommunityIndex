package com.newcode.community.entity;

import lombok.Data;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/2/17 20:16
 * @description 封装分页条件
 **/

public class Page {
    //当前的页码
    private int current=1;

    //一页显示的数量上限
    private int limit=10;

    //数据的总条数(用于计算总页数)
    private int rows;

    //查询路径(复用分页链接)
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        //传入的页码数大于等于1才能认为当前页有效
        if(current>=1){
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        //一页的数量最少为1,最大为100
        if(limit>=1&&limit<=100){
             this.limit = limit;
        }

    }

    public int getRows() {

        return rows;
    }

    public void setRows(int rows) {
        if (rows>=0){
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页的起始行
     * @return
     */
    public int getOffset(){
        //current*limit-limit
        return (current-1)*limit;
    }

    /**
     * 获取总页数
     * @return
     */
    public int getTotalPageCounts(){
        if(rows%limit==0){
            return rows/limit;
        }else{
            return rows/limit+1;
        }
    }

    /**
     * 获取起始页码
     */
    public int getFrom(){
        int from=current-2;
        return from<1?1:from;
    }
    /**
     * 获取结束页码
     */
    public int getEnd(){
        int end=current+2;
        int total=getTotalPageCounts();
        return end>total?total:end;
    }
}
