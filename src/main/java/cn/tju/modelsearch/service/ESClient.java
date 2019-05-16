package cn.tju.modelsearch.service;

import cn.tju.modelsearch.utils.ProjectConstant;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component
public class ESClient {

    private String ip;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public RestHighLevelClient restHighLevelClient;

    public RestHighLevelClient getRestHighLevelClient() {
        return restHighLevelClient;
    }

    public void setRestHighLevelClient(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    public ESClient() {
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
                new HttpHost(ProjectConstant.ESIP, 9200, "http")).setMaxRetryTimeoutMillis(5*60*1000));
        this.restHighLevelClient = client;
    }


}
