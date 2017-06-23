package com.mapdemo.listener;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.search.core.PoiInfo;
import com.mapdemo.overlayutil.PoiOverlay;

import java.util.List;

/**
 * Created by ShiShow_xk on 2017/4/15.
 */
public class MyPoiOverlay extends PoiOverlay {
    private CallBack callBack;

    /**
     * 点击mark的回调
     */
    public interface CallBack {
        void onClick(List<PoiInfo> pioInfos, int index);
    }

    /**
     * 构造函数
     *
     * @param baiduMap 该 PoiOverlay 引用的 BaiduMap 对象
     */
    public MyPoiOverlay(BaiduMap baiduMap, CallBack callBack) {
        super(baiduMap);
        this.callBack = callBack;
    }

    @Override
    public boolean onPoiClick(int index) {
        super.onPoiClick(index);
        callBack.onClick(getPoiResult().getAllPoi(), index);
        return true;
    }
}
