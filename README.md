说明
============================================================

httpclient-auth-basic 是一个使用 [Apache HttpComponents](http://hc.apache.org/httpcomponents-client-ga/) ，
访问 HTTP Basic 认证的示例。

HTTP Basic 认证方式使用 base64 编码方式传送用户名和密码，而 base64 仅仅是一种公开的编码格式而非加密措施，
因而如果信道本身不使用 SSL 等安全协议，用户密码较容易被截获。

在代码中主要演示了 HTTP 和 HTTPS 两种方式。涉及到的技术有 Apache HTTP Server 配置、SSL/TLS/OpenSSL、keytool、证书、
加密解密方面的一些基础知识。

场景
-------------------------------------------------------

在某些企业应用环境中，需要把前端的 Web Server 放到公网上，用来代理内部的 App Server。但是又不希望完全暴露在外网。
所以，需要进行认证和加密。先通过 Apache 的 basic auth 认证，才能进入到内部系统的登录界面。
这里使用了 Apache HTTP Server 的 
[mod_auth_basic](http://httpd.apache.org/docs/2.2/mod/mod_auth_basic.html) 和 
[mod_ssl](http://httpd.apache.org/docs/2.2/mod/mod_ssl.html) 两个模块。

生成认证密码文件
-------------------------------------------------------

使用 Apache 自带工具，按提示输入密码：

    # htpasswd -c /usr/local/apache2/passwd/passwords admin
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

`Common Name` 要使用你的域名或 IP 。
把生成的两个文件放到 `/private/etc/apache2/`，后边会用到。这个目录可自定义，能匹配就好。重新生成证书要重启 Apache。

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
`以下步骤需要在客户端执行`。首先，
通过 IE 浏览器导出客户端证书文件 my.cer 。`下边这张图片需要可以访问 Dropbox 才可以看到`
![导出证书](http://dl.dropbox.com/u/1682099/images/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202011-11-14%20%E4%B8%8A%E5%8D%889.43.44.png)

生成 keystore，最重要的是 `您的名字与姓氏是什么` 和 服务端的 `Common Name` 要保持一致，并且都使用你要访问的域名或 IP ，
否则会抛出异常 `javax.net.ssl.SSLException: hostname in certificate didn't match: <localhost> != <***>`

    # keytool -genkey -v -validity 3650 -keystore my.keystore
    输入keystore密码：123456  
    再次输入新密码: 123456
    您的名字与姓氏是什么？
      [Unknown]：  localhost
    您的组织单位名称是什么？
      [Unknown]：  
    您的组织名称是什么？
      [Unknown]：  
    您所在的城市或区域名称是什么？
      [Unknown]：  
    您所在的州或省份名称是什么？
      [Unknown]：  
    该单位的两字母国家代码是什么
      [Unknown]：  CN
    CN=localhost, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=CN 正确吗？
      [否]：  Y
    
    正在为以下对象生成 1,024 位 DSA 密钥对和自签名证书 (SHA1withDSA)（有效期为 90 天）:
             CN=localhost, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=CN
    输入<mykey>的主密码
            （如果和 keystore 密码相同，按回车）：  
    [正在存储 my.keystore]
    
导入 keystore

    # keytool -import -noprompt -keystore my.keystore -storepass 123456 -alias apache -file my.cer
    
修改 CustomSSLAuth ，把路径指向你自己的 keystore 文件，把密码改为 `storepass` 参数指定的值，看到以下内容就说明成功了。

    HTTP/1.1 200 OK

补充说明
-------------------------------------------------------

在 JDK 中，已经有一个 keystore 文件 cacerts 。
在 Mac 中，这个文件大概的位置在 `/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/lib/security/cacerts` ，
默认密码为 `changeit`，这样可以直接使用 `keytool -import` 命令导入客户端证书，而不用生成自己的 `my.keystore` 。

删除证书

    # keytool -delete -alias apache -keystore my.keystore -storepass 123456
    
查看证书

    # keytool -list -v -alias apache -keystore my.keystore -storepass 123456

