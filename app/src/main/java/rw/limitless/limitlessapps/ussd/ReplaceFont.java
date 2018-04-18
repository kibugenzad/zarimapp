package rw.limitless.limitlessapps.ussd;

import android.content.Context;
import android.graphics.Typeface;

import java.lang.reflect.Field;

/**
 * Created by limit on 1/20/2017.
 */

public class ReplaceFont {
    public static void replaceDefaultFont(Context context, String nameOfFontBeingReplaced, String nameOfFontAsset){
        Typeface customFont = Typeface.createFromAsset(context.getAssets(), nameOfFontAsset);
        replaceFont(nameOfFontBeingReplaced, customFont);
    }

    private static void replaceFont(String nameOfFontBeingReplaced, Typeface customFont) {
        try {
            Field myField = Typeface.class.getDeclaredField(nameOfFontBeingReplaced);
            myField.setAccessible(true);
            myField.set(null, customFont);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
