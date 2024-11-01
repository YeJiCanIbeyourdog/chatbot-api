package chatbot.api.test;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;

public class ApiTest {
    /*
   那个topic_id实际上是变化的，我们这里只是测试下流程
     */
    //测试获取提问
    @Test
    public void query_unanswered_questions() throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpGet get=new HttpGet("https://api.zsxq.com/v2/groups/48888145814518/topics?scope=unanswered_questions&count=20");

        // 添加请求头
        get.addHeader("cookie","zsxqsessionid=6bc57522e8021225dbe393a0fb952752; abtest_env=product; zsxq_access_token=5895F417-4688-C024-A41A-836BBB2238C3_E4719BF902A2D77B");
        get.addHeader("Content-type","application/json;charset=utf8");

        CloseableHttpResponse response = httpClient.execute(get);
        if (response.getStatusLine().getStatusCode()== HttpStatus.SC_OK){
            String res = EntityUtils.toString(response.getEntity());
            System.out.println(res);
        }else{
            System.out.println(response.getStatusLine().getStatusCode());
        }
    }

    //测试回答实现
    @Test
    public void answer() throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post=new HttpPost("https://api.zsxq.com/v2/topics/8858242111152222/answer");
        post.addHeader("cookie","zsxqsessionid=6bc57522e8021225dbe393a0fb952752; abtest_env=product; zsxq_access_token=5895F417-4688-C024-A41A-836BBB2238C3_E4719BF902A2D77B");
        post.addHeader("Content-type","application/json;charset=utf8");

        //silenced是否回答让他人不能看见
        String paramJson = "{\n" +
                "  \"req_data\": {\n" +
                "    \"text\": \"我啥也不会！\\n\",\n" +
                "    \"image_ids\": [],\n" +
                "    \"silenced\": true\n" +
                "  }\n" +
                "}";

        StringEntity stringEntity = new StringEntity(paramJson, ContentType.create("text/json", "UTF-8"));
        post.setEntity(stringEntity);

        CloseableHttpResponse response = httpClient.execute(post);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String res = EntityUtils.toString(response.getEntity());
            System.out.println(res);
        } else {
            System.out.println(response.getStatusLine().getStatusCode());
        }
    }

}
