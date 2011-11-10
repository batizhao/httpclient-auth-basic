说明
============================================================

httpclient-auth-basic 是一个使用 [Apache HttpComponents](http://hc.apache.org/httpcomponents-client-ga/) ，
访问 Basic Authentication 的示例。

在代码中主要演示了 HTTP 和 HTTPS 两种方式。涉及到的技术有 Apache HTTP Server 配置、SSL/TLS/OpenSSL、keytool、证书、
加密解密方面的一些基础知识。

场景
-------------------------------------------------------

在某些企业应用环境中，需要把前端的 Web Server 放到公网上，用来代理内部的 App Server。但是又不希望完全暴露在外网。
所以，需要进行认证和加密。先通过 Apache 的 basic auth 的认证，才能进入到内部系统的登录界面。
这里使用了 Apache HTTP Server 的 
[mod_auth_basic](http://httpd.apache.org/docs/2.2/mod/mod_auth_basic.html) 和 
[mod_ssl](http://httpd.apache.org/docs/2.2/mod/mod_ssl.html) 两个模块。

生成认证密码文件
-------------------------------------------------------

使用 Apache 自带工具，按提示输入密码：

    # htpasswd -c /usr/local/apache/passwd/passwords admin
    New password: admin
    Re-type new password: admin
    Adding password for user admin
    
如果需要增加其它用户：

    # htpasswd /usr/local/apache/passwd/passwords newuser
    
生成 self-signed SSL Certificate
-------------------------------------------------------

    # openssl req -new -x509 -days 3650 -nodes -out server.crt -keyout server.key
    Generating a 1024 bit RSA private key
    .............................++++++
    ..++++++
    writing new private key to 'server.key'
    -----
    You are about to be asked to enter information that will be incorporated
    into your certificate request.
    What you are about to enter is what is called a Distinguished Name or a DN.
    There are quite a few fields but you can leave some blank
    For some fields there will be a default value,
    If you enter '.', the field will be left blank.
    -----
    Country Name (2 letter code) [AU]:CN
    State or Province Name (full name) [Some-State]:
    Locality Name (eg, city) []:
    Organization Name (eg, company) [Internet Widgits Pty Ltd]:
    Organizational Unit Name (eg, section) []:
    Common Name (eg, YOUR name) []:localhost
    Email Address []:

把生成的两个文件放到 `/private/etc/apache2/`，后边会用到。这个目录可自定义，能匹配就好。

配置虚拟主机，代理 App Server
-------------------------------------------------------

先打开 Apache 主配置文件：

    $ vim /etc/apache2/httpd.conf
    
打开配置文件后，确保以下模块打开：
    
    LoadModule proxy_module libexec/apache2/mod_proxy.so
    LoadModule proxy_connect_module libexec/apache2/mod_proxy_connect.so
    LoadModule proxy_ftp_module libexec/apache2/mod_proxy_ftp.so
    LoadModule proxy_http_module libexec/apache2/mod_proxy_http.so
    LoadModule proxy_ajp_module libexec/apache2/mod_proxy_ajp.so
    LoadModule proxy_balancer_module libexec/apache2/mod_proxy_balancer.so
    LoadModule ssl_module libexec/apache2/mod_ssl.so
    LoadModule auth_basic_module libexec/apache2/mod_auth_basic.so

打开虚拟主机配置文件：

    Include /private/etc/apache2/extra/httpd-vhosts.conf
    
在 `httpd-vhosts.conf` 中增加以下配置：

    listen 443
    NameVirtualHost *:443
    
    <VirtualHost *:443>
        ServerName localhost
        ErrorLog "/private/var/log/apache2/sites-error_log"
        CustomLog "/private/var/log/apache2/sites-access_log" common
    	
        # App Server
    	ProxyPassMatch ^/(.*)$ http://localhost:8080/$1
    	ProxyPassReverse / http://localhost:8080/
        
    	SSLEngine On 
    	SSLCertificateFile /private/etc/apache2/server.crt
    	SSLCertificateKeyFile /private/etc/apache2/server.key
    	
    	<Location />
    	    AuthType Basic
    		AuthName "Apache Basic Authentication"
    		AuthUserFile /usr/local/apache2/passwd/passwords
    		Require valid-user
    	</Location>
            	
    </VirtualHost>

保证 App Server 开启，并重启 Apache

    # apachectl restart
    
访问 `https://localhost` ，先输入密码文件中的帐号、密码，就可以看到内部系统的登录界面。

如果项目中使用了 `Spring Security`，并且配置了 `auto-config = "true"` ，会和 Apache 的 basic auth 冲突，
需要改成 `auto-config = "false"`。

使用 `Java` 客户端访问 `https://localhost`
-------------------------------------------------------

通过 IE 浏览器导出客户端证书文件 my.cer

    ＊＊＊＊
生成 keystore

    sudo keytool -genkey -v -validity 3650 -keystore my.keystore
    
导入 keystore

    # keytool -import -noprompt -keystore my.keystore -storepass changeit -alias apache -file my.cer
    
待续...
