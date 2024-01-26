package com.example.servicebestpractice;

public interface DownloadListener {

    void onProgress(int progress);//当前下载进度
    void onSuccess();//下载成功事件
    void onFailed();//下载失败事件
    void onPaused();//下载暂停事件
    void onCanceled();//下载取消事件

}
