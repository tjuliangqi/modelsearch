package cn.tju.modelsearch.domain;

/**
 * @Description: 响应码枚举，参考HTTP状态码的语义
 * @author
 * @date 2018/4/19 09:42
 */
public enum RetCode {

    // 成功
    SUCCESS(20000),

    // 失败
    FAIL(40000),

    // 未认证（签名错误）
    UNAUTHORIZED(40001),

    // 接口不存在
    NOT_FOUND(40004),

    // 服务器内部错误
    INTERNAL_SERVER_ERROR(50000);

    public int code;

    RetCode(int code) {
        this.code = code;
    }
}
