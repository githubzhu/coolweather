package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.R;
import com.coolweather.app.model.City;
import com.coolweather.app.model.CoolWeatherDB;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;


import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB ;
	private List<String> dataList = new ArrayList<String>() ;
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	private Province selectedProvince;
	private City selectedCity;
	private int currentLevel;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView)findViewById(R.id.list_view);
		titleText = (TextView)findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
		listView.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View view,
					int index, long arg3) {
				// TODO Auto-generated method stub
				if(currentLevel == LEVEL_PROVINCE){
					selectedProvince = provinceList.get(index);
					queryCities();
					
				}else if(currentLevel == LEVEL_CITY){
					selectedCity = cityList.get(index);
					queryCounties();
				}
			}

		
			
		});
		queryProvinces();
	}

	private void queryProvinces() {
		// TODO Auto-generated method stub
		provinceList = coolWeatherDB.loadProvinces();
		if(provinceList.size() > 0){
			dataList.clear();
			for(Province province:provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
			
			
		}else{
			queryFromSever(null,"province");
		}
	}



	protected void queryCities() {
		// TODO Auto-generated method stub
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		if(provinceList.size() > 0){
			dataList.clear();
			for(City city:cityList){
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
			
			
		}else{
			queryFromSever(selectedProvince.getProvinceCode(),"city");
		}
	}
	private void queryCounties() {
		// TODO Auto-generated method stub
		countyList = coolWeatherDB.loadCounties(selectedCity.getId());
		if(countyList.size() > 0){
			dataList.clear();
			for(County county:countyList){
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
			
			
		}else{
			queryFromSever(selectedCity.getCityCode(),"county");
		}
	}
	
	private void queryFromSever(final String  code, final String type) {
		// TODO Auto-generated method stub
		String address;
		if(!TextUtils.isEmpty(code)){
			address = "http://weather.com.cn/data/list3/city" + code + ".xml";
		}else{
			address = "http://weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address,new HttpCallbackListener(){

			public void onFinish(String response) {
				// TODO Auto-generated method stub
				boolean result = false;
				if("province".equals(type)){
					result = Utility.handleProvincesResponse(coolWeatherDB,response);
					
				}else if("city".equals(type)){
					result = Utility.handleCitiesResponse(coolWeatherDB,response,selectedProvince.getId());
					
				}else if("country".equals(type)){
					result = Utility.handleCountiesResponse(coolWeatherDB,response,selectedCity.getId());
					
				}
				if(result){
					runOnUiThread(new Runnable(){

						public void run() {
							// TODO Auto-generated method stub
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();
								
							}else if("city".equals(type)){
								queryCities();
								
							}else if("country".equals(type)){
								queryCounties();
								
							}
						}


					});
				}
			}

			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable(){

					public void run() {
						// TODO Auto-generated method stub
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
					}
					
				});
			}
			
		});
	}

	private void showProgressDialog() {
		// TODO Auto-generated method stub
		if(progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载");
			progressDialog.setCanceledOnTouchOutside(false);
			
		}
		progressDialog.show();
	}
	private void closeProgressDialog() {
		// TODO Auto-generated method stub
		if(progressDialog != null){
			progressDialog.dismiss();
		}
	}
	public void onBackPressed(){
		if(currentLevel == LEVEL_COUNTY){
			queryCounties();
		}else if(currentLevel == LEVEL_CITY){
			queryProvinces();
			
		}else{
			finish();
			
		}
	}

}
