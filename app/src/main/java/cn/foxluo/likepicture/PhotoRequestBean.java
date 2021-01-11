package cn.foxluo.likepicture;

import java.util.List;

public class PhotoRequestBean {

    /**
     * code : 200
     * data : {"endRow":4,"firstPage":1,"hasNextPage":false,"hasPreviousPage":false,"isFirstPage":true,"isLastPage":true,"lastPage":1,"list":[{"id":26,"name":"嘿嘿，好看","time":1608875235000,"url":"http://www.foxluo.cn/alumni_club-1.0/img/d2b4667d-c85b-4804-8ed1-9611a3df2ba7..jpg"},{"id":27,"name":"嘿嘿，还是好看","time":1608875263000,"url":"http://www.foxluo.cn/alumni_club-1.0/img/cb08341e-be49-4f57-ad42-2b1cd899fb2b..jpg"},{"id":28,"name":"你好漂亮啊，我好喜欢","time":1608875315000,"url":"http://www.foxluo.cn/alumni_club-1.0/img/53888e36-fb7e-40bf-9e67-791bca1759bd..jpg"},{"id":29,"name":"&nbsp","time":1610012376000,"url":"http://www.foxluo.cn/alumni_club-1.0/img/7ffbfbbc-509c-4e11-ba89-f241441c6767..jpg"}],"navigateFirstPage":1,"navigateLastPage":1,"navigatePages":8,"navigatepageNums":[1],"nextPage":0,"orderBy":null,"pageNum":1,"pageSize":5,"pages":1,"prePage":0,"size":4,"startRow":1,"total":4}
     * message : 操作成功
     */

    private int code;
    private DataBean data;
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class DataBean {
        /**
         * endRow : 4
         * firstPage : 1
         * hasNextPage : false
         * hasPreviousPage : false
         * isFirstPage : true
         * isLastPage : true
         * lastPage : 1
         * list : [{"id":26,"name":"嘿嘿，好看","time":1608875235000,"url":"http://www.foxluo.cn/alumni_club-1.0/img/d2b4667d-c85b-4804-8ed1-9611a3df2ba7..jpg"},{"id":27,"name":"嘿嘿，还是好看","time":1608875263000,"url":"http://www.foxluo.cn/alumni_club-1.0/img/cb08341e-be49-4f57-ad42-2b1cd899fb2b..jpg"},{"id":28,"name":"你好漂亮啊，我好喜欢","time":1608875315000,"url":"http://www.foxluo.cn/alumni_club-1.0/img/53888e36-fb7e-40bf-9e67-791bca1759bd..jpg"},{"id":29,"name":"&nbsp","time":1610012376000,"url":"http://www.foxluo.cn/alumni_club-1.0/img/7ffbfbbc-509c-4e11-ba89-f241441c6767..jpg"}]
         * navigateFirstPage : 1
         * navigateLastPage : 1
         * navigatePages : 8
         * navigatepageNums : [1]
         * nextPage : 0
         * orderBy : null
         * pageNum : 1
         * pageSize : 5
         * pages : 1
         * prePage : 0
         * size : 4
         * startRow : 1
         * total : 4
         */

        private int endRow;
        private int firstPage;
        private boolean hasNextPage;
        private boolean hasPreviousPage;
        private boolean isFirstPage;
        private boolean isLastPage;
        private int lastPage;
        private int navigateFirstPage;
        private int navigateLastPage;
        private int navigatePages;
        private int nextPage;
        private Object orderBy;
        private int pageNum;
        private int pageSize;
        private int pages;
        private int prePage;
        private int size;
        private int startRow;
        private int total;
        private List<ListBean> list;
        private List<Integer> navigatepageNums;

        public int getEndRow() {
            return endRow;
        }

        public void setEndRow(int endRow) {
            this.endRow = endRow;
        }

        public int getFirstPage() {
            return firstPage;
        }

        public void setFirstPage(int firstPage) {
            this.firstPage = firstPage;
        }

        public boolean isHasNextPage() {
            return hasNextPage;
        }

        public void setHasNextPage(boolean hasNextPage) {
            this.hasNextPage = hasNextPage;
        }

        public boolean isHasPreviousPage() {
            return hasPreviousPage;
        }

        public void setHasPreviousPage(boolean hasPreviousPage) {
            this.hasPreviousPage = hasPreviousPage;
        }

        public boolean isIsFirstPage() {
            return isFirstPage;
        }

        public void setIsFirstPage(boolean isFirstPage) {
            this.isFirstPage = isFirstPage;
        }

        public boolean isIsLastPage() {
            return isLastPage;
        }

        public void setIsLastPage(boolean isLastPage) {
            this.isLastPage = isLastPage;
        }

        public int getLastPage() {
            return lastPage;
        }

        public void setLastPage(int lastPage) {
            this.lastPage = lastPage;
        }

        public int getNavigateFirstPage() {
            return navigateFirstPage;
        }

        public void setNavigateFirstPage(int navigateFirstPage) {
            this.navigateFirstPage = navigateFirstPage;
        }

        public int getNavigateLastPage() {
            return navigateLastPage;
        }

        public void setNavigateLastPage(int navigateLastPage) {
            this.navigateLastPage = navigateLastPage;
        }

        public int getNavigatePages() {
            return navigatePages;
        }

        public void setNavigatePages(int navigatePages) {
            this.navigatePages = navigatePages;
        }

        public int getNextPage() {
            return nextPage;
        }

        public void setNextPage(int nextPage) {
            this.nextPage = nextPage;
        }

        public Object getOrderBy() {
            return orderBy;
        }

        public void setOrderBy(Object orderBy) {
            this.orderBy = orderBy;
        }

        public int getPageNum() {
            return pageNum;
        }

        public void setPageNum(int pageNum) {
            this.pageNum = pageNum;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getPages() {
            return pages;
        }

        public void setPages(int pages) {
            this.pages = pages;
        }

        public int getPrePage() {
            return prePage;
        }

        public void setPrePage(int prePage) {
            this.prePage = prePage;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getStartRow() {
            return startRow;
        }

        public void setStartRow(int startRow) {
            this.startRow = startRow;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public List<ListBean> getList() {
            return list;
        }

        public void setList(List<ListBean> list) {
            this.list = list;
        }

        public List<Integer> getNavigatepageNums() {
            return navigatepageNums;
        }

        public void setNavigatepageNums(List<Integer> navigatepageNums) {
            this.navigatepageNums = navigatepageNums;
        }

        public static class ListBean {
            /**
             * id : 26
             * name : 嘿嘿，好看
             * time : 1608875235000
             * url : http://www.foxluo.cn/alumni_club-1.0/img/d2b4667d-c85b-4804-8ed1-9611a3df2ba7..jpg
             */

            private int id;
            private String name;
            private long time;
            private String url;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public long getTime() {
                return time;
            }

            public void setTime(long time) {
                this.time = time;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
        }
    }
}
