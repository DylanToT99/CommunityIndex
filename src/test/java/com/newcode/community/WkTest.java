package com.newcode.community;

import java.io.IOException;

/**
 * @author Dylan
 * @version 1.0
 * @date 2023/3/1 14:20
 * @description TODO
 **/
public class WkTest {
    public static void main(String[] args) {
        String cmd="e:/2023/wkhtmltopdf/bin/wkhtmltoimage --quality 75 https://www.nowcoder.com/ E:/2023/wkhtmltopdf/wk-image/6.png";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
