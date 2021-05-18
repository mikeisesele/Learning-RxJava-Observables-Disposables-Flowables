package com.decagon.learningrxjava

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/***

Assignment: Create an observable using flowable and disposable

 ***/


class MainActivity : AppCompatActivity(){
    lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

/*

        Observable without flowable
        ---------------------------------------------------------
        Observable.just(getCount())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                println("$result")
            })

//            this is not efficient as it can overload the subscriber when
//            the emitter emits data much more than the emitter can handle
*/


        // Observable with flowable. without disposable

        // the yellow lines indicate that the data received at the subscriber has not been worked on and still is in memory.
        // so it treats it as potentially unsafe.
//        ------------------------------------------------------------------------
        Observable.just(getCount()).toFlowable(BackpressureStrategy.DROP)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                println("$result")
            })

//            flowable helps filter data and ensure the subscriber can cope with incoming stream of data but as long as memory is not cleared after the operation,
//              this is not the most efficient approach.



        // Observable with Flowable and Disposable
//        -----------------------------------------------------------------------
        Observable.just(getCount())
            .toFlowable(BackpressureStrategy.DROP)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { result ->
                println("$result")
            }.dispose()

            // network call is done on schedulers.io hence this would not bring any result.
            // because  because since it changed threads the main thread called for disposal,
            // thereby erasing the work it was performing on the io thread.



        // observable with compositeDisposable
        // implementing appropriate disposable which clears the memory
        // after the data has been passed to the main thread from the io thread

        //-----------------------------------------------------------------------



        val observedResult = Observable.just(getCount())
            .toFlowable(BackpressureStrategy.DROP)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { result -> println("$result") }

        compositeDisposable = CompositeDisposable()

        compositeDisposable.add(observedResult)
        // this returns the result to the main thread and clears memory after the operation is done (best done in on destroy)
    }

    // mock function to get data
    private fun getCount(){
        // get some form of data stream from the web
    }

    override fun onDestroy(){
        super.onDestroy()

        compositeDisposable.dispose()
    }
}



