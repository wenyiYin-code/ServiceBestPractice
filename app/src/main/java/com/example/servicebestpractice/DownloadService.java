package com.example.servicebestpractice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.File;

public class DownloadService extends Service {

    private DownloadTask downloadTask;//下载任务
    private String downloadUrl;

    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification("Downloading...",progress));
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            // 下载成功时将前台服务通知关闭，并创建一个下载成功的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("Download Success",-1));
            Toast.makeText(DownloadService.this, "Download Success", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            // 下载失败时将前台服务通知关闭，并创建一个下载失败的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("Download Failed",-1));
            Toast.makeText(DownloadService.this, "Download Failed",Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onPaused() {
            downloadTask = null;
            Toast.makeText(DownloadService.this, "Paused", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this, "Canceled", Toast.LENGTH_SHORT).show();
        }
    };

    private DownloadBinder mBinder = new DownloadBinder();

    public DownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class DownloadBinder extends Binder {
        public void startDownload(String url) {
            if (downloadTask == null) {
                downloadUrl = url;
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(downloadUrl);
                startForeground(1, getNotification("Downloading...", 0));
                Toast.makeText(DownloadService.this, "Downloading...", Toast.LENGTH_SHORT).show();
            }
        }

        public void pauseDownload() {
            if (downloadTask != null) {
                downloadTask.pauseDownload();
            }
        }

        public void cancelDownload() {
            if (downloadTask != null) {
                downloadTask.cancelDownload();
            } else {
                if (downloadUrl != null) {
                    // 取消下载时需将文件删除，并将通知关闭
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory + fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownloadService.this, "Canceled",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private NotificationManager getNotificationManager() {//创建通知管理类
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private void createNotificationChannel() {//创建通知通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "001"; // 通知渠道的标识符
            CharSequence channelName = "Download"; // 通知渠道的位置
            String channelDescription = "正在下载中"; // 通知渠道的描述

            // 通知渠道的级别
            int importance = getNotificationManager().IMPORTANCE_DEFAULT;

            // 创建通知渠道
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationChannel.setDescription(channelDescription);

            // 系统中注册消息
            getNotificationManager().createNotificationChannel(notificationChannel);
        }
    }

    private Notification getNotification(String title, int progress) {

        // 创建通知渠道
        createNotificationChannel();

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        //创建通知
        if (progress>=0) {
            Notification notification = new NotificationCompat.Builder(this,"001")
                    .setContentTitle(title)//消息标题
                    .setContentText(progress + "%")
                    .setProgress(100,progress,false)
                    .setWhen(System.currentTimeMillis())//指定通知被通知创建的时间
                    .setSmallIcon(R.mipmap.ic_launcher)//通知的小图标
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))//通知的大图标
                    .setContentIntent(pi)
                    .build();
            return notification;
        } else {
            Notification notification = new NotificationCompat.Builder(this,"001")
                    .setContentTitle(title)//消息标题
                    .setWhen(System.currentTimeMillis())//指定通知被通知创建的时间
                    .setSmallIcon(R.mipmap.ic_launcher)//通知的小图标
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))//通知的大图标
                    .setContentIntent(pi)
                    .build();
            return notification;
        }
    }
}