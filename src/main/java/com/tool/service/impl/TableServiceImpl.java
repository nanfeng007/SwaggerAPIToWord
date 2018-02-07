package com.tool.service.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tool.dto.Request;
import com.tool.dto.Response;
import com.tool.dto.Table;
import com.tool.service.TableService;

/**
 * Created by zhangben on 2018/2/6.
 */
@Service
public class TableServiceImpl implements TableService {

	@Override
	public List<Table> tableList() {
		List<Table> list = new LinkedList();
		try {
			ClassLoader classLoader = TableService.class.getClassLoader();
			URL resource = classLoader.getResource("data.json");
			Map map = new ObjectMapper().readValue(resource, Map.class);
			//得到host，用于模拟http请求
			String host = String.valueOf(map.get("host"));
			//解析paths
			LinkedHashMap<String, LinkedHashMap> paths = (LinkedHashMap) map.get("paths");
			LinkedHashMap<String, LinkedHashMap> definitions = (LinkedHashMap) map.get("definitions");
			if (paths != null) {
				Iterator<Map.Entry<String, LinkedHashMap>> iterator = paths.entrySet().iterator();
				while (iterator.hasNext()) {
					Table table = new Table();
					List<Request> requestList = new LinkedList<Request>();
					String requestType = "";

					Map.Entry<String, LinkedHashMap> next = iterator.next();
					String url = next.getKey();//得到url
					LinkedHashMap<String, LinkedHashMap> value = next.getValue();
					//得到请求方式，输出结果类似为 get/post/delete/put 这样
					Set<String> requestTypes = value.keySet();
					for (String str : requestTypes) {
						requestType += str + "/";
					}
					Iterator<Map.Entry<String, LinkedHashMap>> it2 = value.entrySet().iterator();
					//解析请求
					Map.Entry<String, LinkedHashMap> get = it2.next();//得到get
					LinkedHashMap getValue = get.getValue();
					String title = (String) ((List) getValue.get("tags")).get(0);//得到大标题
					String tag = String.valueOf(getValue.get("summary"));
					//请求体
					ArrayList parameters = (ArrayList) getValue.get("parameters");
					if (parameters != null && parameters.size() > 0) {
						for (int i = 0; i < parameters.size(); i++) {
							Request request = new Request();
							LinkedHashMap<String, Object> param = (LinkedHashMap) parameters.get(i);
							request.setDescription(String.valueOf(param.get("description")));
							request.setName(String.valueOf(param.get("name")));
							request.setType(String.valueOf(param.get("type")));
							request.setParamType(String.valueOf(param.get("in")));

							request.setRequire((Boolean) param.get("required"));
							requestList.add(request);
							if(request.getParamType().equals("body")) {
								requestList.remove(i);
								LinkedHashMap<String, Object> temp = (LinkedHashMap)param.get("schema");
								if(String.valueOf(temp.get("$ref")).length()>14) {

									//参数是对象的拿到对象名
									String modelname = String.valueOf(temp.get("$ref")).substring(14);
									LinkedHashMap<String, Object> modelJson = (LinkedHashMap)definitions.get(modelname);

									JSONObject jsonObject = new JSONObject(modelJson);
									JSONObject properties = jsonObject.getJSONObject("properties");
									//获取所有对象的属性名
									Set<String> keyset = properties.keySet();
									for(String tempname : keyset) {
										JSONObject param1 = (JSONObject) properties.get(tempname);
										Request request1 = new Request();

										request1.setDescription(String.valueOf(param1.get("description")));
										request1.setName(tempname);
										request1.setType(String.valueOf(param1.get("type")));
										request1.setParamType("body");
										requestList.add(request1);
									}

								}
							}
						}
					}
					//返回体，比较固定
					List<Response> responseList = new LinkedList<Response>();
					//响应体
					LinkedHashMap<String, Object> responses = (LinkedHashMap) getValue.get("responses");
					if (responses != null && responses.size() > 0) {
						Set<String> keysetR = responses.keySet();
						for(String tempname2 : keysetR) {
								LinkedHashMap<String, Object> param3 = (LinkedHashMap) responses.get(tempname2);

								Set<String> keyset = param3.keySet();
							
								Response response = new Response();
								response.setName(tempname2);
								for(String tempname : keyset) {
								Object param2 =  param3.get(tempname);
								if(param2 instanceof String) {
									response.setDescription((String)param2);
								}
								if(param2 instanceof LinkedHashMap){
									LinkedHashMap<String, Object> param4 = (LinkedHashMap) param3.get(tempname);
									//System.out.println(param4);
									if(param4.get("$ref")!=null) {
										//LinkedHashMap<String, Object> temp = (LinkedHashMap)param4.get("schema");
										String op = (String)param4.get("$ref");
										if(op.length()>14) {
											String subop = op.substring(14);
											//参数是对象的拿到对象名
											//String modelname = String.valueOf(temp.get("$ref")).substring(14);
											LinkedHashMap<String, Object> modelJson = (LinkedHashMap)definitions.get(subop);

											JSONObject jsonObject = new JSONObject(modelJson);
											//JSONObject properties = jsonObject.getJSONObject("properties");

											response.setRemark(JSONObject.toJSONString(jsonObject));
										}

									}

								}
								
								}
								responseList.add(response);
							
						}
					}





					//模拟一次HTTP请求,封装请求体和返回体，如果是Restful的文档可以再补充
					//                    if (requestType.contains("post")) {
					//                        Map<String, String> stringStringMap = toPostBody(requestList);
					//                        table.setRequestParam(stringStringMap.toString());
					//                        String post = NetUtil.post(host + url, stringStringMap);
					//                        table.setResponseParam(post);
					//                    } else if (requestType.contains("get")) {
					//                        String s = toGetHeader(requestList);
					//                        table.setResponseParam(s);
					//                        String getStr = NetUtil.get(host + url + s);
					//                        table.setResponseParam(getStr);
					//                    }

					//封装Table
					table.setTitle(title);
					table.setUrl(url);
					table.setTag(tag);
					table.setResponseForm("application/json");
					table.setRequestType(StringUtils.removeEnd(requestType, "/"));
					table.setRequestList(requestList);
					table.setResponseList(responseList);
					list.add(table);
				}
			}
			
			//去除h4重复
			//增加h5的h4内序号
			String s = list.get(0).getTitle();
			Integer counth5 = 1;
			list.get(0).setTag(""+counth5.toString()+""+list.get(0).getTag());
			for(int i = 1 ; i <list.size();i++) {
				if(s.equals(list.get(i).getTitle())) {
					list.get(i).setTitle(null);
					counth5++;
					list.get(i).setTag(""+counth5.toString()+""+list.get(i).getTag());
				}else {
					s=list.get(i).getTitle();
					counth5 = 1;
					list.get(i).setTag(""+counth5.toString()+""+list.get(i).getTag());
				}
			}
			
			Integer count = 1;
			for(int i = 0 ; i <list.size();i++) {
				if(list.get(i).getTitle()!= null) {
					list.get(i).setTitle(""+count.toString() + " " + list.get(i).getTitle());
					count++;
				}
				
			}
			
			String num = list.get(0).getTitle().substring(0,1);
			for(int i = 0 ; i <list.size();i++) {
				//list.get(i).setTag(""+num+"."+list.get(i).getTag());
				if(list.get(i).getTitle() == null) {
					list.get(i).setTag(""+num+"."+list.get(i).getTag());
				}else {
					num = list.get(i).getTitle().substring(0,1);
					list.get(i).setTag(""+num+"."+list.get(i).getTag());
				}
				
				
				
			}
			
			
			
			
			
			return list;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	//封装返回信息，可能需求不一样，可以自定义
	private List<Response> listResponse() {
		List<Response> responseList = new LinkedList<Response>();
		//        responseList.add(new Response("受影响的行数", "counts", null));
		//        responseList.add(new Response("结果说明信息", "msg", null));
		//        responseList.add(new Response("是否成功", "success", null));
		responseList.add(new Response("返回对象", "data", null));
		responseList.add(new Response("错误代码", "httpCode", null));
		responseList.add(new Response("返回信息", "msg", null));
		return responseList;
	}

	//封装post请求体
	private Map<String, String> toPostBody(List<Request> list) {
		Map<String, String> map = new HashMap<>(16);
		if (list != null && list.size() > 0) {
			for (Request request : list) {
				String name = request.getName();
				String type = request.getType();
				switch (type) {
				case "string":
					map.put(name, "string");
					break;
				case "integer":
					map.put(name, "0");
					break;
				case "double":
					map.put(name, "0.0");
					break;
				default:
					map.put(name, "null");
					break;
				}
			}
		}
		return map;
	}

	//封装get请求头
	private String toGetHeader(List<Request> list) {
		StringBuffer stringBuffer = new StringBuffer();
		if (list != null && list.size() > 0) {
			for (Request request : list) {
				String name = request.getName();
				String type = request.getType();
				switch (type) {
				case "string":
					stringBuffer.append(name+"&=string");
					break;
				case "integer":
					stringBuffer.append(name+"&=0");
					break;
				case "double":
					stringBuffer.append(name+"&=0.0");
					break;
				default:
					stringBuffer.append(name+"&=null");
					break;
				}
			}
		}
		String s = stringBuffer.toString();
		if ("".equalsIgnoreCase(s)){
			return "";
		}
		return "?" + StringUtils.removeStart(s, "&");
	}
	
	
	
	@Override
	public List<Table> tableList2() {
		List<Table> list = new LinkedList();
		try {
			ClassLoader classLoader = TableService.class.getClassLoader();
			URL resource = classLoader.getResource("data.json");
			Map map = new ObjectMapper().readValue(resource, Map.class);
			//得到host，用于模拟http请求
			String host = String.valueOf(map.get("host"));
			//解析paths
			LinkedHashMap<String, LinkedHashMap> paths = (LinkedHashMap) map.get("paths");
			LinkedHashMap<String, LinkedHashMap> definitions = (LinkedHashMap) map.get("definitions");
			if (paths != null) {
				Iterator<Map.Entry<String, LinkedHashMap>> iterator = paths.entrySet().iterator();
				while (iterator.hasNext()) {
					Table table = new Table();
					List<Request> requestList = new LinkedList<Request>();
					String requestType = "";

					Map.Entry<String, LinkedHashMap> next = iterator.next();
					String url = next.getKey();//得到url
					LinkedHashMap<String, LinkedHashMap> value = next.getValue();
					//得到请求方式，输出结果类似为 get/post/delete/put 这样
					Set<String> requestTypes = value.keySet();
					for (String str : requestTypes) {
						requestType += str + "/";
					}
					Iterator<Map.Entry<String, LinkedHashMap>> it2 = value.entrySet().iterator();
					//解析请求
					Map.Entry<String, LinkedHashMap> get = it2.next();//得到get
					LinkedHashMap getValue = get.getValue();
					String title = (String) ((List) getValue.get("tags")).get(0);//得到大标题
					String tag = String.valueOf(getValue.get("summary"));
					//请求体
					ArrayList parameters = (ArrayList) getValue.get("parameters");
					if (parameters != null && parameters.size() > 0) {
						for (int i = 0; i < parameters.size(); i++) {
							Request request = new Request();
							LinkedHashMap<String, Object> param = (LinkedHashMap) parameters.get(i);
							request.setDescription(String.valueOf(param.get("description")));
							request.setName(String.valueOf(param.get("name")));
							request.setType(String.valueOf(param.get("type")));
							request.setParamType(String.valueOf(param.get("in")));

							request.setRequire((Boolean) param.get("required"));
							requestList.add(request);
							if(request.getParamType().equals("body")) {
								requestList.remove(i);
								LinkedHashMap<String, Object> temp = (LinkedHashMap)param.get("schema");
								if(String.valueOf(temp.get("$ref")).length()>14) {

									//参数是对象的拿到对象名
									String modelname = String.valueOf(temp.get("$ref")).substring(14);
									LinkedHashMap<String, Object> modelJson = (LinkedHashMap)definitions.get(modelname);

									JSONObject jsonObject = new JSONObject(modelJson);
									JSONObject properties = jsonObject.getJSONObject("properties");
									//获取所有对象的属性名
									Set<String> keyset = properties.keySet();
									for(String tempname : keyset) {
										JSONObject param1 = (JSONObject) properties.get(tempname);
										Request request1 = new Request();

										request1.setDescription(String.valueOf(param1.get("description")));
										request1.setName(tempname);
										request1.setType(String.valueOf(param1.get("type")));
										request1.setParamType("body");
										requestList.add(request1);
									}

								}
							}
						}
					}
					//返回体，比较固定
					List<Response> responseList = new LinkedList<Response>();
					//响应体
					LinkedHashMap<String, Object> responses = (LinkedHashMap) getValue.get("responses");
					if (responses != null && responses.size() > 0) {
						Set<String> keysetR = responses.keySet();
						for(String tempname2 : keysetR) {
								LinkedHashMap<String, Object> param3 = (LinkedHashMap) responses.get(tempname2);

								Set<String> keyset = param3.keySet();
							
								Response response = new Response();
								response.setName(tempname2);
								for(String tempname : keyset) {
								Object param2 =  param3.get(tempname);
								if(param2 instanceof String) {
									response.setDescription((String)param2);
								}
								if(param2 instanceof LinkedHashMap){
									LinkedHashMap<String, Object> param4 = (LinkedHashMap) param3.get(tempname);
									//System.out.println(param4);
									if(param4.get("$ref")!=null) {
										//LinkedHashMap<String, Object> temp = (LinkedHashMap)param4.get("schema");
										String op = (String)param4.get("$ref");
										if(op.length()>14) {
											String subop = op.substring(14);
											//参数是对象的拿到对象名
											//String modelname = String.valueOf(temp.get("$ref")).substring(14);
											LinkedHashMap<String, Object> modelJson = (LinkedHashMap)definitions.get(subop);

											JSONObject jsonObject = new JSONObject(modelJson);
											//JSONObject properties = jsonObject.getJSONObject("properties");

											response.setRemark(JSONObject.toJSONString(jsonObject));
										}

									}

								}
								
								}
								responseList.add(response);
							
						}
					}





					//模拟一次HTTP请求,封装请求体和返回体，如果是Restful的文档可以再补充
					//                    if (requestType.contains("post")) {
					//                        Map<String, String> stringStringMap = toPostBody(requestList);
					//                        table.setRequestParam(stringStringMap.toString());
					//                        String post = NetUtil.post(host + url, stringStringMap);
					//                        table.setResponseParam(post);
					//                    } else if (requestType.contains("get")) {
					//                        String s = toGetHeader(requestList);
					//                        table.setResponseParam(s);
					//                        String getStr = NetUtil.get(host + url + s);
					//                        table.setResponseParam(getStr);
					//                    }

					//封装Table
					table.setTitle(title);
					table.setUrl(url);
					table.setTag(tag);
					table.setResponseForm("application/json");
					table.setRequestType(StringUtils.removeEnd(requestType, "/"));
					table.setRequestList(requestList);
					table.setResponseList(responseList);
					list.add(table);
				}
			}
			
			
			
			Integer counth4h5 = 1;
			for(int i = 0 ; i <(list.size()-1);i++) {
				list.get(i).setTag(""+counth4h5.toString()+""+list.get(i).getTag());
				counth4h5++;
			}
			
			
			
			
			
			return list;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
