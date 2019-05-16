package cn.tju.modelsearch.utils;

import cn.tju.modelsearch.dao.sumMapper;
import cn.tju.modelsearch.domain.ModelEs;
import cn.tju.modelsearch.domain.sum;
import cn.tju.modelsearch.service.ESClient;
import cn.tju.modelsearch.utils.yandtran.language.Language;
import cn.tju.modelsearch.utils.yandtran.translate.TranslateYT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.mapper.ObjectMapper;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.tju.modelsearch.utils.dateUtils.gainDate;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class getEsModel {
    private static final String PUT = "PUT";
    private static final String POST = "POST";
    private static final String GET = "GET";
    private static final String HEAD = "HEAD";
    private static final String DELETE = "DELETE";

    public static List<ModelEs> getModel(ESClient esClient, String type, String value, int offset, int size,sumMapper sumMapper) throws Exception {
        QueryBuilder match ;

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (type.equals("text")){
            //Translate translate = TranslateOptions.newBuilder().setApiKey(ProjectConstant.GOOGLETRANS_KEY).build().getService();
            TranslateYT translateYT = new TranslateYT();
            translateYT.setKey();
            //String trans = translate.translate(value, Translate.TranslateOption.sourceLanguage("zh-CN"), Translate.TranslateOption.targetLanguage("en")).getTranslatedText();
            String trans = translateYT.execute(value, Language.CHINESE, Language.ENGLISH);
            match = QueryBuilders.multiMatchQuery(trans,"name","description");
            searchSourceBuilder.size(ProjectConstant.PAGESIZE);
        }else if (type.equals("category")){
            String trans = initCategory().get(value);
            match = QueryBuilders.multiMatchQuery(trans,"name","description");
            searchSourceBuilder.size(ProjectConstant.PAGESIZE);
        }else if (type.equals("itemId")){
            match = QueryBuilders.matchQuery("ID",value);
            searchSourceBuilder.size(1);
            dateUtils.update(sumMapper,2,dateUtils.gainDate());
            dateUtils.update(sumMapper,2,"2000-00-00");
        }else if (type.equals("recom")){
            match = QueryBuilders.multiMatchQuery(value,"name","description","className","sublassName");
            searchSourceBuilder.size(size);
        }else if (type.equals("index")){
            match = QueryBuilders.matchAllQuery();
            Script script = new Script("Math.random()"); //自定义脚本排序
            ScriptSortBuilder sortBuilder = SortBuilders.scriptSort(script, ScriptSortBuilder.ScriptSortType.NUMBER).order(SortOrder.ASC);
            searchSourceBuilder.sort(sortBuilder);
            searchSourceBuilder.size(size);
        }else {
            String[] tmp = value.split(" ");
            String query = "";
            for (int i = 0;i < 199;i++){
                try {
                    query = query + tmp[i]+" ";
                }catch (Exception e){
                    System.out.println("TYPE:"+type);
                    System.out.println("value:"+value);
                }

            }
            match = QueryBuilders.matchQuery(type,query);
            searchSourceBuilder.size(ProjectConstant.PAGESIZE);
        }
        searchSourceBuilder.from(offset);
        searchSourceBuilder.query(match);
        SearchRequest searchRequest = new SearchRequest(ProjectConstant.ESINDEX);
        searchRequest.source(searchSourceBuilder);
        List<ModelEs> list = new ArrayList<>();
        try {
            SearchResponse searchResponse = esClient.getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            for (SearchHit searchHit : searchHits) {
                ModelEs modelEs = new ModelEs();
                String ID = (String) searchHit.getSourceAsMap().get("ID");
                String hashCode = (String) searchHit.getSourceAsMap().get("hashCode");
                String name = (String) searchHit.getSourceAsMap().get("name");
                String className = (String) searchHit.getSourceAsMap().get("className");
                String subClassName = (String)  searchHit.getSourceAsMap().get("subClassName");
                String description = (String) searchHit.getSourceAsMap().get("description");
                modelEs.setID(ID);
                modelEs.setHashCode(hashCode);
                modelEs.setName(name);
                modelEs.setClassName(className);
                modelEs.setSubClassName(subClassName);
                modelEs.setDescription(description);
                list.add(modelEs);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void updateES(ESClient esClient, ModelEs modelEs) throws IOException {
        UpdateRequest updateRequest = new UpdateRequest(ProjectConstant.ESINDEX,ProjectConstant.TYPE,getId(esClient,modelEs.getID()));
        updateRequest.doc(beanToMap(modelEs));
        System.out.println(getId(esClient,modelEs.getID())+esClient.getRestHighLevelClient().update(updateRequest).getGetResult());
    }

    public static void deleteES(ESClient esClient,String ID) throws IOException {
        String docID = getId(esClient,ID);
//        System.out.println(docID);
        DeleteRequest request = new DeleteRequest(ProjectConstant.ESINDEX, ProjectConstant.TYPE,docID);
        DeleteResponse response = esClient.getRestHighLevelClient().delete(request);
        System.out.println(response.getResult());

    }

    public static void indexES(ESClient esClient,ModelEs modelEs) throws IOException {
        IndexRequest request = new IndexRequest(ProjectConstant.ESINDEX, ProjectConstant.TYPE)
                .source(beanToMap(modelEs));
        IndexResponse response = esClient.getRestHighLevelClient().index(request);
        System.out.println(response.getResult());
    }

    public static String getId(ESClient esClient,String ID){
        QueryBuilder match ;
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        match = QueryBuilders.matchQuery("ID",ID);
        searchSourceBuilder.size(1);
        searchSourceBuilder.query(match);
        SearchRequest searchRequest = new SearchRequest(ProjectConstant.ESINDEX);
        searchRequest.source(searchSourceBuilder);
        List<ModelEs> list = new ArrayList<>();
        String res = "";
        try {
            SearchResponse searchResponse = esClient.getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            for (SearchHit searchHit : searchHits) {
//                System.out.println(searchHit.getId());
                res = String.valueOf(searchHit.getId());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static Integer getCount(ESClient esClient, String type, String value){
        QueryBuilder match ;
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (type.equals("text")){
            Translate translate = TranslateOptions.newBuilder().setApiKey(ProjectConstant.GOOGLETRANS_KEY).build().getService();
            String trans = translate.translate(value, Translate.TranslateOption.sourceLanguage("zh-CN"), Translate.TranslateOption.targetLanguage("en")).getTranslatedText();
            match = QueryBuilders.multiMatchQuery(trans,"name","description");
//            searchSourceBuilder.size(ProjectConstant.PAGESIZE);
        }else if (type.equals("category")){
            String trans = initCategory().get(value);
            match = QueryBuilders.multiMatchQuery(trans,"name","description");
//            searchSourceBuilder.size(ProjectConstant.PAGESIZE);
        }else {
            String[] tmp = value.split(" ");
            String query = "";
            for (int i = 0;i < 199;i++){
                try {
                    query = query + tmp[i]+" ";
                }catch (Exception e){
                    System.out.println("TYPE:"+type);
                    System.out.println("value:"+value);
                }

            }
            match = QueryBuilders.matchQuery(type,query);
//            searchSourceBuilder.size(ProjectConstant.PAGESIZE);
        }
        searchSourceBuilder.query(match);
        SearchRequest searchRequest = new SearchRequest(ProjectConstant.ESINDEX);
        searchRequest.source(searchSourceBuilder);
        List<ModelEs> list = new ArrayList<>();
        int res = 0;
        try {
            SearchResponse searchResponse = esClient.getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT);
            res = (int)searchResponse.getHits().getTotalHits();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static Map<String, Object> beanToMap(Object obj) {
        Map<String, Object> params = new HashMap<String, Object>(0);
        try {
            PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
            PropertyDescriptor[] descriptors = propertyUtilsBean.getPropertyDescriptors(obj);
            for (int i = 0; i < descriptors.length; i++) {
                String name = descriptors[i].getName();
                if (!"class".equals(name)) {
                    params.put(name, propertyUtilsBean.getNestedProperty(obj, name));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return params;
    }

    public static Map<String,String> initCategory(){
        Map<String,String> map = new HashMap<>();
        map.put("工艺品","art");
        map.put("艺术字","font");
        map.put("工艺模型","model");
        map.put("工艺饰品","Ornament");
        map.put("家具","HouseHold");
        map.put("灯","Lamp");
        map.put("床","Bed");
        map.put("沙发","Sofa");
        map.put("桌子","Table");
        map.put("橱柜","Cabinet");
        map.put("椅子","Chair");
        map.put("工具","Tools");
        map.put("盒子","Box");
        map.put("建筑","Buildings");
        map.put("工作场所","WorkPlace");
        map.put("家庭住所","HomePlace");
        map.put("塔","Tower");
        map.put("交通","Transportation");
        map.put("摩托车","Motor");
        map.put("汽车","Car");
        map.put("船","Ship");
        map.put("飞机","Aviation");
        map.put("火车","Train");
        map.put("机械","Machinery");
        map.put("设备","Equipment");
        map.put("部件","Component");
        map.put("武器","Arms");
        map.put("玩具","Toy");
        map.put("滑板","Skateboard");
        map.put("游戏","Game");
        map.put("益智玩具","Kid");
        map.put("模型玩具","Model toy");
        map.put("其他","Others");
        map.put("显示","Display");
        map.put("水龙头","Faucet");
        map.put("头盔","Helmet");
        map.put("邮箱","Mailbox");
        map.put("相机","Camera");
        map.put("台灯","Laptop");
        map.put("电话","Telephone");
        map.put("风景园林","Landscape");
        return map;
    }
}
