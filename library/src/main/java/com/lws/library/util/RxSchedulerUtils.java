package com.lws.library.util;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by lws on 2017/9/13.
 */

public final class RxSchedulerUtils {
    /**
     * 在RxJava的使用过程中会频繁地调用subscribeOn()和observeOn(),通过Transformer结合
     * Observable.compose()我们可以复用这些代码
     */
    public static <T> Observable.Transformer<T, T> normalSchedulersTransformer() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
