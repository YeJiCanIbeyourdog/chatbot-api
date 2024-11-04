package chatbot.api.test;

import cn.bugstack.chatglm.model.*;
import cn.bugstack.chatglm.session.Configuration;
import cn.bugstack.chatglm.session.OpenAiSession;
import cn.bugstack.chatglm.session.OpenAiSessionFactory;
import cn.bugstack.chatglm.session.defaults.DefaultOpenAiSessionFactory;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
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
import org.junit.jupiter.api.BeforeEach;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Component()
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
//        get.addHeader("cookie","        get.addHeader(\"cookie\",\"zsxqsessionid=6bc57522e8021225dbe393a0fb952752; abtest_env=product; zsxq_access_token=5895F417-4688-C024-A41A-836BBB2238C3_E4719BF902A2D77B\");\n");

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


    @Autowired(required = false)
    private OpenAiSession openAiSession;
//    @BeforeEach
//    public void test_OpenAiSessionFactory(String methodName) {
//        if ("test_chatGLM".equals(methodName)) {
//            // 只在testMethod1之前执行的初始化代码
//            // 1. 配置文件
//            Configuration configuration = new Configuration();
//            configuration.setApiHost("https://open.bigmodel.cn/");
//            configuration.setApiSecretKey("b0c604a00958ab72d306b92c8ce6a1a5.Ufmm5qGXlQom2iXY");
//            // 2. 会话工厂
//            OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);
//            // 3. 开启会话
//            this.openAiSession = factory.openSession();
//        }
//    }

    @Test
    public void test_chatGLM() throws Exception {
        // 入参；模型、请求信息
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(Model.GLM_4_Air); // 得看自己账户有哪写模型token
        request.setPrompt(new ArrayList<ChatCompletionRequest.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;

            {
                add(ChatCompletionRequest.Prompt.builder()
                        .role(Role.user.getCode())
                        .content("写个go语言的HelloWorld")
                        .build());
            }
        });

        // 请求
        openAiSession.completions(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, @Nullable String id, @Nullable String type, String data) {
                ChatCompletionResponse response = JSON.parseObject(data, ChatCompletionResponse.class);
                log.info("测试结果 onEvent：{}", response.getData());
                // type 消息类型，add 增量，finish 结束，error 错误，interrupted 中断
                if (EventType.finish.getCode().equals(type)) {
                    ChatCompletionResponse.Meta meta = JSON.parseObject(response.getMeta(), ChatCompletionResponse.Meta.class);
                    log.info("[输出结束] Tokens {}", JSON.toJSONString(meta));
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                log.info("对话完成");
            }
        });

        // 等待
        new CountDownLatch(1).await();

    }
}
