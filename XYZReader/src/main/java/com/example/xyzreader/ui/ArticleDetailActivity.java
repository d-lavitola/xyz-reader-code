package com.example.xyzreader.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleDetails;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.remote.RemoteEndpointUtil;

import java.util.ArrayList;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity implements
        ArticleDetailFragment.DetailFragmentLoadListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = ArticleDetailActivity.class.getName();
    public static final String SELECTED_ARTICLE_ID = "article_id";
    public static final String RETURNING_POSITION = "returning_recyclerview_position";

    ArrayList<ArticleDetails> articlesArrayList;
    private Cursor mCursor;
    long mStartId;
    String imageTransitionName;
    long mSelectedItemId;
    private Window window;
    private ViewPager mPager;
    private ArticlesArrayFragmentPagerAdapter mPagerAdapter;


    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null && extras != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                imageTransitionName = extras.getString(ArticleListActivity.EXTRA_IMAGE_TRANSITION_NAME);
                Log.i(LOG_TAG, "onCreate: start id = " + mStartId);
                mSelectedItemId = mStartId;
            }
        }

        setContentView(R.layout.activity_article_detail);
        setStatusBar(R.color.theme_primary_dark);

        if (imageTransitionName != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
        }


        mPager = findViewById(R.id.pager);

        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));
        //        mPager.setOffscreenPageLimit();

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                }
                assert mCursor != null;
                mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);
            }
        });

        getSupportLoaderManager().initLoader(0, null, this);

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putLong(SELECTED_ARTICLE_ID, mSelectedItemId);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(@NonNull android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        articlesArrayList = RemoteEndpointUtil.createArticleListFromCursor(mCursor);
        mPagerAdapter = new ArticlesArrayFragmentPagerAdapter(this, articlesArrayList, imageTransitionName);
        mPager.setAdapter(mPagerAdapter);

        Log.i(LOG_TAG, "onLoadFinished: Detail Activity Cursor Loader");

        // Select the start ID
        if (mStartId > 0) {
            for (int i = 0; i < articlesArrayList.size(); i++) {
                if (articlesArrayList.get(i).id == mStartId) {
                    mPager.setCurrentItem(i, false);
                    break;
                }
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<Cursor> loader) {
        mCursor = null;
    }

    @Override
    public void onImageLoad(final TwoThreeImageView sharedView) {
        Log.i(LOG_TAG, "onImageLoad: shared image loaded");
        scheduleStartPostponedTransition(sharedView);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
//        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onBodyLoaded(int id) {
        for (int i = 0; i < articlesArrayList.size(); i++) {
            if (articlesArrayList.get(i).id == id) {
                mPagerAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onPaletteLoaded(int darkMutedColor) {
        setStatusBar(darkMutedColor);
    }

    private void setStatusBar(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            float lightness = Lightness(color);
            if (lightness >= 0.5) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else if (lightness < 0.5) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    private void scheduleStartPostponedTransition(final View sharedElement) {
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        startPostponedEnterTransition();
                        return true;
                    }
                });
    }

    public float Lightness(int color){
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        float hsl[] = new float[3];
        ColorUtils.RGBToHSL(red, green, blue, hsl);
        return hsl[2];
    }
}
