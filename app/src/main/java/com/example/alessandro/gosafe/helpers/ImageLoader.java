package com.example.alessandro.gosafe.helpers;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Alessandro on 09/06/2018.
 */

public class ImageLoader {

    public ImageLoader(){

    }

    public static Bitmap loadImageFromStorage(String numpiano, Context ctx)
    {
        Bitmap b;
        try {
            ContextWrapper cw = new ContextWrapper(ctx);
            // gets the files in the directory
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            // lists all the files into an array
            //File[] dirFiles = directory.listFiles();

            //if (dirFiles.length != 0) {
            // loops through the array of files, outputing the name to console
            //  for (int i = 0; i < dirFiles.length; i++) {
            File f=new File(directory, "q"+numpiano+".png");
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.RGB_565;
            opt.inPurgeable=true;
            b = BitmapFactory.decodeStream(new FileInputStream(f), new Rect(), opt);


        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
            b= null;
            Runtime.getRuntime().gc();
            return null;
        }
        catch(FileNotFoundException n){
            return null;
        }

        return b;

    }

}
