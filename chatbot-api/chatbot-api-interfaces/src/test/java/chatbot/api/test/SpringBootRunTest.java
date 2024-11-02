package chatbot.api.test;

import chatbot.api.domain.zsxq.IZsxqApi;
import chatbot.api.domain.zsxq.model.aggregates.UnAnswerQuestionAggregates;
import chatbot.api.domain.zsxq.model.vo.Topics;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
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
            zsxqApi.answer(groupId, cookie, topicId, "这是由idea回答", true);
        }
    }
}
