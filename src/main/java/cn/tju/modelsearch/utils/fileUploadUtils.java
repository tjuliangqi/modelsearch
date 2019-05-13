package cn.tju.modelsearch.utils;


import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class fileUploadUtils {

    public static List<String> uploadFile(HttpServletRequest request) throws Exception {
        List<String> list = new ArrayList<>();
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                request.getSession().getServletContext());
        if (multipartResolver.isMultipart(request)) {
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            Iterator<String> iterator = multiRequest.getFileNames();
            while (iterator.hasNext()) {
                // 取得上传文件
                MultipartFile file = multiRequest.getFile(iterator.next());
                if (file != null) {
                    // 取得当前上传文件的文件名称
                    String myFileName = "1.obj";
                    if (myFileName.contains("/")|myFileName.contains("\\"))
                    {
                        try{
                            myFileName = myFileName.substring(myFileName.lastIndexOf("/"),myFileName.length());
                        }catch (Exception e){
                            myFileName = myFileName.substring(myFileName.lastIndexOf("\\"),myFileName.length());
                        }
                    }
                    // 如果名称不为“”,说明该文件存在，否则说明该文件不存在
                    if (myFileName.trim() != "") {

                        String folderPath = ProjectConstant.SAVEFILEPATH + File.separator + folderName();
                        File fileFolder = new File(folderPath);
                        if (!fileFolder.exists() && !fileFolder.isDirectory()) {
                            fileFolder.mkdirs();
                        }
                        String tempdirName = folderPath + File.separator+UUID.randomUUID().toString() ;
                        File dir = new File(tempdirName);
                        if (!dir.exists()){
                            dir.mkdir();
                        }
                        File uploadFile = new File( tempdirName + File.separator + myFileName);
                        file.transferTo(uploadFile);
                        myFileName = tempdirName + File.separator + myFileName;
                        list.add(myFileName);
                    }
                }
            }
        }
        return list;
    }

    /**
     * 得年月日的文件夹名称
     *
     * @return
     */
    public static String getCurrentFilderName()  throws Exception{
        Calendar now = Calendar.getInstance();
        return now.get(Calendar.YEAR) + "" + (now.get(Calendar.MONTH) + 1) + "" + now.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 创建文件夹
     *
     * @param filderName
     */
    public static void createFilder(String filderName) throws Exception {
        File file = new File(filderName);
        // 如果文件夹不存在则创建
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
    }

    /**
     * 文件扩展名
     *
     * @param fileName
     * @return
     */
    public static String extFile(String fileName)  throws Exception{
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * 当前日期当文件夹名
     *
     * @return
     */
    public static String folderName() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String str = sdf.format(new Date());
        return str;
    }
}
