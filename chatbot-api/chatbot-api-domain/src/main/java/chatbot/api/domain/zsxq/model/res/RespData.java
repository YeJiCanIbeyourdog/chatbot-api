package chatbot.api.domain.zsxq.model.res;

import chatbot.api.domain.zsxq.model.vo.Topics;

import java.util.List;

/*对于回答问题的响应的请求接口：
接口所需传参
 */
public class RespData {
    private List<Topics> topics;

    public List<Topics> getTopics() {
        return topics;
    }

    public void setTopics(List<Topics> topics) {
        this.topics = topics;
    }
}
