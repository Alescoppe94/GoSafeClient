package com.example.alessandro.gosafe.helpers;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ImageLoader {

    public ImageLoader(){

    }

    public static Bitmap loadImageFromStorage(String numpiano, Context ctx)
    {
        Bitmap immagine;
        try {
            ContextWrapper cw = new ContextWrapper(ctx);
            // gets the files in the directory
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File f=new File(directory, "q"+numpiano+".png");
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.RGB_565;
            opt.inPurgeable=true;
            immagine = BitmapFactory.decodeStream(new FileInputStream(f), new Rect(), opt);


        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
            Runtime.getRuntime().gc();
            return null;
        }
        catch(FileNotFoundException n){
            return null;
        }

        return immagine;

    }

}
