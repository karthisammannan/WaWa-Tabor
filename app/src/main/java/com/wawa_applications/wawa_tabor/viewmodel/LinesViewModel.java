package com.wawa_applications.wawa_tabor.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.wawa_applications.wawa_tabor.model.ApiResult;
import com.wawa_applications.wawa_tabor.network.retrofit.ZTMAPIService;
import com.wawa_applications.wawa_tabor.model.LineInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class LinesViewModel extends ViewModel {

    private MutableLiveData<String> lineNo;
    private MutableLiveData<List<LineInfo>> transportList;
    private ZTMAPIService ztmService;
    CompositeDisposable compositeDisposable;

    public LinesViewModel() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ZTMAPIService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        ztmService = retrofit.create(ZTMAPIService.class);

        compositeDisposable = new CompositeDisposable();
    }

    public MutableLiveData<String> getLineNo() {

        if (lineNo == null){
            lineNo = new MutableLiveData<String>();
        }
        return lineNo;
    }

    public LiveData<List<LineInfo>> getTransportList(){

        if (transportList == null){
            createMutableLiveData();
        }
        return transportList;
    }

    public void subscribeBus(String line) {

        if (lineNo == null){
            lineNo = new MutableLiveData<String>();
            lineNo.setValue(line);
        }

        Disposable disposable = Observable.interval(15, TimeUnit.SECONDS)
                .flatMap(n -> ztmService.getBuses(line))
                .doOnError(error -> Log.d("Coś się stało w RxJava", error.getMessage()))
                .subscribe(ztmapiResult -> handleResult(ztmapiResult));

        compositeDisposable.add(disposable);
    }

    public void unSubscribeBus() {
        compositeDisposable.dispose();
    }

    private void handleResult(ApiResult apiResult){
        if (transportList == null){
            createMutableLiveData();
        }
        transportList.postValue(apiResult.getLinesList());
    }

    private void createMutableLiveData() {
        transportList = new MutableLiveData<>();
        transportList.setValue(new ArrayList<LineInfo>());
    }
}
