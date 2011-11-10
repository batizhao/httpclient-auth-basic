package me.batizhao;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * @author: batizhao
 * @since: 11-11-7 下午4:39
 */
public class HttpBasicAuth {

    public void getHttpStatus() throws Exception {

        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            httpclient.getCredentialsProvider().setCredentials(
                    new AuthScope("localhost", 80),
                    new UsernamePasswordCredentials("admin", "admin"));

            HttpGet httpget = new HttpGet("http://localhost/");

            System.out.println("executing request" + httpget.getRequestLine());
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            if (entity != null) {
                System.out.println("Response content length: " + entity.getContentLength());
            }
            EntityUtils.consume(entity);

        } finally {
            httpclient.getConnectionManager().shutdown();
        }

    }

    public static void main(String[] args) throws Exception {

        HttpBasicAuth basicAuth = new HttpBasicAuth();
        basicAuth.getHttpStatus();
    }
}

