package com.flymonkey.fileserver.file;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ReadUtil {
    private final static int[] indexArrary = {9, 0, 1, 2, 3, 4, 5, 6, 7, 8};
    private final static int[] indexArrary2 = {  10 , 1, 2, 3, 4, 5, 6, 7, 8,9};

    public static void main(String[] args) {

        List<byte[]> list = readStreamControlLength("d:/SupportAssistInstaller.exe",10,9,0);
        for (byte[] b:list
             ) {
            System.out.println(WriteUtil.byte2Str(b));
        }
    }
//0,1,2, 0,3,0,0,0,4,0,
    public static List<byte[]> readStreamControlLength(String fileName, int readLength, int end, int start) {
        int startindex = (start + 1) % readLength;
        int startCount = (start + 1) / readLength +( startindex > 0 ? 1 : 0);
        int endindex = (end + 1) % readLength;
        int endCount = (end + 1) / readLength + (endindex > 0 ? 1 : 0);
        startindex = indexArrary[startindex];
        endindex = indexArrary2[endindex];
        int count =0;
        FileInputStream fis = null;
        File file = new File(fileName);

        List<byte[]> list = new ArrayList<>();
        try {
            fis = new FileInputStream(file);
            //数据中转站 临时缓冲区
            int length = 0;
            //循环读取文件内容，输入流中将最多buf.length个字节的数据读入一个buf数组中,返回类型是读取到的字节数。
            //当文件读取到结尾时返回 -1,循环结束。
            byte[] buf = new byte[readLength];
            boolean continue_flag= true;
            boolean append_flag=false;
            while ((length = fis.read(buf)) != -1 && continue_flag) {
                count++;

                int sp=0;
                int el =length;
                if(count ==endCount){
                    el=endindex;
                    continue_flag=false;
                }
                if(count==startCount){
                    append_flag=true;
                    sp=startindex;
                    el -= sp;
                }
                if(append_flag){
                    byte[] temp = new byte[el];
                    System.arraycopy(buf, sp, temp, 0, el);
                    list.add(temp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();//强制关闭输入流
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static List<byte[]> readStream(String fileName, int readLength) {
        FileInputStream fis = null;
        File file = new File(fileName);

        List<byte[]> list = new ArrayList<>();
        try {
            fis = new FileInputStream(file);
            //数据中转站 临时缓冲区
            int length = 0;
            //循环读取文件内容，输入流中将最多buf.length个字节的数据读入一个buf数组中,返回类型是读取到的字节数。
            //当文件读取到结尾时返回 -1,循环结束。
            byte[] buf = new byte[readLength];
            while ((length = fis.read(buf)) != -1) {
                byte[] temp = new byte[readLength];
                System.arraycopy(buf, 0, temp, 0, WriteUtil.BYTE_LENGTH);
                list.add(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();//强制关闭输入流
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static List<String> readFile2List(String filePath, String encoding) throws IOException {
        InputStreamReader brr = new InputStreamReader(new FileInputStream(filePath), encoding);
        BufferedReader br = new BufferedReader(brr);
        String str;
        List<String> list = new ArrayList<>();
        while ((str = br.readLine()) != null) {

            list.add(str);
        }
        return list;

    }

    public static String readFile2Str(String filePath, String encoding) throws IOException {
        InputStreamReader brr = new InputStreamReader(new FileInputStream(filePath), encoding);
        BufferedReader br = new BufferedReader(brr);
        String str;
        StringBuilder sb = new StringBuilder();
        while ((str = br.readLine()) != null) {
            sb.append(str);
        }
        return sb.toString();

    }
}