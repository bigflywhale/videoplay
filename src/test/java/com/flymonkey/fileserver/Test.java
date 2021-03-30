package com.flymonkey.fileserver;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: flymonkey
 * Date: 2021/3/30
 * Time: 11:21
 * Description:
 */
public class Test {
    public static void main(String[] args) {
        ContentInfo info1 = ContentInfoUtil.findExtensionMatch("123.mp3");
        ContentInfo info2 = ContentInfoUtil.findExtensionMatch("123.mpg");
        String contentType1 = info1.getMimeType();
        String contentType2 = info2.getMimeType();
        System.out.println(contentType1);
        System.out.println(contentType2);
    }
}
