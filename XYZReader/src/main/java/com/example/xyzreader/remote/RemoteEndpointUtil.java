package com.example.xyzreader.remote;

import android.database.Cursor;
import android.util.Log;

import com.example.xyzreader.data.ArticleDetails;
import com.example.xyzreader.data.ArticleLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RemoteEndpointUtil {
    private static final String TAG = "RemoteEndpointUtil";

    private RemoteEndpointUtil() {
    }

    public static JSONArray fetchJsonArray() {
        String itemsJson;
        try {
            itemsJson = fetchPlainText();
        } catch (IOException e) {
            Log.e(TAG, "Error fetching items JSON", e);
            return null;
        }

        // Parse JSON
        try {
            JSONTokener tokener = new JSONTokener(itemsJson);
            Object val = tokener.nextValue();
            if (!(val instanceof JSONArray)) {
                throw new JSONException("Expected JSONArray");
            }
            for (int i = 0; i < ((JSONArray) val).length(); i++) {
                JSONObject object = ((JSONArray) val).getJSONObject(i);
                String bodyText = ((JSONArray) val).getJSONObject(i).getString("body")
                        .replace("\r\n\r\n", "\n").replace("\r\n", "");
                object.put("body", bodyText);

            }
            JSONObject object = ((JSONArray) val).getJSONObject(0);
            Log.i(TAG, "fetchJsonArray: first id = " + object.getInt("id"));

            return (JSONArray) val;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing items JSON", e);
        }

        return null;
    }

    private static String fetchPlainText() throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(Config.BASE_URL)
                .build();

        Response response = client.newCall(request).execute();
        assert response.body() != null;
        return response.body().string();
    }

    public static ArticleDetails createInfoFromCursor(Cursor cursor) {

        ArticleDetails articleDetails = new ArticleDetails();
        articleDetails.id = cursor.getLong(ArticleLoader.Query._ID);
        articleDetails.serverId = cursor.getString(ArticleLoader.Query.SERVER_ID);
        articleDetails.title = cursor.getString(ArticleLoader.Query.TITLE);
        articleDetails.author = cursor.getString(ArticleLoader.Query.AUTHOR);
        articleDetails.body = null;
        articleDetails.thumbUrl = cursor.getString(ArticleLoader.Query.THUMB_URL);
        articleDetails.photoUrl = cursor.getString(ArticleLoader.Query.PHOTO_URL);
        articleDetails.aspectRatio = cursor.getLong(ArticleLoader.Query.ASPECT_RATIO);
        articleDetails.publishedDate = cursor.getString(ArticleLoader.Query.PUBLISHED_DATE);

        return articleDetails;
    }
    public static ArticleDetails createInfoAndBodyFromCursor(Cursor cursor) {

        ArticleDetails articleDetails = new ArticleDetails();
        articleDetails.id = cursor.getLong(ArticleLoader.Query._ID);
        articleDetails.serverId = cursor.getString(ArticleLoader.Query.SERVER_ID);
        articleDetails.title = cursor.getString(ArticleLoader.Query.TITLE);
        articleDetails.author = cursor.getString(ArticleLoader.Query.AUTHOR);
        articleDetails.body = cursor.getString(ArticleLoader.Query.BODY);
        articleDetails.thumbUrl = cursor.getString(ArticleLoader.Query.THUMB_URL);
        articleDetails.photoUrl = cursor.getString(ArticleLoader.Query.PHOTO_URL);
        articleDetails.aspectRatio = cursor.getLong(ArticleLoader.Query.ASPECT_RATIO);
        articleDetails.publishedDate = cursor.getString(ArticleLoader.Query.PUBLISHED_DATE);

        return articleDetails;
    }

    public static ArrayList<ArticleDetails> createArticleListFromCursor(Cursor cursor) {
        ArrayList<ArticleDetails> info = new ArrayList<>();
        while(cursor.moveToNext()) {
            info.add(createInfoFromCursor(cursor));
        }
        return info;
    }
}
