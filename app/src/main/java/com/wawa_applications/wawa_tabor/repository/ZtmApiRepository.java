package com.wawa_applications.wawa_tabor.repository;

import com.wawa_applications.wawa_tabor.model.ApiResult;
import com.wawa_applications.wawa_tabor.network.ZtmApiClient;

import io.reactivex.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ZtmApiRepository {

    private ZtmApiClient apiClient;
    private final String API_KEY = "";

    public ZtmApiRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ZtmApiClient.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        apiClient = retrofit.create(ZtmApiClient.class);
    }

    public Observable<ApiResult> getBuses(String lineNo) {
        return apiClient.getBuses(API_KEY, lineNo);
    }

    public Observable<ApiResult> getTrams(String lineNo) {
        return apiClient.getTrams(API_KEY, lineNo);
    }
}
