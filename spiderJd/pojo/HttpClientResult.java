package com.example.spiderJd.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Description: 封装httpClient响应结果
 *
 * @author zj
 * @date 2020/04/06
 */

@Data
@AllArgsConstructor
public class HttpClientResult {
    private static final long serialVersionUID = 2416815219416478390L;

    /**
     * 响应状态码
     */
    private int code;

    /**
     * 响应数据
     */
    private String content;

    private String url;

    public HttpClientResult(int code) {
        this.code = code;
    }

    public HttpClientResult(String content) {
        this.content = content;
    }

}
