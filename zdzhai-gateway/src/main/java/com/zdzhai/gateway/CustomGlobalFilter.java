package com.zdzhai.gateway;

import com.zdzhai.apicommon.model.entity.InterfaceInfo;
import com.zdzhai.apicommon.model.entity.User;
import com.zdzhai.apicommon.service.InnerInterfaceInfoService;
import com.zdzhai.apicommon.service.InnerUserInterfaceInfoService;
import com.zdzhai.apicommon.service.InnerUserService;
import com.zdzhai.zdzhaiclientsdk.utils.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 全局网关过滤器
 *
 * @author dongdong
 */
@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    @DubboReference
    private InnerUserService innerUserService;

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;


    private static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1","192.168.1.1");

    private static final String INVOKE_HOST = "http://localhost:8123";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        //2. 请求日志
        log.info("请求唯一标识" + request.getId());
        String path = INVOKE_HOST + request.getPath().value();
        log.info("请求路径" + path);
        String method = request.getMethod().toString();
        log.info("请求方法" + method);
        log.info("请求参数" + request.getQueryParams());
        String sourceAddress = request.getLocalAddress().getHostString();
        log.info("请求来源地址" + sourceAddress);
        log.info("请求目的地址" + request.getRemoteAddress());
        ServerHttpResponse response = exchange.getResponse();
        //3. 黑白名单
        if (!IP_WHITE_LIST.contains(sourceAddress)) {
            handleNoAuth(response);
        }
        //4，获取请求参数并校验
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");
        String body = headers.getFirst("body");
        String sign = headers.getFirst("sign");
        //要去数据库查用户的信息 然后拿到accessKey和secretKey
        User invokeUser = null;
        try {
            invokeUser = innerUserService.getInvokeUser(accessKey);
        } catch (Exception exception) {
            log.error("getInvokeUser error",exception);
        }
        if (invokeUser == null){
            return handleNoAuth(response);
        }
        String invokeUserAccessKey = invokeUser.getAccessKey();
        if (invokeUserAccessKey != null && !invokeUserAccessKey.equals(accessKey)) {
            handleNoAuth(response);
        }
        if (Long.parseLong(nonce) > 10000) {
            handleNoAuth(response);
        }
        long currentTime = System.currentTimeMillis();
        final long FIVE_MINUTES = 1000 * 60 * 5L;
        if (currentTime - Long.parseLong(timestamp) >= FIVE_MINUTES){
            handleNoAuth(response);
        }
        //后端根据信息得到加密值
        String secretKey = invokeUser.getSecretKey();
        String serverSign = SignUtil.getSign(body, secretKey);
        if (sign == null || !sign.equals(serverSign)) {
            handleNoAuth(response);
        }
        //5.请求的模拟接口是否存在
        // 从数据库中查询模拟接口是否存在，以及请求方法时候匹配
        InterfaceInfo interfaceInfo = null;
        try {
             interfaceInfo = innerInterfaceInfoService.getInterfaceInfo(path, method);
        } catch (Exception exception) {
            log.error("getInterfaceInfo error",exception);
        }

        if (interfaceInfo == null){
            return handleNoAuth(response);
        }
        //6.请求转发，调用模拟接口
        //这里的chain.filter是处理实际的接口调用，但是他是异步调用的,所以我们手动处理响应为同步的
        Long userId = invokeUser.getId();
        Long interfaceInfoId = interfaceInfo.getId();
        return handleResponse(exchange, chain,interfaceInfoId,userId);
/*        Mono<Void> filter = chain.filter(exchange);
        //7.响应日志
        log.info("响应" + response.getStatusCode());
        if (response.getStatusCode() == HttpStatus.OK) {
            //8.调用成功 接口调用次数加1

        } else {
            //9.调用失败，返回错误码
            return handleNoAuth(response);
        }
        return filter;*/
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain,
                                     long interfaceInfoId, long userId) {
        try {
            //从交换机中取出响应对象
            ServerHttpResponse originalResponse = exchange.getResponse();
            //缓冲区工厂，拿到缓存数据
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            //拿到响应状态码
            HttpStatus statusCode = originalResponse.getStatusCode();

            if (statusCode == HttpStatus.OK) {
                //装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        //对象是响应式的
                        if (body instanceof Flux) {
                            //拿到真正的body
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            //往返回值里写数据
                            return super.writeWith(fluxBody.map(dataBuffer -> {
                                //8.调用成功 接口调用次数加1 invokeCount
                                try {
                                    innerUserInterfaceInfoService.invokeCount(interfaceInfoId, userId);
                                } catch (Exception exception) {
                                    log.error("getUserInterfaceInfo error",exception);
                                }
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);//释放掉内存
                                // 构建日志
                                StringBuilder sb2 = new StringBuilder(200);
                                List<Object> rspArgs = new ArrayList<>();
                                rspArgs.add(originalResponse.getStatusCode());
                                //rspArgs.add(requestUrl);
                                //data是最终结果
                                String data = new String(content, StandardCharsets.UTF_8);
                                sb2.append(data);
                                //打印日志
                                log.info("响应结果" + data);
                                return bufferFactory.wrap(content);
                            }));
                        } else {
                            //9.调用失败
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                //设置response对象为装饰过的
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            //降级处理返回数据
            return chain.filter(exchange);
        } catch (Exception e) {
            log.error("gateway log exception.\n" + e);
            return chain.filter(exchange);
        }
    }
}