package cn.tju.modelsearch.controller;

import cn.tju.modelsearch.dao.sumMapper;
import cn.tju.modelsearch.dao.userMapper;
import cn.tju.modelsearch.domain.*;
import cn.tju.modelsearch.service.ESClient;

import cn.tju.modelsearch.service.MailService;
import cn.tju.modelsearch.utils.*;
import com.google.gson.*;
import org.apache.ibatis.annotations.Param;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@RestController
public class modelController {
    @Autowired
    cn.tju.modelsearch.dao.modelMapper modelMapper;
    @Autowired
    userMapper userMapper;
    @Autowired
    ESClient esClient;
    @Autowired
    MailService mailService;
    @Autowired
    sumMapper sumMapper;
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public RetResult<List<Model>> search(@RequestParam(name = "type") String type, @RequestParam(name = "value") String value, @RequestParam(name = "offset") int offset) {
        //调用dao层
        List<ModelEs> list = null;
        try {
            list = getEsModel.getModel(esClient,type,value,offset, ProjectConstant.PAGESIZE,sumMapper);

        } catch (Exception e) {
            return RetResponse.makeErrRsp("没有找到相关模型");
        }
        List<Model> resList = mix(list);
        if (type.equals("itemId")){
            String hashCode = list.get(0).getHashCode();
            resList.get(0).setHashCode(hashCode);
        }
//        System.out.println(list);

        return RetResponse.makeOKRsp(resList);
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public RetResult<String> search(HttpServletRequest httpServletRequest) throws Exception {
        List<String> list = fileUploadUtils.uploadFile(httpServletRequest);
        String hashcode;
        if (list != null){
            String objPath = list.get(0);
            System.out.println(objPath);
            hashcode = OldFeature1000.obj2Hashcode(objPath);
//            List<ModelEs> resList = getEsModel.getModel(esClient,"hashCode",hashcode,0);
//            ModelSql modelSql = new ModelSql();
//            for (ModelEs modelEs:resList){
//                modelSql = modelMapper.selectModelById(modelEs.getID(),"data");
//                System.out.println(modelEs);
//                System.out.println(modelSql);
//            }
            return RetResponse.makeOKRsp(hashcode);
        }else {
            return RetResponse.makeErrRsp("extract error");
        }

    }

    @RequestMapping(value = "/getCheckCode", method = RequestMethod.GET)
    public RetResult<String> getCheckCode(@Param("email") String email){
        List<user> list = userMapper.getUserByEmail(email,"user");
        if (list.size()==0){
            String checkCode = String.valueOf(new Random().nextInt(899999) + 100000);
            String message = "您的注册验证码为："+checkCode;
            try {
                mailService.sendSimpleMail(email, "注册验证码", message);
            }catch (Exception e){
                return RetResponse.makeErrRsp("邮箱注册失败，请检查邮箱是否正确");
            }
            return RetResponse.makeOKRsp(checkCode);
        }else {
            return RetResponse.makeErrRsp("该邮箱已经被注册");
        }
    }

    @RequestMapping(value = "/getCheckUser", method = RequestMethod.GET)
    public RetResult<String> getCheckUser(@Param("userName") String userName){
        List<user> list = userMapper.getUserByUserName(userName,"user");
        if (list.size()==0){
            return RetResponse.makeOKRsp("ok");
        }else {
            return RetResponse.makeErrRsp("该用户名已经被注册");
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public RetResult<Map> login(@Param("email") String email, @Param("psword") String psword){
        List<user> list = userMapper.getUserByEmail(email,"user");
        if (list.size()==0){
            return RetResponse.makeErrRsp("邮箱不正确");
        }else {
            user user = list.get(0);
            if (user.getPsword().equals(psword)){
                Map<String,String> map = new HashMap<>();
                map.put("userName",user.getUserName());
                map.put("identity",String.valueOf(user.getIdentity()));
                dateUtils.update(sumMapper,0,dateUtils.gainDate());
                dateUtils.update(sumMapper,0,"2000-00-00");
                return RetResponse.makeOKRsp(map);
            }else {
                return RetResponse.makeErrRsp("密码不正确");
            }
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public RetResult<String> login(@Param("userName") String userName, @Param("psword") String psword, @Param("email") String email){
        user user = new user();
        user.setUserName(userName);
        user.setPsword(psword);
        user.setEmail(email);
        int flag = userMapper.insertUser(user);
        if (flag==1){
            dateUtils.update(sumMapper,1,dateUtils.gainDate());
            dateUtils.update(sumMapper,1,"2000-00-00");
            return RetResponse.makeOKRsp("ok");
        }else {
            return RetResponse.makeErrRsp("注册失败");
        }
    }

    @RequestMapping(value = "/getRecommand", method = RequestMethod.GET)
    public RetResult<List> getRecommand(@Param("type") int type, @Param("value") String value, @Param("limit") int limit){
        List<Model> resList;
        String typestr = "";
        String valuestr = "";
        switch (type){
            case 0:typestr = "index";valuestr = "";break;
            case 1:typestr = "recom";valuestr = value;break;
            default:return RetResponse.makeErrRsp("type error");
        }
        List<ModelEs> list = null;
        try {
            list = getEsModel.getModel(esClient,typestr,valuestr,0, limit,sumMapper);
        } catch (Exception e) {
            return RetResponse.makeErrRsp("没有找到相关模型");
        }
        resList = mix(list);
        return RetResponse.makeOKRsp(resList);
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public RetResult<String> upload(HttpServletRequest httpServletRequest) throws Exception {
        List<String> list = fileUploadUtils.uploadFile(httpServletRequest);
        String myFileName = list.get(0);

        String tempdirName3 = myFileName.replace("E:/upload","").replace("1.obj","").replace("\\","/");
        //String ftpPath = "/home/cc/test/33/";
        String ftpPath = "/local_model/upload"+tempdirName3;
        String localPath = myFileName;
        String fileName = myFileName.substring(myFileName.lastIndexOf(File.separator)+1);

        //上传一个文件
        try {
            FileInputStream in = new FileInputStream(new File(localPath));
            boolean test = ftpUtils.uploadFile("192.168.199.100", "cc", "123", 21, ftpPath, fileName, in);
            System.out.println(test);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println(e);
        }

        if (list != null){
            ModelSql modelSql = new ModelSql();

            modelSql.setFilePath("K:"+tempdirName3+fileName);
            modelSql.setID(UUID());
            modelSql.setAuthor("");
            modelSql.setImgPath("");
            modelSql.setClassName("");
            modelSql.setSubclassName("");
            modelSql.setSize("");
            int itemId = modelMapper.insertModel(modelSql,ProjectConstant.TABLENAME);
            ModelEs modelEs = new ModelEs();
            modelEs.setDescription(" ");
            modelEs.setClassName(" ");
            modelEs.setSubClassName(" ");
            modelEs.setHashCode(OldFeature1000.obj2Hashcode(list.get(0)));
            modelEs.setID(modelSql.getID());
            getEsModel.indexES(esClient,modelEs);
            return RetResponse.makeOKRsp(modelSql.getID());
        }else {
            return RetResponse.makeErrRsp("upload error");
        }

    }

    @RequestMapping(value = "/modify", method = RequestMethod.POST)
    public RetResult<String> modify(modifyModel modifyModel) {
        if (modifyModel.getID() != null){
            modelMapper.updateModel(modifyModel,ProjectConstant.TABLENAME);
            ModelEs modelEs = null;
            try {
                modelEs = getEsModel.getModel(esClient,"itemId",modifyModel.getID(),0,1,sumMapper).get(0);
            } catch (Exception e) {
                return RetResponse.makeErrRsp("没有找到相关模型");
            }
            modelEs.setClassName(modifyModel.getType());
            modelEs.setSubClassName(modifyModel.getAttr());
            modelEs.setName(modifyModel.getName());
            modelEs.setDescription(modifyModel.getDescription());
            try {
                getEsModel.updateES(esClient,modelEs);
            } catch (IOException e) {
                return RetResponse.makeErrRsp("update error");
            }
            return RetResponse.makeOKRsp("ok");
        }else {
            return RetResponse.makeErrRsp("update error");
        }

    }

    @RequestMapping(value = "/modify", method = RequestMethod.GET)
    public RetResult<String> modify(@Param("itemId") String itemId) {
        modelMapper.deleteById(itemId,ProjectConstant.TABLENAME);
//        System.out.println("ok");
        try {
            getEsModel.deleteES(esClient,itemId);
        } catch (IOException e) {
            return RetResponse.makeErrRsp("delete error");
        }
        return RetResponse.makeOKRsp("ok");
    }

    @RequestMapping(value = "/getCount", method = RequestMethod.GET)
    public RetResult<String> getCount(@Param("type") int type, @Param("value") String value) {
        String tmp = "";
        switch (type){
            case 0 :tmp = "category";break;
            case 1 :tmp = "text";break;
            case 2 :tmp = "hashCode";break;
            default:break;
        }
        int count = 0;
        try {
            count = getEsModel.getCount(esClient,tmp,value);
        } catch (Exception e) {
            return RetResponse.makeErrRsp("没有相关模型");
        }
//        System.out.println("ok");
        return RetResponse.makeOKRsp(String.valueOf(count));
    }
    @RequestMapping(value = "/sum", method = RequestMethod.POST)
    public RetResult<String> Count(@Param("itemId")String itemId) {
        dateUtils.update(sumMapper,3,dateUtils.gainDate());
        dateUtils.update(sumMapper,3,"2000-00-00");
        ModelSql modelSql = modelMapper.selectModelById(itemId,ProjectConstant.TABLENAME);
        int n = modelSql.getDownloadTimes();
        n++;
        modelSql.setDownloadTimes(n);
        modelMapper.updateDownload(n,itemId,ProjectConstant.TABLENAME);
        return RetResponse.makeOKRsp("ok");
    }
    @RequestMapping(value = "/sum", method = RequestMethod.GET)
    public RetResult<List<sum>> getCount(@Param("date") String date) {
        List<sum> list0 = sumMapper.selectSumByDate("2000-00-00",ProjectConstant.SUMTABLE);
        List<sum> list = sumMapper.selectSumByDate(dateUtils.gainDate(),ProjectConstant.SUMTABLE);
        List<sum> list1 = sumMapper.selectSumByDate(date,ProjectConstant.SUMTABLE);
        if (list.size()==0){
            list0.add(null);
        }else {
            list0.addAll(list);
        }
        if (list1.size()==0){
            list0.add(null);
        }else {
            list0.addAll(list1);
        }
        return RetResponse.makeOKRsp(list0);
    }

    public static String JsonToMap(String json){
        Gson gson = new Gson();
        JsonParser jsonParser = new JsonParser();
//        System.out.println(json.trim().replaceAll("\\\\","/"));
        JsonArray jsonElements = jsonParser.parse(json.trim().replaceAll("\\\\","/")).getAsJsonArray();//获取JsonArray对象
        ArrayList<image> images = new ArrayList<>();
        for (JsonElement bean : jsonElements) {
            image image = gson.fromJson(bean, image.class);//解析
            images.add(image);
        }

//        List list = gson.fromJson(json, new TypeToken<List>() {}.getType());
//        System.out.println(list.get(0));
        return images.get(0).getImgURL();
    }

    public List<Model> mix(List<ModelEs> list){
        List<Model> resList = new ArrayList<>();
        for (ModelEs modelEs:list){
            ModelSql modelSql = modelMapper.selectModelById(modelEs.getID(),ProjectConstant.TABLENAME);
            String imgPath = modelSql.getImgPath();
            String objPath = modelSql.getFilePath();
            objPath = objPath.replace("K:","/model");
            modelSql.setFilePath(objPath);
//            System.out.println(imgPath);
            String imgUrl = "";
            if (imgPath.contains("[")){
                imgUrl = JsonToMap(imgPath);
                imgUrl = imgUrl.replace("H:","/img").replace("\\","/");
            }else {
                //文件存放问题
            }
            modelSql.setImgPath(imgUrl);
            Model model = new Model();
            model.setDescription(modelEs.getDescription());
            model.setName(modelEs.getName());
            model.setSize(modelSql.getSize());
            model.setAttr(modelSql.getSubclassName());
            model.setAuth(modelSql.getAuthor());
            model.setDownload(modelSql.getFilePath());
            model.setDownloadTimes(String.valueOf(modelSql.getDownloadTimes()));
            model.setImg(modelSql.getImgPath());
            model.setItemId(modelEs.getID());
            model.setModelSrc(modelSql.getFilePath());
            model.setMoney("$"+String.valueOf(Math.random()));
            model.setPic(modelSql.getImgPath());
            model.setType(modelSql.getClassName());
            List catgory = new ArrayList();
            catgory.add(modelSql.getClassName());
            catgory.add(modelSql.getSubclassName());
            model.setCategory(catgory);
            resList.add(model);
        }
        return resList;
    }

    public static String UUID() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return uuid;

    }
}
