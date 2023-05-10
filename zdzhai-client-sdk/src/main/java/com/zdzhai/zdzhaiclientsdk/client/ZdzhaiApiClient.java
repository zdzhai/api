package com.zdzhai.zdzhaiclientsdk.client;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.zdzhai.apicommon.common.ErrorCode;
import com.zdzhai.apicommon.model.dto.ApiDTO;
import com.zdzhai.apicommon.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
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
public class ZdzhaiApiClient<T> {

    private String accessKey;

    private String secretKey;

    private static final String GATEWAY_HOST = "http://localhost:8090";

    /**
     * 方法名和参数封装类建立map
     */
    public static Map<String,Class> objectMap = new HashMap<>();
    static {
        objectMap.put("getNameByPost",com.zdzhai.apicommon.model.requestbody.User.class);
        objectMap.put("getNameByGet",String.class);
        objectMap.put("getName",String.class);
        objectMap.put("getTuwei",String.class);
        objectMap.put("getJitang",String.class);
    }

    /**
     * 处理所有的接口请求
     * @param apiDTO
     * @return
     */
    public String handleInvoke(ApiDTO apiDTO){
        String name = apiDTO.getName();
        String method = apiDTO.getMethod();
        String requestParams = apiDTO.getRequestParams();
        Class<ZdzhaiApiClient> zdzhaiApiClientClass = ZdzhaiApiClient.class;
        //POST请求 请求参数必须为对象 @RequestBody 对请求参数做统一封装
        if ("POST".equals(method)||"post".equals(method)) {
            Gson gson = new Gson();
            Object obj = gson.fromJson(requestParams, objectMap.get(name));
            try {
                //使用map来对调用方法和封装对象做映射
                //也可以是配置文件
                Method realMethod = zdzhaiApiClientClass.getMethod(name, objectMap.get(name));
                Object invokeRes = realMethod.invoke(this, obj);
                return (String) invokeRes;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if ("GET".equals(method)||"get".equals(method)) {
            try {
                Method realMethod = zdzhaiApiClientClass.getMethod(name, objectMap.get(name));
                Object invokeRes = realMethod.invoke(this, requestParams);
                return (String) invokeRes;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ErrorCode.SYSTEM_ERROR.getMessage();
    }

    public String getNameByGet(String name) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);
        HttpResponse httpResponse = HttpRequest.get(GATEWAY_HOST + "/api/name/get")
                .addHeaders(getHeaderMap(name))
                .body(name)
                .execute();
        return httpResponse.body();
    }

    public String getTuwei(String id) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", id);
        HttpResponse httpResponse = HttpRequest.get(GATEWAY_HOST + "/api/get/tuwei")
                .addHeaders(getHeaderMap(id))
                .body(id)
                .execute();
        return httpResponse.body();
    }

    public String getJitang(String id) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", id);
        HttpResponse httpResponse = HttpRequest.get(GATEWAY_HOST + "/api/get/jitang")
                .addHeaders(getHeaderMap(id))
                .body(id)
                .execute();
        return httpResponse.body();
    }

    public String getNameByPost(User user){
        String json = JSONUtil.toJsonStr(user);
        //链式构建请求
        HttpResponse httpResponse = HttpRequest.post(GATEWAY_HOST + "/api/name/post")
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
