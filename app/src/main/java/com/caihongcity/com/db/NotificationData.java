package com.caihongcity.com.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2015/10/29 0029.
 */
public class NotificationData extends DataSupport{
    public String getUserPhoneNumer() {
        return userPhoneNumer;
    }

    public void setUserPhoneNumer(String userPhoneNumer) {
        this.userPhoneNumer = userPhoneNumer;
    }

    private String userPhoneNumer;
    private String notificationId;
    private String notificationTitle;
    private String notificationContent;
    private String notificationDate;
    private boolean notificationIsRead;

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getNotificationTitle() {
        return notificationTitle;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    public String getNotificationContent() {
        return notificationContent;
    }

    public void setNotificationContent(String notificationContent) {
        this.notificationContent = notificationContent;
    }

    public String getNotificationDate() {
        return notificationDate;
    }

    public void setNotificationDate(String notificationDate) {
        this.notificationDate = notificationDate;
    }

    public boolean isNotificationIsRead() {
        return notificationIsRead;
    }

    public void setNotificationIsRead(boolean notificationIsRead) {
        this.notificationIsRead = notificationIsRead;
    }
}
