package com.example.spiderJd.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Description: 网页解析结果
 *
 * @author zj
 * @date 2020/04/06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JdData {
    /**
     *  数据id
     */
    private Long pid;
    /**
     * 价格
     */
    private Float price;
    /**
     *  标题
     */
    private String title;
    /**
     * 链接
     */
    private String href;
    /**
     *  评论数
     */
    private Integer comment;


    public JdData(Long pid, Float price, String title, String href) {
        this.pid = pid;
        this.price = price;
        this.title = title;
        this.href = href;
    }
}
