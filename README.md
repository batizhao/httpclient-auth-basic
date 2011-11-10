说明
============================================================

httpclient-auth-basic 是一个使用 [Apache HttpComponents](http://hc.apache.org/httpcomponents-client-ga/) ，
访问 Basic Authentication 的示例。

在代码中主要演示了 HTTP 和 HTTPS 两种方式。涉及到的技术有 Apache HTTP Server 配置、SSL/TLS/OpenSSL、keytool、证书、
加密解密方面的一些基础知识。

场景
-------------------------------------------------------

在某些企业应用环境中，需要把前端的 Web Server 放到公网上，用来代理内部的 App Server。但是又不希望完全暴露在外网。
所以，需要进行认证和加密。这里使用了 Apache HTTP Server 的 
[mod_auth_basic](http://httpd.apache.org/docs/2.2/mod/mod_auth_basic.html) 和 
[mod_ssl](http://httpd.apache.org/docs/2.2/mod/mod_ssl.html) 两个模块。

安装、配置 Apache HTTP Server
-------------------------------------------------------

在 MacOS X 上，Apache 已经默认安装。

    $ vim /etc/apache2/httpd.conf
    
打开配置文件后，



