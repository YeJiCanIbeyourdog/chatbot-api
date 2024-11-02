package chatbot.api.domain.zsxq.model.res;
/*对于回答问题的响应的请求接口：
回答请求结果是否成功数据
 */
public class AnswerRes {
    private boolean succeeded;

    public boolean isSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }
}
