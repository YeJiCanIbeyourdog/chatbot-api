package chatbot.api.domain.zsxq.service;

import chatbot.api.domain.zsxq.IZsxqApi;
import chatbot.api.domain.zsxq.model.aggregates.UnAnswerQuestionAggregates;
import chatbot.api.domain.zsxq.model.req.AnswerReq;
import chatbot.api.domain.zsxq.model.req.ReqData;
import chatbot.api.domain.zsxq.model.res.AnswerRes;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ZsxqApi implements IZsxqApi {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(ZsxqApi.class);
    /*
    获取拉取提问请求的结果以及提问的问题topicId
     */
    @Override
    public UnAnswerQuestionAggregates queryUnAnswerQuestionTopicId(String groupId, String cookie) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpGet get=new HttpGet("https://api.zsxq.com/v2/groups/"+groupId+"/topics?scope=unanswered_questions&count=20");

        // 添加请求头
        get.addHeader("cookie",cookie);
        get.addHeader("Content-type","application/json;charset=utf8");

        CloseableHttpResponse response = httpClient.execute(get);
        if (response.getStatusLine().getStatusCode()== HttpStatus.SC_OK){
            String jsonStr = EntityUtils.toString(response.getEntity());
            logger.info("拉取星球提问结果，groupId:{},answer resJsonStr is {}",groupId,jsonStr);
            return JSON.parseObject(jsonStr,UnAnswerQuestionAggregates.class);//把获取提问请求结果json字符串转成对象
        }else{
            throw new RuntimeException("queryUnAnswerQuestionTopicId Err Code is"+response.getStatusLine().getStatusCode());
        }
    }

    @Override
    public boolean answer(String groupId, String cookie, String topicId,String text, boolean silenced) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post=new HttpPost("https://api.zsxq.com/v2/topics/"+topicId+"/answer");
        post.addHeader("cookie",cookie);
        post.addHeader("Content-type","application/json;charset=utf8");
        post.addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");// 模拟浏览器,防止触发风控

        //silenced是否回答让他人不能看见
//        String paramJson = "{\n" +
//                "  \"req_data\": {\n" +
//                "    \"text\": \"我啥也不会！\\n\",\n" +
//                "    \"image_ids\": [],\n" +
//                "    \"silenced\": true\n" +
//                "  }\n" +
//                "}";
        AnswerReq answerReq = new AnswerReq(new ReqData(text, silenced));
        String paramJson = JSON.toJSONString(answerReq);

        StringEntity stringEntity = new StringEntity(paramJson, ContentType.create("text/json", "UTF-8"));
        post.setEntity(stringEntity);

        CloseableHttpResponse response = httpClient.execute(post);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String jsonStr = EntityUtils.toString(response.getEntity());
            logger.info("回答星球问题结果，groupId:{},topicId:{},answer resJsonStr is {}",groupId,topicId,jsonStr);
            AnswerRes answerRes = JSON.parseObject(jsonStr, AnswerRes.class);// 把回答请求结果json字符串转成对象
            return answerRes.isSucceeded();
        } else {
            throw new RuntimeException("answer Err Code is" + response.getStatusLine().getStatusCode());
        }
    }
}
