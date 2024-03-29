# Orien 接口开放平台

> 项目上线地址：http://123.60.69.153 --已过期

#### 1、介绍

个人API开放平台是基于 [@鱼皮](https://space.bilibili.com/12890453)  的《API开放平台》的基础上**继续开发**的一个分布式项目，仅用于个人学习使用

#### 2、优化点
- 1、基于 [@鱼皮](https://space.bilibili.com/12890453)  的《API开放平台》项目
- 2、参考大佬的优化点 [@YukeSeko](https://github.com/YukeSeko/YukeSeko-Interface) ，新增或重构功能如下：
  - 1、新增用户登录后返回token、以及token拦截器
  - 2、重构用户表：用户表中新增手机号`mobile`字段
  - 3、新增用户通过手机号、发送手机验证码方式进行登录，使用令牌桶算法对发送短信接口进行限制 未完全实现
  - 4、新增用户注册，重构用户注册功能（需要使用手机号进行验证）。
  - 5、修改SDK请求基本路径，通过反射技术实现只通过一个基本调用的方法名动态调用API接口。
  - 6、新增第三方登录功能（微信公众号登录、GitHub、支付宝）暂未实现。

#### 项目模块介绍

1.  myapp ：为项目前端，前端项目启动具体看readme.md文档
2.  api-common ：为公共封装类（如公共返回对象、）
3.  api-backend ：为项目主体，主要包括用户相关、接口相关等功能
4.  zdzhai-gateway ：为网关服务，涉及到统一鉴权
7.  zdzhai-interface：为接口服务，主要用于实现接口调用的功能
8.  zdzhai-client-sdk：为生成的SDK源码

> 写在最后
>
> 花了很长的时间完成第一个初步完整的项目，前后端全干，刚开始跟着敲前端，后来熟悉了些后，能够跟着文档找组件实现自己想要的效果还是挺开心的。
>
> 有些地方可能还存在bug，如果有问题，欢迎大家在 Issues 中提出或者 评论区 留言
>
#### 持续更新中。。。
1. 使用策略模式构建，新增第三方登录服务（Github, Gitee），可方便扩展其他第三方。