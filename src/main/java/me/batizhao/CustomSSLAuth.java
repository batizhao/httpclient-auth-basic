package me.batizhao;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * @author: batizhao
 * @since: 11-11-7 下午4:39
 * Source Code: http://hc.apache.org/httpcomponents-client-ga/examples.html [Custom SSL context]
 * 对官方文档做了生成 keystore 文件和帐号密码认证的补充。
 */
public class CustomSSLAuth {

    public void getHttpsStatus() throws KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, CertificateException {

        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream instream = new FileInputStream(new File("/Users/zyb/Downloads/my.keystore"));
            try {
                trustStore.load(instream, "123456".toCharArray());
            } finally {
                try {
                    instream.close();
                } catch (Exception ignore) {
                }
            }

            SSLSocketFactory socketFactory = new SSLSocketFactory(trustStore);
            Scheme sch = new Scheme("https", 443, socketFactory);
            httpclient.getConnectionManager().getSchemeRegistry().register(sch);

            httpclient.getCredentialsProvider().setCredentials(
                    new AuthScope("localhost", 443),
                    new UsernamePasswordCredentials("admin", "admin"));

            HttpGet httpget = new HttpGet("https://localhost/");

            System.out.println("executing request" + httpget.getRequestLine());

            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            if (entity != null) {
                System.out.println("Response content length: " + entity.getContentLength());
                System.out.println("----------------------------------------");
                long len = entity.getContentLength();
                if (len != -1 && len < 10000) {
                    System.out.println(EntityUtils.toString(entity));
                }
            }

            EntityUtils.consume(entity);

        } finally {
            httpclient.getConnectionManager().shutdown();
        }
    }

    public static void main(String[] args) throws Exception {
        CustomSSLAuth sslAuth = new CustomSSLAuth();
        sslAuth.getHttpsStatus();
    }
}
