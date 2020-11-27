package org.techtown.blackbox;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

class MyGalleryAdapter extends BaseAdapter {
    Context context;
    int layout;
    String [] videoTitle;
    LayoutInflater inf;
    private ArrayList<Bitmap> bmThumbnail;

    public MyGalleryAdapter(Context context, int layout, String[] videoTitle) {

        this.context = context;
        this.layout = layout;
        this.videoTitle = videoTitle;
        inf = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() { // 보여줄 데이터의 총 개수 - 꼭 작성해야 함
        return videoTitle.length;
    }

    @Override
    public Object getItem(int position) { // 해당행의 데이터- 안해도 됨
        return null;
    }

    @Override
    public long getItemId(int position) { // 해당행의 유니크한 id - 안해도 됨
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 보여줄 해당행의 row xml 파일의 데이터를 셋팅해서 뷰를 완성하는 작업
        if (convertView == null) {
            convertView = inf.inflate(layout, null);
        }


        Bitmap bmThumbnail;
        bmThumbnail = ThumbnailUtils.createVideoThumbnail(context.getFilesDir().getAbsolutePath() + "/blackbox/" + videoTitle[position],
                MediaStore.Images.Thumbnails.MINI_KIND);
        if(bmThumbnail !=null) {
            bmThumbnail = Bitmap.createBitmap(bmThumbnail, 0, bmThumbnail.getHeight() / 5, bmThumbnail.getWidth(), bmThumbnail.getHeight() * 3 / 5);
        }
        ImageView imageThumbnail = (ImageView)convertView.findViewById(R.id.gallery_imageView);


        TextView textView = convertView.findViewById(R.id.galleryTextView);
        imageThumbnail.setImageBitmap(bmThumbnail);
        if(videoTitle[position] != null) {
            String[] title = videoTitle[position].split("_");
            textView.setText(title[1] + "_" + title[2]);
        }else{
            textView.setText("잘못된 형식");
        }


        return convertView;
    }



}

