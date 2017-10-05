package cn.cxw.util;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;

import cn.cxw.model.GaussPoint;

/**
 * Created by CXW-IBM on 2017/8/27.
 */

public class CoordinateUtil {


    //  由高斯投影坐标反算成经纬度  X 纬度， Y经度
    public static LatLng GaussToGPS(double x, double y) {
        int ProjNo;
        int ZoneWide; ////带宽

        double longitude1, latitude1, longitude0, X0, Y0, xval, yval;//latitude0,
        double e1, e2, f, a, ee, NN, T, C, M, D, R, u, fai, iPI;
        iPI = 0.0174532925199433; ////3.1415926535898/180.0;
        //a = 6378245.0; f = 1.0/298.3; //54年北京坐标系参数
        a = 6378140.0;
        f = 1 / 298.257; //80年西安坐标系参数
        ZoneWide = 6; ////6度带宽
        ProjNo = (int) (y / 1000000L); //查找带号
        longitude0 = (ProjNo - 1) * ZoneWide + ZoneWide / 2;
        longitude0 = longitude0 * iPI; //中央经线


        X0 = ProjNo * 1000000L + 500000L;
        Y0 = 0;
        xval = y - X0;
        yval = x - Y0; //带内大地坐标
        e2 = 2 * f - f * f;
        e1 = (1.0 - Math.sqrt(1 - e2)) / (1.0 + Math.sqrt(1 - e2));
        ee = e2 / (1 - e2);
        M = yval;
        u = M / (a * (1 - e2 / 4 - 3 * e2 * e2 / 64 - 5 * e2 * e2 * e2 / 256));
        fai = u + (3 * e1 / 2 - 27 * e1 * e1 * e1 / 32) * Math.sin(2 * u) + (21 * e1 * e1 / 16 - 55 * e1 * e1 * e1 * e1 / 32) * Math.sin(
                4 * u)
                + (151 * e1 * e1 * e1 / 96) * Math.sin(6 * u) + (1097 * e1 * e1 * e1 * e1 / 512) * Math.sin(8 * u);
        C = ee * Math.cos(fai) * Math.cos(fai);
        T = Math.tan(fai) * Math.tan(fai);
        NN = a / Math.sqrt(1.0 - e2 * Math.sin(fai) * Math.sin(fai));
        R = a * (1 - e2) / Math.sqrt((1 - e2 * Math.sin(fai) * Math.sin(fai)) * (1 - e2 * Math.sin(fai) * Math.sin(fai)) * (1 - e2 * Math.sin
                (fai) * Math.sin(fai)));
        D = xval / NN;
        //计算经度(Longitude) 纬度(Latitude)
        longitude1 = longitude0 + (D - (1 + 2 * T + C) * D * D * D / 6 + (5 - 2 * C + 28 * T - 3 * C * C + 8 * ee + 24 * T * T) * D
                * D * D * D * D / 120) / Math.cos(fai);
        latitude1 = fai - (NN * Math.tan(fai) / R) * (D * D / 2 - (5 + 3 * T + 10 * C - 4 * C * C - 9 * ee) * D * D * D * D / 24
                + (61 + 90 * T + 298 * C + 45 * T * T - 256 * ee - 3 * C * C) * D * D * D * D * D * D / 720);
        //转换为度 DD
        //latitude = latitude1 / iPI;
        //longitude = longitude1 / iPI;
        return new LatLng(latitude1 / iPI, longitude1 / iPI);
    }

    ////  由经纬度反算成高斯投影坐标

    ////  由经纬度反算成高斯投影坐标
    public static GaussPoint GPSToGauss(LatLng gps){
        return GPSToGauss(gps.latitude, gps.longitude);
    }
    public static GaussPoint GPSToGauss(double latitude, double longitude) {
        int ProjNo = 0;
        int ZoneWide; ////带宽
        double longitude1, latitude1, longitude0, latitude0, X0, Y0, x, y;
        double a, f, e2, ee, NN, T, C, A, M, iPI;
        iPI = 0.0174532925199433; ////3.1415926535898/180.0;
        ZoneWide = 6; ////6度带宽
        a = 6378245.0;
        f = 1.0 / 298.3; //54年北京坐标系参数
        ////a=6378140.0; f=1/298.257; //80年西安坐标系参数
        ProjNo = (int) (longitude / ZoneWide);
        longitude0 = ProjNo * ZoneWide + ZoneWide / 2;
        longitude0 = longitude0 * iPI;
        latitude0 = 0;
        System.out.println(latitude0);
        longitude1 = longitude * iPI; //经度转换为弧度
        latitude1 = latitude * iPI; //纬度转换为弧度
        e2 = 2 * f - f * f;
        ee = e2 * (1.0 - e2);
        NN = a / Math.sqrt(1.0 - e2 * Math.sin(latitude1) * Math.sin(latitude1));
        T = Math.tan(latitude1) * Math.tan(latitude1);
        C = ee * Math.cos(latitude1) * Math.cos(latitude1);
        A = (longitude1 - longitude0) * Math.cos(latitude1);
        M = a * ((1 - e2 / 4 - 3 * e2 * e2 / 64 - 5 * e2 * e2 * e2 / 256) * latitude1 - (3 * e2 / 8 + 3 * e2 * e2 / 32 + 45 * e2 * e2
                * e2 / 1024) * Math.sin(2 * latitude1)
                + (15 * e2 * e2 / 256 + 45 * e2 * e2 * e2 / 1024) * Math.sin(4 * latitude1) - (35 * e2 * e2 * e2 / 3072) * Math.sin(6 * latitude1));
        y = NN * (A + (1 - T + C) * A * A * A / 6 + (5 - 18 * T + T * T + 72 * C - 58 * ee) * A * A * A * A * A / 120);
        x = M + NN * Math.tan(latitude1) * (A * A / 2 + (5 - T + 9 * C + 4 * C * C) * A * A * A * A / 24
                + (61 - 58 * T + T * T + 600 * C - 330 * ee) * A * A * A * A * A * A / 720);
        X0 = 1000000L * (ProjNo + 1) + 500000L;
        Y0 = 0;
        y = y + X0;
        x = x + Y0;

        System.out.println("x：" + x);
        System.out.println("y：" + y);

        GaussPoint output = new GaussPoint();
        output.x = x;//纬度
        output.y = y;//经度
        return output;

    }

    public static GaussPoint BDToGauss(LatLng bdLocation) {
        return GPSToGauss(BDToGPS(bdLocation.latitude, bdLocation.longitude));
    }

    /********************************************
     * 将百度经纬度坐标转换为GPS经纬度坐标
     ********************************************/
    public static LatLng BDToGPS(BDLocation bdLocation) {
        return BDToGPS(bdLocation.getLatitude(), bdLocation.getLongitude());
    }

    //将百度坐标转换为GPS坐标
    public static LatLng BDToGPS(double x1, double y1) {
        double x2, y2;
        double x, y;
        LatLng desLatLng = GPSToBD(x1, y1);
        x2 = desLatLng.latitude;
        y2 = desLatLng.longitude;
        x = 2 * x1 - x2;
        y = 2 * y1 - y2;
        desLatLng = new LatLng(x, y);

        return desLatLng;
    }

    // 将GPS设备采集的原始GPS坐标转换成百度坐标
    public static LatLng GPSToBD(double x1, double y1) {
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);

        LatLng sourceLatLng = new LatLng(x1, y1);
        converter.coord(sourceLatLng);
        return converter.convert();
    }

    public final static double a = 6378245.0;
    public final static double ee = 0.00669342162296594323;

    // 判断坐标是否在中国
    public static boolean outOfChina(BDLocation bdLocation) {
        double lat = bdLocation.getLatitude();
        double lon = bdLocation.getLongitude();
        if (lon < 72.004 || lon > 137.8347)
            return true;
        if (lat < 0.8293 || lat > 55.8271)
            return true;
        if ((119.962 < lon && lon < 121.750) && (21.586 < lat && lat < 25.463))
            return true;

        return false;
    }

    public final static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;

    public static BDLocation BAIDU_to_WGS84(BDLocation bdLocation) {
        if (outOfChina(bdLocation)) {
            return bdLocation;
        }
        double x = bdLocation.getLongitude() - 0.0065;
        double y = bdLocation.getLatitude() - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        bdLocation.setLongitude(z * Math.cos(theta));
        bdLocation.setLatitude(z * Math.sin(theta));
        return GCJ02_to_WGS84(bdLocation);
    }

    public static BDLocation GCJ02_to_WGS84(BDLocation bdLocation) {
        if (outOfChina(bdLocation)) {
            return bdLocation;
        }
        BDLocation tmpLocation = new BDLocation();
        tmpLocation.setLatitude(bdLocation.getLatitude());
        tmpLocation.setLongitude(bdLocation.getLongitude());
        BDLocation tmpLatLng = WGS84_to_GCJ02(tmpLocation);
        double tmpLat = 2 * bdLocation.getLatitude() - tmpLatLng.getLatitude();
        double tmpLng = 2 * bdLocation.getLongitude() - tmpLatLng.getLongitude();
        for (int i = 0; i < 0; ++i) {
            tmpLocation.setLatitude(bdLocation.getLatitude());
            tmpLocation.setLongitude(bdLocation.getLongitude());
            tmpLatLng = WGS84_to_GCJ02(tmpLocation);
            tmpLat = 2 * tmpLat - tmpLatLng.getLatitude();
            tmpLng = 2 * tmpLng - tmpLatLng.getLongitude();
        }
        bdLocation.setLatitude(tmpLat);
        bdLocation.setLongitude(tmpLng);
        return bdLocation;
    }

    public static BDLocation WGS84_to_GCJ02(BDLocation bdLocation) {
        if (outOfChina(bdLocation)) {
            return bdLocation;
        }
        double dLat = transformLat(bdLocation.getLongitude() - 105.0,
                bdLocation.getLatitude() - 35.0);
        double dLon = transformLon(bdLocation.getLongitude() - 105.0,
                bdLocation.getLatitude() - 35.0);
        double radLat = bdLocation.getLatitude() / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0)
                / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI);
        bdLocation.setLatitude(bdLocation.getLatitude() + dLat);
        bdLocation.setLongitude(bdLocation.getLongitude() + dLon);
        return bdLocation;
    }

    public static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
                + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x
                * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0
                * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y
                * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    public static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
                * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x
                * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0
                * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x
                / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }
}
