package org.commcare.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.commcare.dalvik.R;
import org.commcare.suite.model.DisplayData;
import org.commcare.suite.model.DisplayUnit;
import org.commcare.utils.MediaUtil;
import org.commcare.views.media.AudioButton;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.services.locale.Localizer;

import java.io.File;

/**
 * This layout for the GenericMenuFormAdapter allows you to load an image, audio, and text
 * to menus.
 *
 * @author wspride
 */

public class HorizontalMediaView extends RelativeLayout {
    private static final String t = "AVTLayout";

    private TextView mTextView;
    private AudioButton mAudioButton;
    private ImageView mImageView;
    private final EvaluationContext ec;
    private final int iconDimension;

    public static final int NAVIGATION_NONE = 0;
    public static final int NAVIGATION_NEXT = 1;


    public HorizontalMediaView(Context c) {
        this(c, null);
    }

    private HorizontalMediaView(Context c, EvaluationContext ec) {
        super(c);
        mTextView = null;
        mAudioButton = null;
        mImageView = null;
        this.ec = ec;
        this.iconDimension = (int)getResources().getDimension(R.dimen.menu_icon_size);
    }

    public void setDisplay(DisplayUnit display) {
        DisplayData mData = display.evaluate(ec);
        setAVT(Localizer.processArguments(mData.getName(), new String[]{""}).trim(), mData.getAudioURI(), mData.getImageURI());
    }

    public void setAVT(String displayText, String audioURI, String imageURI) {
        this.setAVT(displayText, audioURI, imageURI, NAVIGATION_NONE);
    }

    private void setAVT(String displayText, String audioURI, String imageURI, int navStyle) {
        this.removeAllViews();

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mTextView = (TextView)inflater.inflate(R.layout.menu_list_item, null);
        mTextView.setText(displayText);

        // Layout configurations for our elements in the relative layout
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams audioParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(iconDimension, iconDimension);

        RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        String audioFilename = "";
        if (audioURI != null && !audioURI.equals("")) {
            try {
                audioFilename = ReferenceManager._().DeriveReference(audioURI).getLocalURI();
            } catch (InvalidReferenceException e) {
                Log.e(t, "Invalid reference exception");
                e.printStackTrace();
            }
        }


        if (navStyle != NAVIGATION_NONE) {
            ImageView iconRight = new ImageView(this.getContext());
            if (navStyle == NAVIGATION_NEXT) {
                iconRight.setImageResource(R.drawable.icon_next);
            } else {
                iconRight.setImageResource(R.drawable.icon_done);
            }
            iconRight.setId(2345345);
            iconParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            iconParams.addRule(CENTER_VERTICAL);

            iconParams.rightMargin = 5;
            //iconRight.setPadding(20, iconRight.getPaddingTop(), iconRight.getPaddingRight(), iconRight.getPaddingBottom());
            this.addView(iconRight, iconParams);
        }

        File audioFile = new File(audioFilename);

        // First set up the audio button
        if (!"".equals(audioFilename) && audioFile.exists()) {
            // An audio file is specified
            mAudioButton = new AudioButton(getContext(), audioURI, true);
            mAudioButton.setId(3245345); // random ID to be used by the relative layout.
            // Set not focusable so that list onclick will work
            mAudioButton.setFocusable(false);
            mAudioButton.setFocusableInTouchMode(false);
            if (navStyle != NAVIGATION_NONE) {
                audioParams.addRule(RelativeLayout.LEFT_OF, 2345345);
            } else {
                audioParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            }
            audioParams.addRule(CENTER_VERTICAL);
            addView(mAudioButton, audioParams);
        }

        Bitmap b = MediaUtil.inflateDisplayImage(getContext(), imageURI, iconDimension, iconDimension);
        if (b != null) {
            mImageView = new ImageView(getContext());
            mImageView.setPadding(10, 10, 10, 10);
            mImageView.setAdjustViewBounds(true);
            mImageView.setImageBitmap(b);
            mImageView.setId(23422634);
            imageParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            audioParams.addRule(CENTER_VERTICAL);
            addView(mImageView, imageParams);
        }

        textParams.addRule(RelativeLayout.CENTER_VERTICAL);
        textParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        if (imageURI != null && !imageURI.equals("") && mImageView != null) {
            textParams.addRule(RelativeLayout.RIGHT_OF, mImageView.getId());
        } else {
            textParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }

        if (navStyle != NAVIGATION_NONE) {
            textParams.addRule(RelativeLayout.LEFT_OF, 2345345);
        } else if (mAudioButton != null) {
            textParams.addRule(RelativeLayout.LEFT_OF, mAudioButton.getId());
        }
        addView(mTextView, textParams);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility != View.VISIBLE) {
            if (mAudioButton != null) {
                mAudioButton.endPlaying();
            }
        }
    }
}