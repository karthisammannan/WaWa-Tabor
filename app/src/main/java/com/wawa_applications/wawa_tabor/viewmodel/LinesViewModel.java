package com.wawa_applications.wawa_tabor.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.wawa_applications.wawa_tabor.model.ApiResult;
import com.wawa_applications.wawa_tabor.model.Line;
import com.wawa_applications.wawa_tabor.repository.ZtmApiRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class LinesViewModel extends ViewModel {

    private MutableLiveData<String> lineNoLiveData;
    private MutableLiveData<List<Line>> lineListLiveData;
    private MutableLiveData<Boolean> isResult;
    private CompositeDisposable compositeDisposable;
    private Disposable currentDisposable;
    private ZtmApiRepository repository;

    public LinesViewModel() {
        compositeDisposable = new CompositeDisposable();
        repository = new ZtmApiRepository();
        isResult = new MutableLiveData<>(true);
    }

    public MutableLiveData<String> getLineNoLiveData() {
        checkIfLineNoIsInitiated();
        return lineNoLiveData;
    }

    public LiveData<List<Line>> getLineListLiveData(){
        checkIfLineInfoListIsInitiated();
        return lineListLiveData;
    }

    public MutableLiveData<Boolean> getIsResult() {
        return isResult;
    }

    public int indicateLineType(String lineInput) {
        String busPattern ="\\d{3}|N\\d{2}";
        String tramPattern = "\\d{2}";

        lineInput = lineInput.toUpperCase();

        if(lineInput.matches(busPattern)) {
            return 1;
        } else if (lineInput.matches(tramPattern)){
            return 2;
        } else {
            return 0;
        }
    }

    public void subscribeBus(String line, int lineType) {

        setLineNoLiveData(line);

        currentDisposable = Observable.interval(5, TimeUnit.SECONDS)
                .flatMap(n -> getLines(line, lineType))
                .doOnError(error -> Log.d("Error: ", error.getMessage()))
                .subscribe(this::handleResult);

        compositeDisposable.add(currentDisposable);
    }

    public void unSubscribeBus() {
        compositeDisposable.remove(currentDisposable);
    }

    private Observable<ApiResult> getLines(String line, int lineType) {
        if (lineType == 1){
            return repository.getBuses(line);
        } else {
            return repository.getTrams(line);
        }
    }

    private void handleResult(ApiResult apiResult){
        checkIfLineInfoListIsInitiated();
        if (apiResult.getLinesList().size() > 0){
            isResult.postValue(true);
            lineListLiveData.postValue(apiResult.getLinesList());
        } else {
            isResult.postValue(false);
            compositeDisposable.remove(currentDisposable);
        }
    }

    private void setLineNoLiveData(String line) {
        checkIfLineNoIsInitiated();
        if (lineNoLiveData.getValue() != null && !lineNoLiveData.equals(line)) {
            Log.i("Unsubscribe", "Unsubscribe line: " + lineNoLiveData.getValue());
            compositeDisposable.remove(currentDisposable);
        }
        lineNoLiveData.setValue(line);
    }

    private void checkIfLineNoIsInitiated() {
        if (lineNoLiveData == null) {
            lineNoLiveData = new MutableLiveData<>();
        }
    }

    private void checkIfLineInfoListIsInitiated() {
        if (lineListLiveData == null) {
            lineListLiveData = new MutableLiveData<>();
            lineListLiveData.setValue(new ArrayList<>());
        }
    }
}
