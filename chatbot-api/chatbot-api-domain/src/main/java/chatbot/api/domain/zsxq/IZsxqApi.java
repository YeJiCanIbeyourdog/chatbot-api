package chatbot.api.domain.zsxq;

import chatbot.api.domain.zsxq.model.aggregates.UnAnswerQuestionAggregates;

import java.io.IOException;

/*
知识星球API接口
 */
public interface IZsxqApi {

    UnAnswerQuestionAggregates queryUnAnswerQuestionTopicId(String groupId, String cookie) throws IOException ;

    boolean answer(String groupId, String cookie, String topicId, String text,boolean silenced) throws IOException;
}
