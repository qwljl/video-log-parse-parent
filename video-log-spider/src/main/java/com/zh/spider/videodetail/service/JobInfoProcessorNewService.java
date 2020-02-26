package com.zh.spider.videodetail.service;

import com.zh.spider.videodetail.entity.VideoDetails;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Json;
import us.codecraft.webmagic.selector.Selectable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author zhouhao
 * @version 1.0
 * @date 2020/2/18 13:17
 * @Description 页面详情页处理
 */
//https://api.bilibili.com/x/web-interface/view?aid=88304073&cid=151930706
//https://api.bilibili.com/x/web-interface/view?aid=88304073

/*https://s.search.bilibili.com/cate/search?callback=jqueryCallback_bili_04516996634956216&main_ver=v3&search_type=video&view_type=hot_rank&order=click&copy_right=-1&cate_id=24&page=1
 &pagesize=20&jsonp=jsonp&time_from=20200211&time_to=20200218&_=1582032132311*/
@Slf4j
public class JobInfoProcessorNewService implements PageProcessor {
    /**
     * 视频详情页接口
     */
    private static final String URL_DETAIL_PREFIX = "https://api.bilibili.com/x/web-interface/view?aid=";


    @Override
    public void process(Page page) {
        try {
            //列表页
            List<Selectable> resultNodes = page.getJson().removePadding("callback").jsonPath("$.result[*]").nodes();
            if (CollectionUtils.isNotEmpty(resultNodes)) {
                for (Selectable resultNode : resultNodes) {
                    Json resultJson = new Json(resultNode.get());
                    String rankScore = resultJson.jsonPath("$.rank_score").get();
                    String arcurl = resultJson.jsonPath("$.arcurl").get();
                    String pubdate = resultJson.jsonPath("$.pubdate").get();
                    String aid = resultJson.jsonPath("$.id").get();
                    //防止aid不存在
                    if(StringUtils.isNotBlank(aid)){
                        Map<String, Object> map = new HashMap<>();
                        map.put("videoUrl", arcurl);
                        map.put("rankScore", rankScore);
                        map.put("pubdate", pubdate);
                        arcurl = URL_DETAIL_PREFIX + aid;
                        Request request = new Request(arcurl);
                        request.setExtras(map);
                        page.addTargetRequest(request);
                    }
                   /* int index = arcurl.lastIndexOf("av");
                    if (index != -1) {
                        arcurl = URL_DETAIL_PREFIX + arcurl.substring(index + 2);
                        Request request = new Request(arcurl);
                        request.setExtras(map);
                        page.addTargetRequest(request);
                    }*/
                }
                String numPages = page.getJson().removePadding("callback").jsonPath("$.numPages").get();
                String curPage = page.getJson().removePadding("callback").jsonPath("$.page").get();
                int cur_page = Integer.parseInt(curPage);
                if (cur_page < Integer.parseInt(numPages)) {
                    //翻页
                    cur_page++;
                    String pageListUrl = page.getRequest().getUrl();
                    int index = pageListUrl.lastIndexOf("&page=");
                    //防止由于不断追加新的&page=导致的url过长
                    if (index > -1) {
                        //舍去&page=
                        pageListUrl = pageListUrl.substring(0, index);
                    }
                    pageListUrl = pageListUrl + "&page=" + cur_page;
                    //添加到Scheduler队列继续发送请求
                    page.addTargetRequest(pageListUrl);
                }
            }
        } catch (Exception e) {
            //jsonPath的第二级节点没找到会抛出异常此时肯定不在列表页那就是详情页了
            //详情页
            getVideodetails(page);
        }

    }

    //详情页处理
    private void getVideodetails(Page page) {
        Json json = page.getJson();
        String code = json.jsonPath("$.code").get();
        if (!code.equals("0")) {
            String message = json.jsonPath("$.message").get();
            log.error("爬取视频详情时出错,错误信息: [{}],视频地址:[{}]", message,page.getRequest().getUrl());
            return;
        }
        //获取视频ID
        String videoAid = json.jsonPath("$.data.aid").get();

        //获取视频名字
        String videoName = json.jsonPath("$.data.title").get();
        /**
         * 获取发布日期，具体时间
         * 例如:2020-02-12 22:00:15
         */
        //String publishDate = json.jsonPath("$.data.pubdate").get();
        String publishDate = (String) Optional.ofNullable(page.getRequest().getExtra("pubdate")).orElse("");
        /**
         * 博主ID
         */
        String videoAuthorMid = json.jsonPath("$.data.owner.mid").get();
        /**
         * 视频博主
         */
        String videoAuthor = json.jsonPath("$.data.owner.name").get();
        /**
         * 视频地址
         */
        String videoUrl = (String) Optional.ofNullable(page.getRequest().getExtra("videoUrl")).orElse("");
        /**
         * 综合得分
         */
        String rankScore = (String) Optional.ofNullable(page.getRequest().getExtra("rankScore")).orElse("");
        /**
         * 播放量
         */
        String playCount = json.jsonPath("$.data.stat.view").get();
        /**
         *  弹幕数
         */
        String danmakuCount = json.jsonPath("$.data.stat.danmaku").get();
        /**
         * 点赞数
         */
        String videoLikeCount = json.jsonPath("$.data.stat.like").get();
        /**
         * 投硬币数
         */
        String videoCoinCount = json.jsonPath("$.data.stat.coin").get();
        /**
         * 收藏人数
         */
        String videoCollectCount = json.jsonPath("$.data.stat.favorite").get();
        /**
         * 转发人数
         */
        String videoShareCount = json.jsonPath("$.data.stat.share").get();
        /**
         *  评论数
         */
        String commentCount = json.jsonPath("$.data.stat.reply").get();
        /**
         * 视频分类ID  详细分类
         */
        //TODO 待定,需要改成粗分类
        String videoTypeId = json.jsonPath("$.data.tid").get();
        /**
         * 视频分类名字
         */
        String videoTypeName = json.jsonPath("$.data.tname").get();

        VideoDetails videoDetails = new VideoDetails();
        videoDetails.setVideoAid(videoAid);
        videoDetails.setVideoName(videoName);
        videoDetails.setVideoUrl(videoUrl);
        videoDetails.setPlayCount(playCount);
        videoDetails.setDanmakuCount(danmakuCount);
        videoDetails.setCommentCount(commentCount);
        videoDetails.setVideoAuthor(videoAuthor);
        videoDetails.setVideoAuthorMid(videoAuthorMid);
        videoDetails.setVideoLikeCount(videoLikeCount);
        videoDetails.setVideoCoinCount(videoCoinCount);
        videoDetails.setVideoCollectCount(videoCollectCount);
        videoDetails.setVideoShareCount(videoShareCount);
        videoDetails.setRankScore(rankScore);
        videoDetails.setPublishDate(publishDate);
        videoDetails.setVideoTypeId(videoTypeId);
        videoDetails.setVideoTypeName(videoTypeName);
        //添加到resultItems
        page.putField("videoDetails", videoDetails);
    }

    //爬虫配置
    private Site site = Site.me()
            .setDomain("bilibili.com")//设置域名，需设置域名后，addCookie才可生效
            .setCharset("utf-8") //按照哪种字符集进行读取
            .setSleepTime(2000)
            .setTimeOut(10000)//超时时间 毫秒
            .setRetrySleepTime(3000)//重试间隔时间 毫秒
            .setRetryTimes(3);//重试次数

    @Override
    public Site getSite() {
        return site;
    }

}