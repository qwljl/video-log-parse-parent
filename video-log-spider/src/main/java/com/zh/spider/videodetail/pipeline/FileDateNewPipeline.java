package com.zh.spider.videodetail.pipeline;

import com.zh.spider.videodetail.entity.VideoDetails;
import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

/**
 * @author zhouhao
 * @version 1.0
 * @date 2020/2/19 00:25
 * @Description 用于持久化数据
 */
@Slf4j
public class FileDateNewPipeline implements Pipeline {

    private static final String PATH_SEPERATOR = "/";

    private String path;

    public FileDateNewPipeline(String path) {
        this.path = path;
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        VideoDetails videoDetails = resultItems.get("videoDetails");
        if (Objects.isNull(videoDetails)) {
            return;
        }
        String path = this.path + PATH_SEPERATOR;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        LocalDateTime now = LocalDateTime.now();
        String nowStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path
                + "access.log."
                + nowStr
                , true), "utf-8"))) {
            bw.write(videoDetails.toString());
            bw.newLine();
            bw.flush();
        } catch (Exception e) {
            log.warn("write file error", e);
        }
    }

    public static void main(String[] args) {
       /* LocalDateTime now = LocalDateTime.parse("1581825622");
        String nowStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));*/
        long pubDate = 1581825622;
        Date date = new Date(pubDate * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        /*Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zoneId);
        String nowStr = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
*/
        System.out.println(simpleDateFormat.format(date));
    }
}
