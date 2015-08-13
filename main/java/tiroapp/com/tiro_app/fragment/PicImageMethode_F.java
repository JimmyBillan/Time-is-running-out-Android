package tiroapp.com.tiro_app.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import tiroapp.com.tiro_app.R;
import tiroapp.com.tiro_app.interfaces.DialogClickListenerImagePicker;

public class PicImageMethode_F extends DialogFragment {



    public DialogClickListenerImagePicker callback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            callback = (DialogClickListenerImagePicker) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement DialogClickListener interface");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Take a pics from : ")
                .setItems(R.array.imagePicker , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            callback.onGalleryClick();
                        }
                        else if(which == 1){
                            callback.onCameraClick();
                        }
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }


}

