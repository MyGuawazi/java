package demo;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;


 
/**
 * HTTPClient动态调用webservice类
 */
public class DynamicHttpclientCall {
	
    private String namespace;
    private String methodName;
    private String wsdlLocation;
    private String soapResponseData;
    private String result;
    

    public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getWsdlLocation() {
		return wsdlLocation;
	}

	public void setWsdlLocation(String wsdlLocation) {
		this.wsdlLocation = wsdlLocation;
	}

	public String getSoapResponseData() {
		return soapResponseData;
	}
	
	/**
	 * 默认构造方法
	 */
	public DynamicHttpclientCall(){
		
	}
	
	/**
	 * 构造方法
	 * @param namespace webservice的命名空间
	 * @param methodName 要调用的方法名
	 * @param wsdlLocation webservice的wsdl地址
	 */
	public DynamicHttpclientCall(String namespace, String methodName,
            String wsdlLocation) {

        this.namespace = namespace;
        this.methodName = methodName;
        this.wsdlLocation = wsdlLocation;
    }
	
	/**
	 * 执行调用的方法
	 * @param patameterMap 传给webservice方法的参数，以键值对的形式存储在map中
	 * @return 返回结果状态码，200为调用成功
	 * @throws Exception
	 */
    public int invoke(Map<String, String> patameterMap) throws Exception {
    	int statusCode;
    	try {
    		 PostMethod postMethod = new PostMethod(wsdlLocation);	//新建一个postMethod，以便以post方式发送请求
    	        String soapRequestData = buildRequestData(patameterMap);//构造Soap请求字符串

    	        byte[] bytes = soapRequestData.getBytes("utf-8");//获取请求字符串的utf-8编码
    	        InputStream inputStream = new ByteArrayInputStream(bytes, 0, bytes.length);//将请求数据构建到输出流
    	        RequestEntity requestEntity = new InputStreamRequestEntity(inputStream,	//构建请求实体
    	                bytes.length, "application/soap+xml; charset=utf-8");
    	        postMethod.setRequestEntity(requestEntity);//将请求实体放进postMethod

    	        HttpClient httpClient = new HttpClient();//生成HttpClient对象
//    	        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(4000);
//    	        httpClient.getHttpConnectionManager().getParams().setSoTimeout(4000);
    	        statusCode = httpClient.executeMethod(postMethod);//用HttpClient发送Soap请求，并获取状态码
    	        soapResponseData = postMethod.getResponseBodyAsString();//获取返回的xml字符串，并赋值给soapResponseData
    	        XmlUtils xmlUtils = new XmlUtils(this.soapResponseData);
    	        this.result = xmlUtils.obtainValue(this.methodName+"Result");
		}catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
       
        return statusCode;																//返回状态码
    }
    
    
    /**
     * 根据参数构建Soap请求字符串
     * @param patameterMap 传给webservice方法的参数，以键值对的形式存储在map中
     * @return 返回请求字符串
     */
    public String buildRequestData(Map<String, String> patameterMap) {
    	Set<String> nameSet = patameterMap.keySet();
        StringBuffer soapRequestData = new StringBuffer();
        soapRequestData.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        soapRequestData
                .append("<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                        + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
                        + " xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">");
        soapRequestData.append("<soap12:Body>");
        soapRequestData.append("<" + methodName + " xmlns=\"" + namespace + "\">");
        for (String name : nameSet) {
            soapRequestData.append("<" + name + ">" + patameterMap.get(name) + "</" + name + ">");
        }
        soapRequestData.append("</" + methodName + ">");
        soapRequestData.append("</soap12:Body>");
        soapRequestData.append("</soap12:Envelope>");

        return soapRequestData.toString();
    }

    /**
     * @param args
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
        

        DynamicHttpclientCall dynamicHttpclientCall = new DynamicHttpclientCall(
                "http://schemas.xmlsoap.org/soap/envelope/", "CancelEntity",
                "http://192.168.14.36:8080/DirectSales.asmx");
    	
        Map<String, String> patameterMap = new HashMap<String, String>();

        patameterMap.put("EntityID", "1");
        patameterMap.put("closeDate", "2015-05-20");
        
        //执行调用方法
        String soapRequestData = dynamicHttpclientCall.buildRequestData(patameterMap);
        System.out.println(soapRequestData);
        
        int statusCode = dynamicHttpclientCall.invoke(patameterMap);
        if(statusCode == 200) {
            System.out.println("调用成功！");
            System.out.println(dynamicHttpclientCall.result);
            org.json.JSONObject json = new org.json.JSONObject(dynamicHttpclientCall.result);
            String su = json.getString("Success");
            String ms = json.getString("Message");
            System.out.println(su);
            System.out.println(ms);
           // JSONArray jsonObject = JSONArray.fromObject(dynamicHttpclientCall.result);
        }
        else {
            System.out.println("调用失败！错误码：" + statusCode);
        }
        
    }

}
