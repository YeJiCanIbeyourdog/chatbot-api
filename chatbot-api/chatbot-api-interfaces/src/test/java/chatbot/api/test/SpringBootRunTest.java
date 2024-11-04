package chatbot.api.test;

import chatbot.api.Application;
import chatbot.api.domain.chatglm.config.ChatGLMSDKConfig;
import chatbot.api.domain.zsxq.IZsxqApi;
import chatbot.api.domain.zsxq.model.aggregates.UnAnswerQuestionAggregates;
import chatbot.api.domain.zsxq.model.vo.Topics;
import cn.bugstack.chatglm.model.*;
import cn.bugstack.chatglm.session.OpenAiSession;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

//@SpringBootTest(classes = {ChatGLMSDKConfig.class})
@SpringBootTest
@RunWith(SpringRunner.class)
public class SpringBootRunTest {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(SpringBootRunTest.class);

    @Value("${chatbot-api.groupId}")
    private  String groupId;
    @Value("${chatbot-api.cookie}")
    private  String cookie;

    @Resource
    private IZsxqApi zsxqApi;
    @Test
    public void test_zsxqApi() throws IOException {
        UnAnswerQuestionAggregates unAnswerQuestionAggregates = zsxqApi.queryUnAnswerQuestionTopicId(groupId, cookie);
        logger.info("测试结果：{}", JSON.toJSONString(unAnswerQuestionAggregates));
        List<Topics> topicsList = unAnswerQuestionAggregates.getResp_data().getTopics();
        for (Topics topics : topicsList) {
            String topicId = topics.getTopic_id();
            String text = topics.getQuestion().getText();
//            boolean answer = zsxqApi.answer(groupId, cookie, topicId, "这是由idea回答", true);
            logger.info("topicId:{},问题：{}", topicId,text);
//            zsxqApi.answer(groupId, cookie, topicId, "这是由idea回答", true);
        }
    }

    @Autowired(required = false)
    private OpenAiSession openAiSession;//配置文件里的enable:需要为ture
    @Test
    public void test_completions() throws Exception {
        // 入参；模型、请求信息
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(Model.GLM_4_Air);
        request.setPrompt(new ArrayList<ChatCompletionRequest.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;

            {
                add(ChatCompletionRequest.Prompt.builder()
                        .role(Role.user.getCode())
                        .content("给我三个随机字母，只回答我这三个字母")
                        .build());
            }
        });

        StringBuilder content = new StringBuilder();
        // 请求
        openAiSession.completions(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, @Nullable String id, @Nullable String type, String data) {
                ChatCompletionResponse response = JSON.parseObject(data, ChatCompletionResponse.class);
                logger.info("测试结果 onEvent：{}", response.getData());
                // type 消息类型，add 增量，finish 结束，error 错误，interrupted 中断
                logger.info("type:{}", type);
                System.out.println(type);
                if (EventType.finish.getCode().equals(type)) {
                    ChatCompletionResponse.Meta meta = JSON.parseObject(response.getMeta(), ChatCompletionResponse.Meta.class);
                    logger.info("[输出结束] Tokens {}", JSON.toJSONString(meta));
                }
                content.append(response.getData());
            }

            @Override
            public void onClosed(EventSource eventSource) {
                logger.info("对话完成");
            }
        });
        System.out.println(content);

        // 等待
        new CountDownLatch(1).await();
    }
}
