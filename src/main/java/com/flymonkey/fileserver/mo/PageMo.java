package com.flymonkey.fileserver.mo;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: flymonkey
 * Date: 2021/3/30
 * Time: 16:14
 * Description:
 */
public class PageMo {
    private int totalPage;

    private List<FileMo> list;

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<FileMo> getList() {
        return list;
    }

    public void setList(List<FileMo> list) {
        this.list = list;
    }
}
