package com.lws.android.library.util;

import android.content.Context;

import okhttp3.OkHttpClient;

/**
 * Created by lws on 2017/9/13.
 */

public interface StethoHelper {
    void init(Context context);

    OkHttpClient.Builder addNetworkInterceptor(OkHttpClient.Builder builder);
}
