package com.zcshou.joystick;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapView;
import com.zcshou.gogogo.R;

/**
 * Hosts the floating Baidu map and gives it Google-Maps-like compass behavior:
 * the compass is hidden while the map faces north, appears after rotation or
 * tilt, and the SDK's built-in compass restores the default north-up view when
 * tapped.
 */
public class CompassMapContainer extends FrameLayout {
    private static final float COMPASS_EPSILON_DEGREES = 0.5f;

    private BaiduMap mBaiduMap;
    private boolean mCompassVisible = false;

    public CompassMapContainer(Context context) {
        super(context);
    }

    public CompassMapContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CompassMapContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        post(this::initCompass);
    }

    private void initCompass() {
        MapView mapView = findViewById(R.id.map_joystick);
        if (mapView == null) {
            return;
        }

        mBaiduMap = mapView.getMap();
        if (mBaiduMap == null) {
            return;
        }

        // Keep the compass clearly visible inside the small floating map.
        int compassOffset = Math.round(36.0f * getResources().getDisplayMetrics().density);
        mBaiduMap.setCompassPosition(new Point(compassOffset, compassOffset));

        // Match Google Maps: hidden at north-up, visible only after rotation/tilt.
        mBaiduMap.getUiSettings().setCompassEnabled(false);
        mCompassVisible = false;
        updateCompassVisibility(mBaiduMap.getMapStatus());

        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus status) {
                updateCompassVisibility(status);
            }

            @Override
            public void onMapStatusChangeStart(MapStatus status, int reason) {
                updateCompassVisibility(status);
            }

            @Override
            public void onMapStatusChange(MapStatus status) {
                updateCompassVisibility(status);
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus status) {
                updateCompassVisibility(status);
            }
        });
    }

    private void updateCompassVisibility(MapStatus status) {
        if (mBaiduMap == null || status == null) {
            return;
        }

        final boolean shouldShow = Math.abs(status.rotate) > COMPASS_EPSILON_DEGREES
                || Math.abs(status.overlook) > COMPASS_EPSILON_DEGREES;

        // Baidu Map SDK 7.x may dispatch map-status callbacks off the UI thread.
        post(() -> {
            if (mBaiduMap == null || mCompassVisible == shouldShow) {
                return;
            }
            mBaiduMap.getUiSettings().setCompassEnabled(shouldShow);
            mCompassVisible = shouldShow;
        });
    }
}
