package com.yoake.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class R2LocationManager {

    private static volatile R2LocationManager INSTANCE;
    private final Context context;
    private final LocationManager locationManager;

    // 缓存相关
    private long lastLocationTime = 0L;
    private Location cacheLocation;

    // 实时任务管理
    private final Map<String, LocationListener> activeListeners = new ConcurrentHashMap<>();

    private R2LocationManager(Context context) {
        this.context = context.getApplicationContext();
        this.locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
    }

    public static R2LocationManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (R2LocationManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new R2LocationManager(context);
                }
            }
        }
        return INSTANCE;
    }

    public Location getLastLocation() {
        if (cacheLocation != null) return cacheLocation;
        return null;
    }


    /**
     * 获取当前位置，支持缓存，优先通过 NETWORK_PROVIDER，失败时回退到 GPS_PROVIDER
     * <p>
     * 如果失败了将使用最近一次的定位信息
     *
     * @param cacheTime 缓存时间（毫秒），如果缓存时间未过则直接返回缓存
     * @param callback  定位回调接口
     */
    public void getLocation(long cacheTime, LocationCallback callback) {
        getLocation(cacheTime, 0, null, (result, error) -> {
            Location location = result;
            if (location == null) {
                //定位失败，使用上次的定位信息，误差可能很大。
                location = getLastLocation();
            }
            callback.onResult(location, error);
        });
    }

    /**
     * 获取当前位置，支持缓存，优先通过 NETWORK_PROVIDER，失败时回退到 GPS_PROVIDER
     *
     * @param cacheTime 缓存时间（毫秒），如果缓存时间未过则直接返回缓存
     * @param interval  定位间隔（毫秒），0 表示只获取一次
     * @param key       标识任务的唯一 key，用于实时定位任务移除
     * @param callback  定位回调接口
     */
    public void getLocation(long cacheTime, long interval, String key, LocationCallback callback) {
        // 如果缓存有效，直接返回缓存位置
        if (cacheLocation != null && System.currentTimeMillis() - lastLocationTime < cacheTime) {
            callback.onResult(cacheLocation, null);
            return;
        }

        // 开始定位
        getLocationFromProvider(LocationManager.NETWORK_PROVIDER, interval, key, (networkLocation, networkError) -> {
            if (networkLocation != null) {
                callback.onResult(networkLocation, null);
            } else {
                // 回退到 GPS_PROVIDER
                getLocationFromProvider(LocationManager.GPS_PROVIDER, interval, key, (gpsLocation, gpsError) -> {
                    if (gpsLocation != null) {
                        callback.onResult(gpsLocation, null);
                    } else {
                        callback.onResult(null, gpsError != null ? gpsError : networkError);
                    }
                });
            }
        });
    }

    /**
     * 通用方法：根据指定的 provider 获取位置
     *
     * @param provider 定位提供者
     * @param interval 定位间隔（毫秒），0 表示只获取一次
     * @param key      唯一标识任务的 key
     * @param callback 定位回调接口
     */
    private void getLocationFromProvider(String provider, long interval, String key, LocationCallback callback) {
        AtomicBoolean hasResult = new AtomicBoolean(false);
        try {
            if (!locationManager.isProviderEnabled(provider)) {
                callback.onResult(null, LocationError.PROVIDER_DISABLED);
                return;
            }

            LocationListener listener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // 更新缓存
                    lastLocationTime = System.currentTimeMillis();
                    cacheLocation = location;
                    // 如果是一次性定位，自动移除监听器
                    if (interval == 0) {
                        locationManager.removeUpdates(this);
                        if (!hasResult.get()) {
                            callback.onResult(location, null);
                        }
                    } else {
                        callback.onResult(location, null);
                    }
                    hasResult.set(true);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                    // 回调错误信息
                    if (!hasResult.get()) {
                        callback.onResult(null, LocationError.PROVIDER_DISABLED);
                    }
                    hasResult.set(true);
                    // 自动移除监听器
                    locationManager.removeUpdates(this);

                    // 从实时任务列表中移除
                    if (interval > 0) {
                        activeListeners.remove(key);
                    }
                }
            };

            // 注册监听器
            locationManager.requestLocationUpdates(provider, interval, 0f, listener);
            // 设置超时机制，超过一定时间未获取到位置，则回调失败
            Handler handler = new Handler();
            Runnable timeoutRunnable = () -> {
                locationManager.removeUpdates(listener);  // 移除监听器
                if (!hasResult.get()) {
                    hasResult.set(true);
                    callback.onResult(null, LocationError.TIMEOUT);  // 超时回调失败
                }
            };

            // 设置超时为 5 秒
            handler.postDelayed(timeoutRunnable, 6000); // 超过10秒回调失败

            // 如果是实时定位，存储监听器
            if (interval > 0) {
                activeListeners.put(key, listener);
            }
        } catch (SecurityException e) {
            hasResult.set(true);
            callback.onResult(null, LocationError.PERMISSION_DENIED);
        } catch (Exception e) {
            hasResult.set(true);
            callback.onResult(null, LocationError.UNKNOWN_ERROR);
        }
    }

    /**
     * 停止实时定位
     *
     * @param key 唯一标识任务的 key
     */
    public void stopLocationUpdates(String key) {
        LocationListener listener = activeListeners.remove(key);
        if (listener != null) {
            locationManager.removeUpdates(listener);
        }
    }

    /**
     * 将经纬度转换为地址
     */
    public Address getAddressFromLocation(Location location) {
        return getAddressFromLocation(location.getLatitude(), location.getLongitude());
    }

    /**
     * 将经纬度转换为地址
     */
    public Address getAddressFromLocation(double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses == null || addresses.isEmpty()) {
                return null;
            }
            return addresses.get(0); // 返回第一条地址
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将地址转换为经纬度
     */
    public Pair<Double, Double> getLocationFromAddress(String address) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                return new Pair<>(location.getLatitude(), location.getLongitude());
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 错误类型定义
     */
    public enum LocationError {
        PROVIDER_DISABLED("Provider is disabled"),
        PERMISSION_DENIED("Permission denied"),
        UNKNOWN_ERROR("Unknown error"),
        TIMEOUT("timeout");


        private final String message;

        LocationError(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 定位结果回调接口
     */
    public interface LocationCallback {
        void onResult(Location location, LocationError error);
    }
}
