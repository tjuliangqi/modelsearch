package cn.tju.modelsearch.utils;

import cn.tju.modelsearch.dao.sumMapper;
import cn.tju.modelsearch.domain.sum;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class dateUtils {

    public static String gainDate(){
        Date date = new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        String dateStr=sdf.format(date);
        return dateStr;
    }
    public static void update(sumMapper sumMapper,int type,String date){
        List<sum> list = sumMapper.selectSumByDate(date,ProjectConstant.SUMTABLE);
        if (list.size()==0){
            sumMapper.insertSum(date,ProjectConstant.SUMTABLE);
        }
        List<sum> list1 = sumMapper.selectSumByDate(date,ProjectConstant.SUMTABLE);
        sum sum = list1.get(0);
        switch (type){
            case 0:{
                int n = sum.getLogin();
                n++;
                sum.setLogin(n);
                break;
            }
            case 1:{
                int n = sum.getRegis();
                n++;
                sum.setRegis(n);
                break;
            }
            case 2:{
                int n = sum.getDetail();
                n++;
                sum.setDetail(n);
                break;
            }
            case 3:{
                int n = sum.getDownload();
                n++;
                sum.setDownload(n);
                break;
            }
            default:break;
        }
        sumMapper.updateSum(sum,ProjectConstant.SUMTABLE);
    }
}
