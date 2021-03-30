package com.flymonkey.fileserver.file;

import com.flymonkey.fileserver.decrypt.Md5Util;
import com.flymonkey.fileserver.decrypt.RSAUtil;
import com.flymonkey.fileserver.mo.FileMo;
import com.flymonkey.fileserver.mo.PageMo;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class WriteUtil {
    public final static String SEPARATOR = java.io.File.separator;
    public static final String DATA_DIR = "D:\\fileserver";
    public static final String DESTINATION_DIR = DATA_DIR + SEPARATOR + "destination";
    public static final String ORIGIN_DIR_OR_FILE = DATA_DIR + SEPARATOR + "origin";
    public static final String INDEX_DIR = DESTINATION_DIR + SEPARATOR + "index";
    public static final String SUM_INDEX_FILE_NAME = "sumindex";
    public static final String SUM_INDEX_FILE = INDEX_DIR + SEPARATOR + SUM_INDEX_FILE_NAME;
    public static final String CUT_DIR = DESTINATION_DIR + SEPARATOR + "cut";
    public static final String REVERSE_DIR = DESTINATION_DIR + SEPARATOR + "reverse";

    public static File ORIGINFILE;
    public static File SUM_INDEX_ORIGINFILE;
    public static final String FILE_ENCODING = System.getProperty("file.encoding");

    public static final int SPLIT_LENGTH = 1024;
    public static final int BYTE_LENGTH = 1024;
    public static char[] FILE_SUFFIX_ARRARY = new char[36];

    static {
        int index = 0;
        for (char i = 48; i < 58; i++) {
            FILE_SUFFIX_ARRARY[index] = i;
            index++;
        }
        for (char i = 65; i < 91; i++) {
            FILE_SUFFIX_ARRARY[index] = i;
            index++;
        }
        ORIGINFILE = new File(ORIGIN_DIR_OR_FILE);
        SUM_INDEX_ORIGINFILE = new File(SUM_INDEX_FILE);

    }

    public static String int236(int value) {
        StringBuilder sb = new StringBuilder();
        int index = value % 36;
        sb.append(FILE_SUFFIX_ARRARY[index]);
        while ((value /= 36) > 0) {
            index = value % 36;
            sb.insert(0, FILE_SUFFIX_ARRARY[index]);
        }
        return sb.toString();
    }

    public static void  getAllChildFile(File file, List<File> list){

        if(file.isDirectory()){
            File[] files = file.listFiles();
            for (File f:files
                 ) {
                if(f.isDirectory()){
                    getAllChildFile(f,list);
                }else{
                    list.add(f);
                }
            }
        }

    }
    public static void replacePath(String indexdir,String org, String newStr) throws IOException {
        File file = new File(indexdir);
        List<File> list = new ArrayList<>();
        getAllChildFile(file,list);


        for (File f : list
        ) {
            String content = ReadUtil.readFile2Str(f.getAbsolutePath(), "gbk");
             content =content.replace(org,newStr);
             WriteUtil.replaceFile(f.getAbsolutePath(),content);
        }

    }
    public static void oldIndex2new(String indexdir, String sumIndexDir) throws IOException {
        File file = new File(indexdir);
        List<File> list = new ArrayList<>();
         getAllChildFile(file,list);

        StringBuilder sb_sum_index = new StringBuilder();
        for (File f : list
        ) {
            String content = ReadUtil.readFile2Str(f.getAbsolutePath(), "gbk");
            String[] childIndex = content.split("#");
            String rsaName = null;
            try {
                rsaName = RSAUtil.encrypt(childIndex[0], RSAUtil.publicKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int length = 0;
            StringBuilder sb2 = new StringBuilder();
            sb2.append(rsaName).append("#");
            for (int i = 1; i < childIndex.length; i++) {
                String s = childIndex[i];
                List<byte[]> result = ReadUtil.readStream(s, BYTE_LENGTH);
                sb2.append(s).append("#");
                for (byte[] b : result) {
                    length += b.length;
                }

            }
            sb2.insert(0,"#").insert(0,length);
            WriteUtil.replaceFile(f.getAbsolutePath(), sb2.toString());
            sb_sum_index.append(f.getAbsolutePath()).append("#");
            sb_sum_index.append(rsaName).append("#");
            sb_sum_index.append(length).append("\r\n");
        }
        WriteUtil.append2File(sumIndexDir, sb_sum_index.toString());
    }

    public static String byte2Str(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes
        ) {
            sb.append(b).append(",");
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
//        sum2Split();
//        oldIndex2new(INDEX_DIR,SUM_INDEX_FILE);
//        replacePath(INDEX_DIR,"d:\\fileserver\\cut","d:\\fileserver\\destination\\cut");
        append2SumIndex();
    }

    public static void append2SumIndex() throws IOException {

        File file = new File(INDEX_DIR);
        List<File> list = new ArrayList<>();
        getAllChildFile(file,list);
        StringBuilder sb = new StringBuilder();
        for (File f : list) {
            if (SUM_INDEX_FILE_NAME.equals(f.getName())) {
                continue;
            }
            String s = ReadUtil.readFile2Str(f.getPath(), FILE_ENCODING);
            sb.append(f.getAbsolutePath());
            sb.append("#");
            String[] arrary = s.split("#");
            sb.append(arrary[1]);
            sb.append("#");
            sb.append(arrary[0]);
            sb.append("\r\n");
        }
        WriteUtil.append2File(SUM_INDEX_FILE, sb.toString());
    }

    public static void fileName2RSA() throws Exception {
        File index = new File(INDEX_DIR);
        File[] index_files = index.listFiles();
        for (File f : index_files) {
            if (SUM_INDEX_FILE_NAME.equals(f.getName())) {
                continue;
            }
            String s = ReadUtil.readFile2Str(f.getPath(), FILE_ENCODING);
            String[] arrary = s.split("#");
            s = s.replace(arrary[0], RSAUtil.encrypt(arrary[0], RSAUtil.publicKey));
            WriteUtil.replaceFile(f.getAbsolutePath(), s);
        }
    }

    public static void sum2Split() throws Exception {
        StringBuilder sb = new StringBuilder();
        if (ORIGINFILE.isDirectory()) {
            File[] files = ORIGINFILE.listFiles();
            for (File file : files) {
                splitFile(file, CUT_DIR, INDEX_DIR, sb);
            }
        } else {
            File file = ORIGINFILE;
            splitFile(file, CUT_DIR, INDEX_DIR, sb);
        }
        WriteUtil.append2File(SUM_INDEX_FILE, sb.toString());
    }

    public static void split2Sum() throws IOException {
        sumFile(INDEX_DIR, REVERSE_DIR);
    }

    public static void splitFile(File originFile, String destination_path_cut, String destination_path_index, StringBuilder sumIndexSb) throws Exception {
        String origin = originFile.getAbsolutePath();
        List<byte[]> list = ReadUtil.readStream(origin, BYTE_LENGTH);
        List<byte[]> temp = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(RSAUtil.encrypt(originFile.getName(), RSAUtil.publicKey)).append("#");
        } catch (Exception e) {
            e.printStackTrace();
        }
        long fileLength = 0l;
        //1m一个文件
        for (int i = 0; i < list.size(); i++) {
            if (i % SPLIT_LENGTH == 0 && i != 0) {
                String fileName = genFileName(destination_path_cut, temp);
                sb.append(fileName).append("#");
                writeStreamReverse(temp, fileName);
                temp = new ArrayList<>();
            }
            fileLength += list.get(i).length;
            temp.add(list.get(i));
        }
        if (temp != null && temp.size() > 0) {
            String fileName = genFileName(destination_path_cut, temp);
            sb.append(fileName).append("#");
            writeStreamReverse(temp, fileName);
        }
        sb.insert(0, "#").insert(0, fileLength);

        List<byte[]> indexList = new ArrayList<>();
        indexList.add(sb.toString().getBytes());
        String fileName = genFileName(destination_path_index, indexList);

        append2File(fileName, sb.toString());
        sumIndexSb.append(fileName).append("#");
        sumIndexSb.append(RSAUtil.encrypt(originFile.getName(), RSAUtil.publicKey)).append("#");
        sumIndexSb.append(fileLength).append("#").append("\r\n");
    }

    /**
     * sumindex:
     * 每行：一个index文件的描述
     * 每行格式 index文件路径#原始文件rsa加密#原始文件大小
     *
     * index文件格式：原始文件大小#原始文件rsa加密#cut文件路径#……
     *
     */

    public static void sumFile(String destination_path_index, String destination_path_index_reverse) throws IOException {
        File indexFile = new File(destination_path_index);
        List<String> indexFileList = new ArrayList<>();
        File[] indexDirs = indexFile.listFiles();
        for (File f : indexDirs
        ) {
            File[] fs = f.listFiles();
            for (File ff : fs
            ) {
                indexFileList.add(ff.getAbsolutePath());
            }
        }
        for (String str : indexFileList
        ) {
            String content = ReadUtil.readFile2Str(str, "gbk");
            String[] childIndex = content.split("#");
            String reverseName = null;
            try {
                reverseName = RSAUtil.decrypt(childIndex[0], RSAUtil.privatKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
            List<byte[]> sum = new ArrayList<>();
            for (int i = 1; i < childIndex.length; i++) {
                String s = childIndex[i];
                List<byte[]> result = ReadUtil.readStream(s, BYTE_LENGTH);
                for (int j = result.size(); j > 0; j--) {
                    sum.add(result.get(j - 1));
                }

            }

            WriteUtil.writeStream(sum, destination_path_index_reverse + SEPARATOR + reverseName);
        }
    }

    public static PageMo listFileName(int count) throws Exception {
        PageMo pageMo = new PageMo();
        List<FileMo> list = new ArrayList<>();
        pageMo.setList(list);
        File file = new File(SUM_INDEX_FILE);
        List<String> strs = ReadUtil.readFile2List(SUM_INDEX_FILE, FILE_ENCODING);
        for (int i = 10 * count; i < 10 + 10 * count && i < strs.size(); i++
        ) {
            String str = strs.get(i);
            String[] arrary = str.split("#");
            FileMo mo = new FileMo();
            mo.setRealName(URLEncoder.encode(arrary[0], "utf-8"));
            mo.setShowName(RSAUtil.decrypt(arrary[1], RSAUtil.privatKey));
            mo.setFileLength(Long.valueOf(arrary[2]));
            list.add(mo);
        }
        int length = strs.size();
        int totalPage = length/10+(length%10 >0?1:0);
        pageMo.setTotalPage(totalPage);
        return pageMo;
    }


    /**
     * 根据文件md5获取文件目标地址
     *
     * @param diskVolumn
     * @param list
     * @return
     */
    public static String genFileName(String diskVolumn, List<byte[]> list) {
        String fileMd5 = Md5Util.getMD5OfByteList(list);
        String prefix = fileMd5.substring(0, 3);
        String fileName = fileMd5.substring(3);
        StringBuilder sb = new StringBuilder(diskVolumn).append(SEPARATOR).append(prefix).append(SEPARATOR).append(fileName);
        File file;
        int index = 0;

        String str = sb.toString();
        while ((file = new File(str)).exists()) {
            str = sb.toString() + "-" + int236(index);
            index++;
        }
        return file.getAbsolutePath();

    }

    public static void append2File(String fileName, String content) {
        File fileOUT = new File(fileName); //定义输出文件
        if (!fileOUT.getParentFile().exists()) {
            fileOUT.getParentFile().mkdirs();
        }
        try {
            //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void replaceFile(String fileName, String content) {
        File fileOUT = new File(fileName); //定义输出文件
        if (!fileOUT.getParentFile().exists()) {
            fileOUT.getParentFile().mkdirs();
        }
        try {
            //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, false);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 正向输出
     *
     * @param list
     * @param fileName
     */
    public static void writeStream(List<byte[]> list, String fileName) {

        File fileOUT = new File(fileName); //定义输出文件
        if (!fileOUT.getParentFile().exists()) {
            fileOUT.getParentFile().mkdirs();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileOUT); //输出流连接到输出文件
            for (byte[] bs : list
            ) {
                fos.write(bs, 0, bs.length);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        //关闭流
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 反向输出
     *
     * @param list
     * @param fileName
     */
    public static void writeStreamReverse(List<byte[]> list, String fileName) {

        File fileOUT = new File(fileName); //定义输出文件
        if (!fileOUT.getParentFile().exists()) {
            fileOUT.getParentFile().mkdirs();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileOUT); //输出流连接到输出文件
            for (int i = list.size(); i > 0; i--
            ) {
                byte[] bs = list.get(i - 1);
                fos.write(bs, 0, bs.length);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        //关闭流
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

