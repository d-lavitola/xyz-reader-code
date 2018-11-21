package com.example.xyzreader.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleDetails;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.remote.RemoteEndpointUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends android.support.v4.app.Fragment implements
        RequestListener<Bitmap>, android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    private static final String ARG_ARTICLE_ITEM = "article_details";
    private static final float PARALLAX_FACTOR = .75f;


    public ArticleDetails mArticleDetails;

    private boolean photoTransitionReady = false;

    android.support.v7.widget.Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbarLayout;
    private Cursor mCursor;
    private View mRootView;
    private TextView titleView;
    private TwoThreeImageView mPhotoView;
    private String mArticleByline;
    private int mMutedColor;
    private int mDarkMutedColor;
    private TextView mBodyTextView;
    private String imageTransitionName;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static android.support.v4.app.Fragment newInstance(ArticleDetails articleDetails, String imageTransitionName) {
        Bundle arguments = new Bundle();
        arguments.putString(ArticleListActivity.EXTRA_IMAGE_TRANSITION_NAME, imageTransitionName);
        arguments.putParcelable(ARG_ARTICLE_ITEM, articleDetails);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null ) {
            imageTransitionName = getArguments()
                    .getString(ArticleListActivity.EXTRA_IMAGE_TRANSITION_NAME);
            if (savedInstanceState != null) {
                mArticleDetails = savedInstanceState.getParcelable(ARG_ARTICLE_ITEM);
            } else {
                mArticleDetails = getArguments().getParcelable(ARG_ARTICLE_ITEM);
            }

        }
        mMutedColor = ContextCompat.getColor(getActivityCast(), R.color.theme_primary);
        mDarkMutedColor = ContextCompat.getColor(getActivityCast(), R.color.theme_primary_dark);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(
                        ShareCompat.IntentBuilder.from(Objects.requireNonNull(getActivity()))
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });
        toolbar = mRootView.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivityCast().onBackPressed();
            }
        });

        collapsingToolbarLayout = mRootView.findViewById(R.id.collapsing_toolbar_layout);


//      mStatusBarColorDrawable = new ColorDrawable(0);

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivityCast())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });
        bindViews();
        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    private void updateStatusBar() {
//        toolbar.;
    }

    private Date parsePublishedDate() {
        try {
            String date = mArticleDetails.publishedDate;
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        titleView = mRootView.findViewById(R.id.article_title);
        TextView bylineView = mRootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        mPhotoView = mRootView.findViewById((R.id.detail_photo));
        mBodyTextView = mRootView.findViewById(R.id.article_body_textview);


        final TextView toolbarTitle = mRootView.findViewById(R.id.toolbar_title);
        AppBarLayout appBarLayout = mRootView.findViewById(R.id.appbar_layout);

        if (mArticleDetails != null) {
            mRootView.setAlpha(0);
            if (mArticleDetails.photoUrl != null & mPhotoView != null) {
                Log.i(TAG, "bindViews: photoUrl = " + mArticleDetails.photoUrl);
                loadImage();
            }
            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (Math.abs(verticalOffset)-appBarLayout.getTotalScrollRange() == 0)
                    {
                        // Collapsed
                        toolbarTitle.setText(mArticleDetails.title);
                    }
                    else
                    {
                        // Expanded
                        toolbarTitle.setText("");
                    }
                }
            });
            titleView.setText(mArticleDetails.title);
            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                mArticleByline = String.valueOf(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + " by <font color='#ffffff'>"
                                + mArticleDetails.author
                                + "</font>"));
                bylineView.setText(mArticleByline);

            } else {
                mArticleByline = String.valueOf(Html.fromHtml(
                        outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                                + mArticleDetails.author
                                + "</font>"));
                // If date is before 1902, just show the string
                bylineView.setText(mArticleByline);

            }
            if (mArticleDetails.body != null) {
                mBodyTextView.setText(mArticleDetails.body);
            }
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1).setDuration(600);

        } else {

            mRootView.setVisibility(View.VISIBLE);
            titleView.setText("N/A");
            bylineView.setText("N/A" );
        }
    }

    private void loadImage() {
        Glide.with(this).asBitmap()
                .load(mArticleDetails.photoUrl)
                .addListener(this)
                .into(mPhotoView);
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(ARG_ARTICLE_ITEM, mArticleDetails);
        super.onSaveInstanceState(outState);
    }


    public void createPaletteAsync(Bitmap bitmap, final Boolean updateStatusBar) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette p) {
                mMutedColor = p.getMutedColor(mMutedColor);
                mDarkMutedColor = p.getDarkMutedColor(mDarkMutedColor);
                updateStatusBar();
                if (updateStatusBar) {
                    getActivityCast().onPaletteLoaded(mDarkMutedColor);
                }
            }
        });
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return ArticleLoader.newInstanceForItemId(getContext(), mArticleDetails.id);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onLoadFinished(@NonNull android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (mCursor == null) {
            mCursor = data;
            mCursor.moveToFirst();
            Log.i(TAG, "onLoadFinished: title = " + mCursor.getString(ArticleLoader.Query.TITLE));
            mArticleDetails = RemoteEndpointUtil.createInfoAndBodyFromCursor(mCursor);
            mCursor.close();
            Log.i(TAG, "onLoadFinished: cursor closed id=" + mArticleDetails.id);
        }
        mBodyTextView.setText(mArticleDetails.body);
    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<Cursor> loader) {
    }

    @Override
    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
        getActivityCast().onImageLoad(mPhotoView);
        return false;
    }

    @Override
    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
        boolean setStatusBar = false;
        if (imageTransitionName.equals(mArticleDetails.title)) {
            mPhotoView.setTransitionName(mArticleDetails.title);
            titleView.setTransitionName(mArticleDetails.title + "_Title");
            getActivityCast().onImageLoad(mPhotoView);
            setStatusBar = true;
        }
        createPaletteAsync(resource, setStatusBar);
        return false;
    }

    public interface DetailFragmentLoadListener {
        void onImageLoad(final TwoThreeImageView sharedView);
        void onBodyLoaded(int id);
        void onPaletteLoaded(int darkMutedColor);
    }


}
