package com.escanor.multidatasource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Test {
    public static void main(String[] args) throws FileNotFoundException {
        String a = new String("abc");
        String b = new String("abc");
        System.out.println(a.intern() == b.intern());
        System.out.println(a.equals(b));
        FileInputStream fileInputStream = new FileInputStream("ssss");
        //fileInputStream.skip();
        
    }
}
