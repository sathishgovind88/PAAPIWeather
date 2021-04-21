package testWeather;

import static org.testng.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class testWeather {
	private static final String baseurl = "https://www.metaweather.com/api/";
	
	private Response response;
	private String jsonString;	
	private int strwoeid = 0;
	
	RequestSpecification request;
				
	@DataProvider
	public Object[][] getdatafromexcel() throws IOException{
		Object[][] xlarr = getexceldata();
		
		return(xlarr);
		
	}
		
	@Test(dataProvider="getdatafromexcel")
	public void getWeather(String strCity,String strDate) throws IOException {
		
		System.out.println(strCity + " " + strDate);			
				
		RestAssured.baseURI = baseurl;
		request = RestAssured.given();
				
		jsonString = "";
		
		response = request.get("/location/search/?query=" + strCity);
						 		 
		jsonString = response.asString();
		
		Assert.assertEquals(200, response.statusCode());
		 
		 jsonString = response.asString();
		 Reporter.log(jsonString);
		 
		 strwoeid = getwoeid();
		 
		 Reporter.log("WoeId:-" + strwoeid + "for the City:-" + strCity);
		 
		 if (strwoeid == 0) {
			 Assert.fail("WoeId should not be 0");
		 }
		 else {
			 getweatherdetails(strDate);
			 saveWeatherdetails(strCity,strDate);
		 }
	}
	
	public int getwoeid() {
		String[] arr;
		String[] idarr;
		
		arr = jsonString.split(",");
		 
		 for (int i = 0;i< arr.length;i++) {
			 if (arr[i].contains("woeid")) {
				 
				 idarr = arr[i].split(":");
				 				 
				 return Integer.parseInt(idarr[1].trim());				 		
			 }
		 }
		return 0;	
	}
	
	public void getweatherdetails(String strDate){
		response = request.get("/location/" + strwoeid + "/" + strDate);
		 
		jsonString = response.asString();
		
		Reporter.log(jsonString);	
				
	}
	
	public void saveWeatherdetails(String strCity,String strDate) throws IOException {
		
		boolean result;String filepath="";
		String[] arr;						
			
		strDate = strDate.replace("/", "-");
		
		filepath =  ".//WeatherDetails//" + strCity + "-" + strDate + ".txt";
		
		//System.out.println(filepath);
		
		File objfile = new File(filepath);
		
		result = objfile.createNewFile();
		
		if (result == true) {
			assertTrue(true, "New file is created Successfuly");
			
			FileWriter objwrt = new FileWriter(objfile);				
			BufferedWriter objbft = new BufferedWriter(objwrt);
			
			arr = jsonString.split(",");
			for (int i = 0;i< arr.length;i++) {	
								
				objbft.write(arr[i]);
				objbft.newLine();
			}
			objwrt.close();		
		}
		
	}
	
	public Object[][] getexceldata() throws IOException{
		
		String[][] dataarr;int rows;int cols;
		int ci =0;int cj = 0;
		
		FileInputStream objfile =new FileInputStream(".//Data.xls");
		HSSFWorkbook objwbk = new HSSFWorkbook(objfile);
		HSSFSheet objsht = objwbk.getSheetAt(0);
		
		rows = objsht.getLastRowNum();
		cols = 2;
		
		dataarr = new String[rows][cols];
				
		for (int i = 1; i <= rows; i++) {
			for(int j = 0; j < cols; j++) {
				dataarr[ci][j] = objsht.getRow(i).getCell(j).getStringCellValue();
			}
			ci++;
		}
		return dataarr;		
	}

 
}
