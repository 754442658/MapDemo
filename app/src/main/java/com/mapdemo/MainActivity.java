package com.mapdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.mapdemo.listener.MyLocationListener;
import com.mapdemo.listener.MyOrientationListener;
import com.mapdemo.listener.MyPoiOverlay;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends Activity implements OnGetPoiSearchResultListener {
    @InjectView(R.id.bmapView)
    MapView mMapView;
    @InjectView(R.id.et_search)
    EditText et_search;
    @InjectView(R.id.bt_model)
    Button bt_model;
    @InjectView(R.id.et_city)
    EditText et_city;
    private BaiduMap mBaiduMap;
    private static final String TAG = "MainActivity";

    // 定位相关
    public LocationClient mLocationClient = null;
    // 定位回调
    public BDLocationListener myListener;
    // 定位对象
    private BDLocation bdLocation;
    // 定位模式
    private MyLocationConfiguration.LocationMode model = MyLocationConfiguration.LocationMode.NORMAL;
    // 第一次定位
    private boolean isFirst = true;

    // 方向传感器
    MyOrientationListener myOriention;
    // 传感器方向角度
    private float lastX;

    // poi检索实例
    private PoiSearch poiSearch;

    // mark的点击回调
    private MyPoiOverlay.CallBack callBackOnMarkClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        try {
            initView();
            addListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        mBaiduMap = mMapView.getMap();
        // 设置地图缩放级别
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom
                (17).build()));
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        // 初始化定位 监听器
        myListener = new MyLocationListener(new MyLocationListener.CallBack() {
            @Override
            public void onResult(BDLocation location) {
                updateLocation(location);
            }
        });

        // 初始化方向传感器
        myOriention = new MyOrientationListener(this);
        myOriention.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                lastX = x;
            }
        });

        //注册定位监听函数
        mLocationClient.registerLocationListener(myListener);
        // 初始化定位
        initLocation();
        // 刚加载完毕调用一下定位
        mLocationClient.start();

        // 初始化POI检索实例
        poiSearch = PoiSearch.newInstance();
        // 设置POI检索监听器
        poiSearch.setOnGetPoiSearchResultListener(this);

        // 初始化mark的点击回调事件
        callBackOnMarkClick = new MyPoiOverlay.CallBack() {
            @Override
            public void onClick(List<PoiInfo> pioInfos, int index) {
                PoiInfo poiInfo = pioInfos.get(index);
                // 检索poi详细信息
                poiSearch.searchPoiDetail(new PoiDetailSearchOption()
                        .poiUid(poiInfo.uid));
            }
        };
    }

    private void addListener() {

    }

    /**
     * 初始化定位配置
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认gcj02，设置返回的定位结果坐标系
        option.setCoorType("bd09ll");
        int span = 0;
        if (model == MyLocationConfiguration.LocationMode.COMPASS) {
            // 罗盘，带方向
            span = 1000;
            // 开启方向传感器
            myOriention.start();
        } else {
            // 普通
            span = 10000;
            if (myOriention.isRun) {
                // 如果正在监听传感器就关闭方向传感器
                myOriention.stop();
            }
        }
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setScanSpan(span);
        //可选，设置是否需要地址信息，默认不需要
        option.setIsNeedAddress(true);
        //可选，默认false,设置是否使用gps
        option.setOpenGps(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setLocationNotify(false);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIsNeedLocationPoiList(true);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setIgnoreKillProcess(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.SetIgnoreCacheException(false);

        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        option.setEnableSimulateGps(false);
        mLocationClient.setLocOption(option);

        // 开始定位
        mLocationClient.start();
    }

    @OnClick({R.id.bt_pt, R.id.bt_wx, R.id.bt_jt, R.id.bt_rl, R.id.bt_model, R.id.start, R.id
            .bt_search, R.id.bt_searchArea})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.start:
                startActivity(new Intent(this, SensorActivity.class));
                break;
            case R.id.bt_pt:
                //普通地图
                Log.e(TAG, "onViewClicked: 普通图");
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            case R.id.bt_wx:
                //卫星地图
                Log.e(TAG, "onViewClicked: 卫星图");
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.bt_jt:
                //开启交通图
                Log.e(TAG, "onViewClicked: 交通图");
                if (mBaiduMap.isTrafficEnabled())
                    mBaiduMap.setTrafficEnabled(false);
                else
                    mBaiduMap.setTrafficEnabled(true);
                break;
            case R.id.bt_rl:
                //开启热力图
                Log.e(TAG, "onViewClicked: 热力图");
                if (mBaiduMap.isBaiduHeatMapEnabled())
                    mBaiduMap.setBaiduHeatMapEnabled(false);
                else
                    mBaiduMap.setBaiduHeatMapEnabled(true);
                break;
            case R.id.bt_model:
                //开启定位图
                Log.e(TAG, "onViewClicked: 开启定位");
//                mLocationClient.start();
//                // 开启定位图层
//                mBaiduMap.setMyLocationEnabled(true);
//                // 开启方向传感器
//                myOriention.start();

                if (model == MyLocationConfiguration.LocationMode.NORMAL) {
                    model = MyLocationConfiguration.LocationMode.COMPASS;
                    bt_model.setText("定位模式/罗盘");
                } else if (model == MyLocationConfiguration.LocationMode.COMPASS) {
                    model = MyLocationConfiguration.LocationMode.NORMAL;
                    bt_model.setText("定位模式/普通");
                }
                initLocation();
                break;
            case R.id.bt_search:
                // poi搜索
                if (TextUtils.isEmpty(et_search.getText())) {
                    Toast.makeText(this, "请输入要搜索的内容", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(et_city.getText())) {
                    // 如果没有写城市，就搜索周边
                    LatLng cenpt = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                    poiSearch.searchNearby(new PoiNearbySearchOption()
                            //搜索结果排序规则，PoiSortType.comprehensive->距离排序
                            .sortType(PoiSortType.comprehensive) //->综合排序；
                            .radius(1000)//检索半径范围，单位：米
                            .location(cenpt) //检索位置
                            .keyword(et_search.getText().toString().trim())
                            .pageNum(0)
                    );
                } else {
                    poiSearch.searchInCity((new PoiCitySearchOption())
                            .city(et_city.getText().toString().trim())
                            .keyword(et_search.getText().toString().trim())
                            // pageNum 是检索的页码从0开始，如：10 表示检索第十页
                            .pageNum(0));
                }
                break;
            case R.id.bt_searchArea:
                // 区域检索
                if (TextUtils.isEmpty(et_search.getText())) {
                    Toast.makeText(this, "请输入要搜索的内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                LatLng southwest = new LatLng(bdLocation.getLatitude() - 0.01, bdLocation.getLongitude() - 0.012);// 西南
                LatLng northeast = new LatLng(bdLocation.getLatitude() + 0.01, bdLocation.getLongitude() + 0.012);// 东北
                LatLngBounds bounds = new LatLngBounds.Builder().include(southwest)
                        .include(northeast).build();// 得到一个地理范围对象

                poiSearch.searchInBound(new PoiBoundSearchOption()
                        .bound(bounds)
                        .keyword(et_search.getText().toString())
                        .pageNum(0));// 发起poi范围检索请求
                break;
        }
    }

    /**
     * 更新定位信息
     */
    private void updateLocation(BDLocation location) {

        bdLocation = location;

        // 如果是第一次定位，并且是普通模式的话，地图定位到中心
        if (isFirst && model == MyLocationConfiguration.LocationMode.NORMAL) {
            //设定中心点坐标
            LatLng cenpt = new LatLng(location.getLatitude(), location.getLongitude());
            //定义地图状态
            MapStatus mMapStatus = new MapStatus.Builder()
                    .target(cenpt)
                    .zoom(18)
                    .build();
            //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化

            MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
            //改变地图状态
            mBaiduMap.setMapStatus(mMapStatusUpdate);
            isFirst = false;
        }


        // 构造定位数据
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(lastX)
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .build();
        // 设置定位数据
        mBaiduMap.setMyLocationData(locData);
        // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                .fromResource(R.mipmap.icon_location);

        // 定位模式
        // COMPASS        罗盘态，显示定位方向圈，保持定位图标在地图中心
        // FOLLOWING      跟随态，保持定位图标在地图中心
        // NORMAL         普通态： 更新定位数据时不对地图做任何操作
        MyLocationConfiguration.LocationMode mCurrentMode = model;
        MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, null);
        mBaiduMap.setMyLocationConfiguration(config);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
        // 注销定位监听器
        mLocationClient.unRegisterLocationListener(myListener);
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        // 释放poi检索
        poiSearch.destroy();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    /**
     * poi检索相关
     *
     * @param result
     */
    @Override
    public void onGetPoiResult(PoiResult result) {
        //获取POI检索结果
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            // 检索结果正常返回
            mBaiduMap.clear();
            MyPoiOverlay poiOverlay = new MyPoiOverlay(mBaiduMap, callBackOnMarkClick);
            poiOverlay.setData(result);// 设置POI数据
            mBaiduMap.setOnMarkerClickListener(poiOverlay);
            poiOverlay.addToMap();// 将所有的overlay添加到地图上
            poiOverlay.zoomToSpan();
            // 获取总分页数
            int totalPage = result.getTotalPageNum();
            Toast.makeText(
                    MainActivity.this,
                    "总共查到" + result.getTotalPoiNum() + "个兴趣点, 分为"
                            + totalPage + "页", Toast.LENGTH_SHORT).show();
        }


        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            //详情检索失败
            // result.error请参考SearchResult.ERRORNO
            Log.e(TAG, "onGetPoiResult: 检索失败");
        } else {
            //检索成功
            Log.e(TAG, "onGetPoiResult: 检索成功");
            List<PoiInfo> poiInfos = result.getAllPoi();
            // 地图定位到第一个mark
            if (poiInfos.size() > 0) {
                // 更改地图位置

                //设定中心点坐标
                LatLng cenpt = poiInfos.get(0).location;
                //定义地图状态
                MapStatus mMapStatus = new MapStatus.Builder()
                        .target(cenpt)
                        .zoom(18)
                        .build();
                //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化

                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                //改变地图状态
                mBaiduMap.setMapStatus(mMapStatusUpdate);
            }


            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < poiInfos.size(); i++) {
                sb.append("********************************************").append("\n");
                sb.append("name = ").append(poiInfos.get(i).name).append("\n");
                sb.append("uid = ").append(poiInfos.get(i).uid).append("\n");
                sb.append("address = ").append(poiInfos.get(i).address).append("\n");
                sb.append("city = ").append(poiInfos.get(i).city).append("\n");
                sb.append("phoneNum = ").append(poiInfos.get(i).phoneNum).append("\n");
                sb.append("postCode = ").append(poiInfos.get(i).postCode).append("\n");
                sb.append("type = ").append(poiInfos.get(i).type).append("\n");
                if (poiInfos.get(i).location != null) {
                    sb.append("latitude = ").append(poiInfos.get(i).location.latitude).append("\n");
                    sb.append("longitude = ").append(poiInfos.get(i).location.longitude).append("\n");
                }
                sb.append("hasCaterDetails = ").append(poiInfos.get(i).hasCaterDetails).append("\n");
                sb.append("isPano = ").append(poiInfos.get(i).isPano).append("\n");
            }

            String text = sb.toString();
            MyDialog dialog = new MyDialog(MainActivity.this, text);
            dialog.show();
        }
    }

    /**
     * poi详情检索相关
     */
    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
        Log.e(TAG, "onGetPoiDetailResult: " + poiDetailResult.toString());
        Log.e(TAG, "name = " + poiDetailResult.getName());
        Log.e(TAG, "address = " + poiDetailResult.getAddress());
        Log.e(TAG, "telephone = " + poiDetailResult.getTelephone());
        Log.e(TAG, "uid = " + poiDetailResult.getUid());
        Log.e(TAG, "tag = " + poiDetailResult.getTag());
        Log.e(TAG, "detailUrl = " + poiDetailResult.getDetailUrl());
        Log.e(TAG, "type = " + poiDetailResult.getType());
        Log.e(TAG, "price = " + poiDetailResult.getPrice());
        Log.e(TAG, "overallRating = " + poiDetailResult.getOverallRating());
        Log.e(TAG, "tasteRating = " + poiDetailResult.getTasteRating());
        Log.e(TAG, "serviceRating = " + poiDetailResult.getServiceRating());
        Log.e(TAG, "environmentRating = " + poiDetailResult.getEnvironmentRating());
        Log.e(TAG, "facilityRating = " + poiDetailResult.getFacilityRating());
        Log.e(TAG, "hygieneRating = " + poiDetailResult.getHygieneRating());
        Log.e(TAG, "technologyRating = " + poiDetailResult.getTechnologyRating());
        Log.e(TAG, "imageNum = " + poiDetailResult.getImageNum());
        Log.e(TAG, "grouponNum = " + poiDetailResult.getGrouponNum());
        Log.e(TAG, "commentNum = " + poiDetailResult.getCommentNum());
        Log.e(TAG, "favoriteNum = " + poiDetailResult.getFavoriteNum());
        Log.e(TAG, "checkinNum = " + poiDetailResult.getCheckinNum());
        Log.e(TAG, "shopHours = " + poiDetailResult.getShopHours());
    }

    /**
     * poi检索相关
     */
    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }
}
