package com.example.spiderJd.spider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.spiderJd.pojo.HttpClientResult;
import com.example.spiderJd.pojo.JdData;
import com.example.spiderJd.util.HttpClientUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.util.*;


/**
 * Description: 爬虫
 *
 * @author zj
 * @date 2020/04/06
 */
public class JdSpider {

    public static String ref = "";

    /**
     * 1、获取第一页的结果
     *
     * @param keyword
     * @return
     */
    public static List<JdData> getFirst(String keyword) {
        String url = "https://search.jd.com/Search";

        Map<String, String> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("wq", keyword);
        params.put("enc", "utf-8");
        params.put("pvid", "");

        /**
         *  开始请求
         */
        HttpClientResult res = null;
        try {
            res = HttpClientUtils.doGet(url, params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (res.getCode() != 200) {
            return null;
        }

        /**
         *  解析网页
         */
        List<JdData> jdData = JdSpider.parseHtml(res.getContent());
        ref = res.getUrl();

        /**
         *  获取评论数
         */
        List<JdData> result = JdSpider.getComment(jdData);

        return result;
    }

    /**
     * 2、获取下拉页面的数据
     *
     * @param keyword
     * @return
     */
    public static List<JdData> dropDown(String keyword, int page) {

        /**
         * 构造请求参数
         */
        Map<String, String> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("wq", keyword);
        params.put("enc", "utf-8");
        params.put("page", page * 2 + "");
        params.put("qrst", "1");
        params.put("rt", "1");
        params.put("log_id", "");
        params.put("s", (2 * page - 1) * 27 + "");
        params.put("scrolling", "y");
        params.put("show_items", "");
        params.put("stop", "1");
        params.put("tpl", "3_M");
        params.put("vt", "2");

        List<JdData> result = JdSpider.getData(params);
        return result;

    }

    /**
     * 3、翻页
     * @param keyword
     * @param page
     * @return
     */
    public static List<JdData> getNext(String keyword, int page){
        String url = "https://search.jd.com/s_new.php";
        Map<String, String> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("wq", keyword);
        params.put("enc", "utf-8");
        params.put("page", (2*page+1) + "");
        params.put("qrst", "1");
        params.put("rt", "1");
        params.put("stop", "1");
        params.put("vt", "2");
        params.put("s", 2*page*27+ "");
        params.put("click","0");

        List<JdData> result = JdSpider.getData(params);
        return result;
    }

    /**
     * 4、获取的数据
     * @param keyword
     * @param page
     * @return
     */
    public static List<JdData> getData(Map<String, String> params){
        String url = "https://search.jd.com/s_new.php";
        /**
         *  构造请求头
         */
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36");
        headers.put("Referer", ref);
        headers.put("X-Requested-With", "XMLHttpRequest");

        /**
         *  开始请求
         */
        HttpClientResult res = null;
        try {
            res = HttpClientUtils.doGet(url, headers, params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (res.getCode() != 200) {
            return null;
        }

        /**
         *  解析网页
         */
        List<JdData> jdData = JdSpider.parseHtml(res.getContent());
        ref = res.getUrl();

        /**
         *  获取评论数
         */
        return JdSpider.getComment(jdData);
    }
    /**
     * 5、评论数
     *
     * @param dataList
     * @return
     */
    public static List<JdData> getComment(List<JdData> dataList) {
        StringBuffer stringBuffer = new StringBuffer();
        for (JdData jdData : dataList) {
            stringBuffer.append(jdData.getPid() + ",");
        }
        StringBuffer pids = stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        String url = "https://club.jd.com/comment/productCommentSummaries.action?referenceIds=%s&callback=&_=";
        url = String.format(url, pids);

        /**
         *  开始请求
         */
        HttpClientResult result = null;
        try {
            result = HttpClientUtils.doGet(url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (result.getCode() != 200) {
            return null;
        }


        Map<Long, Integer> map = new HashMap<>();
        JSONObject jsonObject = JSON.parseObject(result.getContent());
        List<JSONObject> commentsCount = (List) jsonObject.get("CommentsCount");

        for (JSONObject jsonObj : commentsCount) {
            Long pid = Long.valueOf(jsonObj.get("ProductId").toString());
            Integer comments = (Integer) jsonObj.get("CommentCount");
            map.put(pid, comments);
        }

        for (JdData jdData : dataList) {
            jdData.setComment(map.get(jdData.getPid()));
        }
        return dataList;
    }

    /**
     * 6、解析html, 用css选择器
     *
     * @return
     */
    public static List<JdData> parseHtml(String content) {
        Document doc = Jsoup.parse(content, "UTF-8");

        List<JdData> dataList = new LinkedList<>();

        Elements elements = doc.select("li.gl-item");
        for (Element element : elements) {
            /**
             *  pid
             */
            String pidStr = element.attr("data-sku");
            if (null == pidStr || pidStr.equals("")) {
                // 将整条网页保存到日志中，以便查看解析失败原因。
                continue;
            }
            Long pid = Long.valueOf(pidStr);

            /**
             *  价格
             */
            String priceStr = element.select("div.p-price").select("strong > i").text();
            if (null == priceStr || priceStr.equals("")) {
                continue;
            }
            Float price = Float.valueOf(priceStr);

            /**
             * 标题
             */
            String title = element.select("div.p-name a").attr("title");

            /**
             *  链接
             */
            String href = element.select("div.p-name a").attr("href");
            JdData jdData = new JdData(pid, price, title, href);
            dataList.add(jdData);

        }
        return dataList;
    }

    public static void main(String[] args) throws InterruptedException {

        String keyword = "蓝牙耳机";
        List<JdData> jdList = JdSpider.getFirst(keyword);

        for (int i = 0; i < 10; i++) {
            jdList.addAll(JdSpider.dropDown(keyword, 1));
            jdList.addAll(JdSpider.getNext(keyword, 1));
            Thread.sleep(2);
        }

        System.out.println(jdList.size());



    }

}
