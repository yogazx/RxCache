package com.safframework.rxcache.transformstrategy.impl;

import com.safframework.rxcache.RxCache;
import com.safframework.rxcache.domain.Record;
import com.safframework.rxcache.domain.Source;
import com.safframework.rxcache.transformstrategy.FlowableStrategy;
import com.safframework.rxcache.transformstrategy.MaybeStrategy;
import com.safframework.rxcache.transformstrategy.ObservableStrategy;
import io.reactivex.*;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import org.reactivestreams.Publisher;

import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * 先获取缓存，再获取网络请求
 * Created by tony on 2019-01-25.
 */
public class CacheAndRemoteStrategy implements ObservableStrategy,
        FlowableStrategy,
        MaybeStrategy {


    @Override
    public <T> Publisher<Record<T>> execute(RxCache rxCache, String key, Flowable<T> source, Type type) {

        Flowable<Record<T>> cache = rxCache.<T>load2Flowable(key, type);

        Flowable<Record<T>> remote = source
                .map(new Function<T, Record<T>>() {
                    @Override
                    public Record<T> apply(@NonNull T t) throws Exception {

                        rxCache.save(key, t);

                        return new Record<>(Source.CLOUD, key, t);
                    }
                });

        return Flowable.concatDelayError(Arrays.asList(cache, remote))
                .filter(new Predicate<Record<T>>() {
                    @Override
                    public boolean test(@NonNull Record<T> record) throws Exception {
                        return record.getData() != null;
                    }
                });
    }

    @Override
    public <T> Maybe<Record<T>> execute(RxCache rxCache, String key, Maybe<T> source, Type type) {

        Maybe<Record<T>> cache = rxCache.<T>load2Maybe(key, type);

        Maybe<Record<T>> remote = source
                .map(new Function<T, Record<T>>() {
                    @Override
                    public Record<T> apply(@NonNull T t) throws Exception {

                        rxCache.save(key, t);

                        return new Record<>(Source.CLOUD, key, t);
                    }
                });

        return Maybe.concatDelayError(Arrays.asList(cache,remote))
                .filter(new Predicate<Record<T>>() {
                    @Override
                    public boolean test(@NonNull Record<T> record) throws Exception {
                        return record.getData() != null;
                    }
                })
                .firstElement();
    }

    @Override
    public <T> Observable<Record<T>> execute(RxCache rxCache, String key, Observable<T> source, Type type) {

        Observable<Record<T>> cache = rxCache.<T>load2Observable(key, type);

        Observable<Record<T>> remote = source
                .map(new Function<T, Record<T>>() {
                    @Override
                    public Record<T> apply(@NonNull T t) throws Exception {

                        rxCache.save(key, t);

                        return new Record<>(Source.CLOUD, key, t);
                    }
                });

        return Observable.concatDelayError(Arrays.asList(cache, remote))
                .filter(new Predicate<Record<T>>() {
                    @Override
                    public boolean test(@NonNull Record<T> record) throws Exception {
                        return record.getData() != null;
                    }
                });
    }
}
