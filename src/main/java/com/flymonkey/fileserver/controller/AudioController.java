package com.flymonkey.fileserver.controller;

import com.flymonkey.fileserver.decrypt.Md5Util;
import com.flymonkey.fileserver.file.ReadUtil;
import com.flymonkey.fileserver.file.WriteUtil;
import com.flymonkey.fileserver.mo.FileMo;
import com.flymonkey.fileserver.mo.PageMo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.util.*;

@Controller
@RequestMapping("/audio")
public class AudioController {


    @GetMapping(value = "/welcome")
    public ModelAndView test(HttpServletRequest req) throws Exception {
        ModelAndView mv = new ModelAndView();
        String index = req.getParameter("index");
        String code = req.getParameter("code");
        String md5 = Md5Util.encrypt(code);
        if(!md5.equals(Md5Util.MD5_STR)){
            return null;
        }
        int count = 1;
        try {
            count = Integer.valueOf(index);
        } catch (Exception e) {
        }
        PageMo mo = WriteUtil.listFileName(count - 1);
        mv.addObject("list", mo.getList());
        mv.addObject("text", "第" + count + "页，共" + mo.getTotalPage() + "页");

        mv.setViewName("/audio/audio");
        return mv;
    }

    @RequestMapping("/download2")
    public void downloadFile2(HttpServletRequest request, HttpServletResponse response) throws IOException {
        File file = new File("e:\\fileserverdir\\origin\\11.mp3");
        String fileName = file.getName();
        sendVideo(request, response, file, fileName);
    }

    private void sendVideo(HttpServletRequest request, HttpServletResponse response, File file, String fileName) throws FileNotFoundException, IOException {
        RandomAccessFile randomFile = new RandomAccessFile(file, "r");//只读模式
        long contentLength = randomFile.length();
        String range = request.getHeader("Range");
        int start = 0, end = 0;
        if (range != null && range.startsWith("bytes=")) {
            String[] values = range.split("=")[1].split("-");
            start = Integer.parseInt(values[0]);
            if (values.length > 1) {
                end = Integer.parseInt(values[1]);
            }
        }
        int requestSize = 0;
        if (end != 0 && end > start) {
            requestSize = end - start + 1;
        } else {
            requestSize = Integer.MAX_VALUE;
        }

        response.setContentType("audio/mpeg");
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("ETag", fileName);
        response.setHeader("Last-Modified", new Date().toString());
        //第一次请求只返回content length来让客户端请求多次实际数据
        if (range == null) {
            response.setHeader("Content-length", contentLength + "");
        } else {
            //以后的多次以断点续传的方式来返回视频数据
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);//206
            long requestStart = 0, requestEnd = 0;
            String[] ranges = range.split("=");
            if (ranges.length > 1) {
                String[] rangeDatas = ranges[1].split("-");
                requestStart = Integer.parseInt(rangeDatas[0]);
                if (rangeDatas.length > 1) {
                    requestEnd = Integer.parseInt(rangeDatas[1]);
                }
            }
            long length = 0;
            if (requestEnd > 0) {
                length = requestEnd - requestStart + 1;
                response.setHeader("Content-length", "" + length);
                response.setHeader("Content-Range", "bytes " + requestStart + "-" + requestEnd + "/" + contentLength);
            } else {
                length = contentLength - requestStart;
                response.setHeader("Content-length", "" + length);
                response.setHeader("Content-Range", "bytes " + requestStart + "-" + (contentLength - 1) + "/" + contentLength);
            }
        }
        ServletOutputStream out = response.getOutputStream();
        int needSize = requestSize;
        randomFile.seek(start);
        System.out.println("download2:" + start + "-" + needSize);

        while (needSize > 0) {
            byte[] buffer = new byte[4096];
            int len = randomFile.read(buffer);
            if (needSize < buffer.length) {
                out.write(buffer, 0, needSize);
            } else {
                out.write(buffer, 0, len);
                if (len < buffer.length) {
                    break;
                }
            }
            needSize -= buffer.length;
        }
        Collection<String> names = response.getHeaderNames();
        for (String s : names
        ) {
            System.out.println("download2 " + s + ":" + response.getHeader(s));
        }
        System.out.println("download2 content-Type: " + response.getContentType());

        randomFile.close();
        out.close();

    }

    @RequestMapping("/download")
    public void downloadFile1(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //设置文件路径
        String realPath = request.getParameter("filename");//index文件路径
        String realname = request.getParameter("realname");
        String lengthStr = request.getParameter("fileLength");//实际文件总长度
        int fileLength = Integer.valueOf(lengthStr);

        realPath = URLDecoder.decode(realPath, "utf-8");
        if (realname.endsWith(".m4a")) {
            response.setHeader("Content-Type", "audio/mp4a-latm");
        } else {

            response.setHeader("Content-Type", "audio/mpeg");
        }
        String rangeString = request.getHeader("Range");//如果是video标签发起的请求就不会为null
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        String content = ReadUtil.readFile2Str(WriteUtil.INDEX_DIR + realPath, "gbk");
        String[] indexContentArray = content.split("#");//分割index文件内容
        String content_range = "";
        String content_length = "";
        int requestStart = 0, requestEnd = 0;

        if (rangeString == null) {
            content_length = lengthStr;
            requestEnd = fileLength;
            response.setHeader("Content-Length", content_length);
        } else {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            String[] ranges = rangeString.split("=");
            if (ranges.length > 1) {
                String[] rangeDatas = ranges[1].split("-");
                requestStart = Integer.parseInt(rangeDatas[0]);
                if (rangeDatas.length > 1) {
                    requestEnd = Integer.parseInt(rangeDatas[1]);
                }
            }
            if (requestEnd == 0) {
                requestEnd = fileLength - 1;
            }
            content_length = requestEnd - requestStart + 1 + "";
            content_range = "bytes " + requestStart + "-" + requestEnd + "/" + fileLength;
            response.setHeader("Content-Range", content_range);
            response.setHeader("Content-Length", content_length);
        }
        response.setHeader("Etag", Md5Util.encrypt(realPath));
        response.setHeader("Accept-Ranges", "bytes");
        OutputStream os = response.getOutputStream();

        int startindex = requestStart % WriteUtil.BYTE_LENGTH;//开始点所在位置（包含）
        int endindex = requestEnd % WriteUtil.BYTE_LENGTH;//结束点所在位置（包含）
        int startCount = requestStart / WriteUtil.BYTE_LENGTH;//开始点所在读取次数（从0次开始）
        int endCount = requestEnd / WriteUtil.BYTE_LENGTH;//结束点所在读取次数（从0次开始）
        int count = 0;
        boolean break_flag = false;
        boolean append_flag = false;


        for (int i = 2; i < indexContentArray.length; i++) {//前两个分别为文件名的RSA以及文件大小
            String cutFilePath = indexContentArray[i];//cut文件路径
            List<byte[]> cutFileContent = ReadUtil.readStream(WriteUtil.CUT_DIR + cutFilePath, WriteUtil.BYTE_LENGTH);
            for (int j = cutFileContent.size(); j > 0; j--) {//倒着读取
                int byteLenth = cutFileContent.get(j - 1).length;
                int startPosition = 0;
                int copyLenth = byteLenth;
                if (count == endCount) {
                    copyLenth = endindex + 1;
                    break_flag = true;
                }
                if (count == startCount) {
                    append_flag = true;
                    startPosition = startindex;
                    copyLenth -= startPosition;
                }
                if (append_flag) {
                    os.write(cutFileContent.get(j - 1), startPosition, copyLenth);
                }
                count++;
                if (break_flag) {
                    break;
                }
            }
            if (break_flag) {
                break;
            }
        }
        os.close();
    }

    @RequestMapping("/download3")
    public void downloadFile3(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //设置文件路径


        response.setHeader("Content-Type", "application/zip");
        response.addHeader("Content-Disposition", "attachment;fileName=1.zip");// 设置文件名

        OutputStream os = response.getOutputStream();

        FileInputStream fileInputStream = new FileInputStream(new File("D:\\file\\fileserver.zip"));

        int length = 0;
        byte[] bytes = new byte[1024 * 8];
        while ((length = fileInputStream.read(bytes)) != 0) {
            os.write(bytes);
        }
        os.close();
    }

    public void write(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        for (int i = 0; i < len; i++) {
            System.out.print(b[off + i] + ",");
        }
    }
}
