package cn.wolfcode.util;

import org.apache.commons.codec.digest.DigestUtils;


public class MD5Util {
    public static String encode(String password,String salt){
        return DigestUtils.md5Hex(""+salt.charAt(0)+salt.charAt(2)+password+salt.charAt(4)+salt.charAt(5));
    }

    public static void main(String[] args) {
        String encode = encode("123456", "1a2b3c4d5e");
        System.out.println("encode = " + encode);
    }
}
