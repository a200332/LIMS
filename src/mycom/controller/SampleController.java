package mycom.controller;


import java.net.URLDecoder;


import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mycom.dao.AnalysisItemsMapper;
import mycom.dao.CommonMapper;
import mycom.dao.ProductMapper;
import mycom.dao.SampleMapper;
import mycom.pojo.AnalysisItems;
import mycom.pojo.Product;
import mycom.pojo.Sample;

import mycom.util.dbutil;

import org.apache.ibatis.session.SqlSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;



@Controller
public class SampleController
{
	
	@ResponseBody
	@RequestMapping(value = "/getSampleByDeparmentIdAndStatus", produces = "text/plain; charset=utf-8")
	public String getSampleByDeparmentIdAndStatus(@RequestBody String str)
			throws Exception
	{
		int errorCode = 0;
		String reason = "";

		JSONObject req = new JSONObject(URLDecoder.decode(str, "UTF-8"));
		JSONObject resp = new JSONObject();
		
		int departmentId = req.getInt("departmentId");
		byte status = (byte) (((Integer) req.get("status")).intValue());	
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("departmentId", departmentId);
		map.put("status", status);

		
	
		SqlSession session = dbutil.getMybatisSqlSession();
        SampleMapper sample = session.getMapper(SampleMapper.class);
        
        List<Sample> result = sample.selectSampleByDeparmentIdAndStatus(map);
        System.out.println(result.get(0).getName());
        
        
	    session.close();
		resp.put("errorCode", errorCode);
		resp.put("reason", reason);
		resp.put("result", result);
		return resp.toString();
	}
	
	
	
	
	
	@ResponseBody
	@RequestMapping(value = "/postSampleAndAnalysisItems", produces = "text/plain; charset=utf-8")
	public String postSampleAndAnalysisItems(@RequestBody String str)
			throws Exception
	{
		int errorCode = 0;
		String reason = "";
		

		JSONObject req = new JSONObject(URLDecoder.decode(str, "UTF-8"));
		JSONObject resp = new JSONObject();

		SqlSession session = dbutil.getMybatisSqlSession();

		JSONArray analysisItemsId = (JSONArray) req.get("analysisItemsId");
		List idList = analysisItemsId.toList();
		AnalysisItemsMapper obj = session.getMapper(AnalysisItemsMapper.class);
		
		//获得所需的分析项目的记录；
		List analysisItemsList = obj.selectTableNameById(idList);
		
		//创建样品记录和各个分析项目的记录；
		SampleMapper sample = session.getMapper(SampleMapper.class);
		
		Sample sampleObj = new Sample();
		sampleObj.setCreater(Integer.valueOf((String) req.get("creater")));
		sampleObj.setDepartmentid(Integer.valueOf((String) req.get("departmentId")));
		sampleObj.setModelname((String) req.get("modelName"));
		sampleObj.setName((String) req.get("name"));
		
		sampleObj.setStatus((byte) (((Integer) req.get("status")).intValue()));		
		
		//Date
		Timestamp samplingTime = new Timestamp(System.currentTimeMillis());  
		Timestamp creationTime = new Timestamp(System.currentTimeMillis());
	    try 
	    {
	    	samplingTime = Timestamp.valueOf((String) req.get("samplingTime")); 
	        System.out.println(samplingTime);
	    } catch (Exception e)
	    {  
	        e.printStackTrace();  
	    }
	    
	    sampleObj.setSamplingtime(samplingTime);
		sampleObj.setCreationtime(creationTime );

		sample.insertSelective(sampleObj);
		int sampleId = sampleObj.getId();
		//System.out.println(SampleId );

		//样品基本信息插入结束，下面写分析项目插入
		for(int i = 0; i < analysisItemsList.size(); i++)
		{
			AnalysisItems analysisItem = (AnalysisItems) analysisItemsList.get(i);
			String tableName = analysisItem.getTablename();
			
			CommonMapper common = session.getMapper(CommonMapper.class);
			
			//在各个分析表中插入一条记录，只给表名和name(使用sample的值),sampleId属性；
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("tableName", "`" + tableName + "`");
			map.put("sampleId", sampleId);
			map.put("name", tableName);
			common.insertSample(map);
		}
		
		
		
		
		
	    session.commit();
	    session.close();
		
		String result = "";
		resp.put("errorCode", errorCode);
		resp.put("reason", reason);
		resp.put("result", result);
		return resp.toString();
	}
	
	
	
	
	
	
	@RequestMapping("/getSampleList")
	public ModelAndView getSampleList()
	{
		SqlSession session = dbutil.getMybatisSqlSession();
        SampleMapper sample = session.getMapper(SampleMapper.class);
        
        List<Sample> result = sample.selectAll();
        System.out.println(result.get(2).getName());
        
	    session.commit();
	    session.close();
		
		return new ModelAndView ("index", "result", result);
	}
}




