package kherb64.android.ipscscorer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import kherb64.android.ipscscorer.data.ScoreContract;

/**
 * Alert dialog for input of number of target types
 */
public class BuildTargetsDialogFragment extends DialogFragment {


    /* The activity that creates an instance of this dialog fragment must
             * implement this interface in order to receive event callbacks.
             * Each method passes the DialogFragment in case the host needs to query it. */
    public interface BuildTargetsDialogListener {
        void onDialogPositiveClick(DialogFragment dialog, int steelCount, int paperCount);
        void onDialogNegativeClick(DialogFragment dialog);
        int numTargetsPrefs (String targetType);
    }

    private static final boolean USE_FIX = false;
    BuildTargetsDialogListener mListener;
    private View mRootView;
    private int mSteelCount;
    private int mPaperCount;
    EditText mSteelView;
    EditText mPaperView;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (USE_FIX) {
            builder.setTitle("Build targets")
                    .setMessage("burli")
                    .setPositiveButton("Ok", new OnPositiveClickListener())
                    .setNegativeButton("Cancel", new OnNegativeClickListener());
        } else {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            mRootView = inflater.inflate(R.layout.build_targets_fragment, null);
            mSteelView = (EditText) mRootView.findViewById(R.id.build_targets_steel);
            mPaperView = (EditText) mRootView.findViewById(R.id.build_targets_paper);
            builder.setTitle("Build targets")
                    .setView(mRootView)
                    .setPositiveButton("Ok", new OnPositiveClickListener())
                    .setNegativeButton("Cancel", new OnNegativeClickListener());
        }
        AlertDialog dialog = builder.create();

        mSteelView.setText(Integer.toString(mSteelCount));
        mPaperView.setText(Integer.toString(mPaperCount));

        return dialog;
    }

    private final class OnPositiveClickListener
        implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            try {
                mSteelCount = Integer.parseInt(mSteelView.getText().toString());
                mPaperCount = Integer.parseInt(mPaperView.getText().toString());
            } catch (Exception e) {
                // nothing
            }
            mSteelCount = Math.min(mSteelCount, MainActivity.MAX_NUM_TARGETS);
            mPaperCount = Math.min(mPaperCount, MainActivity.MAX_NUM_TARGETS);
            mListener.onDialogPositiveClick(BuildTargetsDialogFragment.this,
                    mSteelCount, mPaperCount);
        }
    }

    private final class OnNegativeClickListener
            implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            mListener.onDialogNegativeClick(BuildTargetsDialogFragment.this);
        }
    }

    // Override the Fragment.onAttach() method to instantiate the BuildTargetsDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the BuildTargetsDialogListener so we can send events to the host
            mListener = (BuildTargetsDialogListener) activity;
            mSteelCount = mListener.numTargetsPrefs(ScoreContract.TargetEntry.TARGET_TYPE_STEEL);
            mPaperCount = mListener.numTargetsPrefs(ScoreContract.TargetEntry.TARGET_TYPE_PAPER);
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement BuildTargetsDialogListener");
        }
    }


}
