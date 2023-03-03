package com.fruit.controllers;

import com.fruit.service.FruitService;
import com.fruit.service.impl.FruitServiceImpl;
import com.fruit.pojo.Fruit;
import com.myssm.myspringmvc.ViewBaseServlet;
import com.myssm.util.StringUtil;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

public class FruitController extends ViewBaseServlet {
    private FruitService fruitService = null;

    private String add(String fname,Integer price,Integer fcount,String remark) throws ServletException, IOException {
        fruitService.addFruit(new Fruit(0,fname,price,fcount,remark));
        return "redirect:fruit.do";
    }

    private String del(Integer fid)throws IOException, ServletException {
        if(fid!=null){
            fruitService.delFruit(fid);
            return "redirect:fruit.do";
        }
        return "error";
    }

    private String update(Integer fid,String fname,Integer price,Integer fcount,String remark){

        fruitService.updateFruit(new Fruit(fid,fname, price ,fcount ,remark ));

        //response.sendRedirect("fruit.do");
        return "redirect:fruit.do";
    }


    private String edit(Integer fid,HttpServletRequest request){
        if(fid!=null){
            Fruit fruit = fruitService.getFruitByFid(fid);
            request.setAttribute("fruit",fruit);
            //super.processTemplate("edit",req,resp);
            return "edit";
        }
        return "error";
    }

    private String index(String oper,String keyword,Integer pageNo,HttpServletRequest request) throws IOException {

            HttpSession session = request.getSession() ;
            if(pageNo==null)pageNo=1;

            if(!StringUtil.isEmpty(oper) && "search".equals(oper)){
                if(StringUtil.isEmpty(keyword))keyword="";
            }else {
                Object keywordObj = session.getAttribute("keyword");
                if(keywordObj!=null)keyword = (String)keywordObj;
                else keyword="";
            }

            session.setAttribute("pageNo",pageNo);
            session.setAttribute("keyword",keyword);
            List<Fruit> fruitList = fruitService.getFruitList(keyword,pageNo);

            session.setAttribute("fruitList",fruitList);

            //页数
            int pageCount = fruitService.getPageCount(keyword);

            session.setAttribute("pageCount",pageCount);

            return "index";
    }
}
