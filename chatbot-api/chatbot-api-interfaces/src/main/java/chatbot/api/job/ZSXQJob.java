package chatbot.api.job;

import chatbot.api.domain.zsxq.IZsxqApi;
import chatbot.api.domain.zsxq.model.aggregates.UnAnswerQuestionAggregates;
import chatbot.api.domain.zsxq.model.vo.Topics;
import cn.bugstack.chatglm.model.*;
import cn.bugstack.chatglm.session.OpenAiSession;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component()
public class ZSXQJob {
    @Autowired(required = false)
    private OpenAiSession openAiSession;

    @Resource
    private IZsxqApi zsxqApi;

    private volatile Set<String> topicIds = new HashSet<>();

    @Value("${chatbot-api.groupId}")
    private String groupId;
    @Value("${chatbot-api.cookie}")
    private String cookie;


    @Scheduled(cron = "0 * * * * ?")
    public void exec() throws Exception {
        UnAnswerQuestionAggregates unAnswerQuestionAggregates = zsxqApi.queryUnAnswerQuestionTopicId(groupId, cookie);
        log.info("获取问题结果：{}", JSON.toJSONString(unAnswerQuestionAggregates));
        List<Topics> topicsList = unAnswerQuestionAggregates.getResp_data().getTopics();
        for (Topics topics : topicsList) {
            if (topics.getAnswered()) {
                continue;
            }
            String topicId = topics.getTopic_id();
            if (null == openAiSession) {
                log.info("你没有开启 ChatGLM 参考yml配置文件来开启");
            } else {
                if (topicIds.contains(topicId)){
                    continue;
                } else {
                    topicIds.add(topicId);
                }
                new Thread(() -> {
                    log.info("开起一个新线程发送给chatGML问题");
                    // 入参；模型、请求信息
                    ChatCompletionRequest request = new ChatCompletionRequest();
                    request.setModel(Model.GLM_4_Air);
                    request.setPrompt(new ArrayList<ChatCompletionRequest.Prompt>() {
                        private static final long serialVersionUID = -7988151926241837899L;
                        {
                            add(ChatCompletionRequest.Prompt.builder()
                                    .role(Role.user.getCode())
                                    .content(topics.getQuestion().getText())
                                    .build());
                        }
                    });
                    // 请求
                    try {
                        StringBuilder content = new StringBuilder();
                        openAiSession.completions(request, new EventSourceListener() {
                            @Override
                            public void onEvent(EventSource eventSource, @Nullable String id, @Nullable String type, String data) {
                                ChatCompletionResponse chatCompletionResponse = com.alibaba.fastjson.JSON.parseObject(data, ChatCompletionResponse.class);
                                log.info("测试结果 onEvent：{}", chatCompletionResponse.getData());
                                // type 消息类型，add 增量，finish 结束，error 错误，interrupted 中断
                                if (EventType.finish.getCode().equals(type)) {
                                    ChatCompletionResponse.Meta meta = com.alibaba.fastjson.JSON.parseObject(chatCompletionResponse.getMeta(), ChatCompletionResponse.Meta.class);
                                    log.info("[输出结束] Tokens {}", com.alibaba.fastjson.JSON.toJSONString(meta));
                                }
                                content.append(chatCompletionResponse.getData());
                            }
                            @Override
                            public void onClosed(EventSource eventSource) {
                                log.info("对话完成");
                                try {
                                    log.info("发送请求回答星球问题中");
                                    zsxqApi.answer(groupId, cookie, topicId, "ChatGLM 回答：" + content, true);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    log.error("回答发送失败：{}", e.getMessage());
                                    throw new RuntimeException(e);
                                }
                                topicIds.remove(topicId);
                            }
                        });
                    } catch (Exception e) {
                        log.error("chatGML回答失败：{}", e.getMessage());
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        }


    }


}
