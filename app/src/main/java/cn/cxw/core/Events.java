package cn.cxw.core;

import android.net.Uri;

import cn.cxw.model.Liberary;
import cn.cxw.model.Point;
import cn.cxw.model.Trails;

public class Events {

    public static class SearchPointEvent{
        public Point point;
        public SearchPointEvent(Point point) {
            this.point = point;
        }
    }

    public static class ShowPointEvent{
        public Liberary liberary;
        public ShowPointEvent(Liberary liberary) {
            this.liberary = liberary;
        }
    }

    public static class ShowTrailEvent{
        public Trails trail;

        public ShowTrailEvent(Trails trail) {
            this.trail = trail;
        }
    }

    public static class ReceiveWarningInfoEvent{
        public Object obj;
        public boolean ok;

        public ReceiveWarningInfoEvent(Object obj, boolean ok) {
            this.obj = obj;
            this.ok = ok;
        }
    }

    public static class ChangeArrowIconEvent{
        public int iconId;

        public ChangeArrowIconEvent(int iconId) {
            this.iconId = iconId;
        }
    }

    public static class ReceivedEditContentEvent{
        public String content;
        public Object info;

        public ReceivedEditContentEvent(String content, Object info) {
            this.content = content;
            this.info = info;
        }
    }

    public static class SavedTrailEvent{
        public String name;
        public boolean isRoad;

        public SavedTrailEvent(String name, boolean isRoad) {
            this.name = name;
            this.isRoad = isRoad;
        }
    }

    public static class OnRefreshListEvent {
        public int msgType;

        public OnRefreshListEvent(int msgType) {
            this.msgType = msgType;
        }

    }

    /**
     * App下载完成事件
     * @author cxw
     *
     */
    public static class AppDownloadCompleteEvent {
        public String mErrorMessage;
        public Uri mLocalInstallUri;
        public int mAppVersion;

        public AppDownloadCompleteEvent(Uri localInstallUri, int appVersion, String errorMessage) {
            mLocalInstallUri = localInstallUri;
            mErrorMessage = errorMessage;
            mAppVersion = appVersion;
        }
    }

}
