package top.codecafe.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.kymjs.api.Api;
import com.kymjs.frame.adapter.BasePullUpRecyclerAdapter;
import com.kymjs.frame.adapter.RecyclerHolder;
import com.kymjs.kjcore.Core;
import com.kymjs.kjcore.http.KJHttp;
import com.kymjs.kjcore.http.Request;
import com.kymjs.model.osc.News;
import com.kymjs.model.osc.NewsList;

import java.util.ArrayList;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.codecafe.R;
import top.codecafe.utils.XmlUtils;

/**
 * OSC新闻资讯
 *
 * @author kymjs (http://www.kymjs.com/) on 12/11/15.
 */
public class OSCNewsListFragment extends MainListFragment<News> {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Observable.just(KJHttp.getCache(Api.OSC_NEWS))
                .filter(new Func1<byte[], Boolean>() {
                    @Override
                    public Boolean call(byte[] cache) {
                        return cache != null && cache.length != 0;
                    }
                })
                .map(new Func1<byte[], ArrayList<News>>() {
                    @Override
                    public ArrayList<News> call(byte[] bytes) {
                        return parserInAsync(bytes);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<News>>() {
                    @Override
                    public void call(ArrayList<News> blogs) {
                        datas = blogs;
                        adapter.refresh(datas);
                        viewDelegate.mEmptyLayout.dismiss();
                    }
                });
    }

    @Override
    protected BasePullUpRecyclerAdapter<News> getAdapter() {
        return new BasePullUpRecyclerAdapter<News>(recyclerView, datas, R.layout.item_blog) {
            @Override
            public void convert(RecyclerHolder holder, News item, int position, boolean
                    isScrolling) {
                holder.setText(R.id.item_blog_tv_title, item.getTitle());
                holder.setText(R.id.item_blog_tv_description, item.getBody().trim());
                holder.setText(R.id.item_blog_tv_author, item.getAuthor());
                holder.setText(R.id.item_blog_tv_date, item.getPubDate());

                holder.getView(R.id.item_blog_tip_recommend).setVisibility(View.GONE);
                holder.getView(R.id.item_blog_img).setVisibility(View.GONE);
            }
        };
    }

    @Override
    protected String getChangePageTitleAction() {
        return "";
    }

    @Override
    protected ArrayList<News> parserInAsync(byte[] t) {
        return XmlUtils.toBean(NewsList.class, t).getList();
    }

    @Override
    public void doRequest() {
        new Core.Builder().url(Api.OSC_NEWS)
                .contentType(Request.HttpMethod.GET)
                .cacheTime(100)
                .callback(callBack)
                .doTask();
    }

    @Override
    public void onItemClick(View view, Object data, int position) {

    }
}