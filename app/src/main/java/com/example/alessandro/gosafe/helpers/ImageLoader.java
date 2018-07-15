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
 * Classe statica che si occupa del caricamento delle immagini
 */
public class ImageLoader {

    /**
     * Costruttore
     */
    public ImageLoader(){

    }

    /**
     * Metodo statico che carica le immagini e le trasforma in bitmap pronti per essere visualizzati dalla gui
     * @param numpiano numero del piano di cui estrarre l'immagine
     * @param ctx Context necessario per la connessione al db
     * @return ritorna il Bitmap con l'immagine caricata
     */
    public static Bitmap loadImageFromStorage(String numpiano, Context ctx)
    {
        Bitmap immagine;
        try {
            ContextWrapper cw = new ContextWrapper(ctx);
            //prende i file nella directory
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File f=new File(directory, "q"+numpiano+".png");  //i file immagini sono salvati nella memoria interna del telefono
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.RGB_565; //scelto questo fromato perchè occupa meno memoria
            opt.inPurgeable=true;
            immagine = BitmapFactory.decodeStream(new FileInputStream(f), new Rect(), opt); //crea il bitmap


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
