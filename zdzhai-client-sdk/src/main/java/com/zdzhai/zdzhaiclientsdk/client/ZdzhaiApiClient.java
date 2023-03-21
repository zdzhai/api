package com.zdzhai.zdzhaiclientsdk.client;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.zdzhai.zdzhaiclientsdk.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static com.zdzhai.zdzhaiclientsdk.utils.SignUtil.getSign;

/**
 * api接口调用客户端
 * @author dongdong
 * @Date 2023/3/20 22:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZdzhaiApiClient {

    private String accessKey;

    private String secretKey;

    public String getNameByGet(String name) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);
        String result = HttpUtil.get("http://localhost:8123/api/name/", paramMap);
        System.out.println(result);
        return result;
    }

    public String getNameByPost(String name ){
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);
        String result = HttpUtil.post("http://localhost:8123/api/name/", paramMap);
        System.out.println(result);
        return result;
    }

    public String getNameByJson( User user){
        String json = JSONUtil.toJsonStr(user);
        //链式构建请求
        HttpResponse httpResponse = HttpRequest.post("http://localhost:8123/api/name/user")
                .addHeaders(getHeaderMap(json))
                .body(json)
                .execute();
        System.out.println(httpResponse.getStatus());
        String result = httpResponse.body();
        Console.log(result);
        return result;
    }

    private Map<String, String> getHeaders() {
        Map<String, String> map = new HashMap<>();
        map.put("accessKey",accessKey);
        map.put("secretKey",secretKey);
        return map;
    }

    /**
     * 根据用户传入的参数和密钥通过签名生成算法生成不可解密的值
     * 后端用同样的方法进行加密，然后对比
     * @param body
     * @return
     */
    private Map<String, String> getHeaderMap(String body){
        Map<String, String> map = new HashMap<>();
        map.put("accessKey",accessKey);
//        map.put("secretKey",secretKey);
        map.put("nonce", RandomUtil.randomNumbers(4));
        map.put("body",body);
        map.put("timestamp",String.valueOf(System.currentTimeMillis()));
        map.put("sign", getSign(body,secretKey));
        return map;
    }


}
