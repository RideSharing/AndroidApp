package com.halley.vehicle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.halley.app.AppConfig;
import com.halley.app.AppController;
import com.halley.custom_theme.CustomActionBar;
import com.halley.helper.SessionManager;
import com.halley.registerandlogin.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class VehicleActivity extends ActionBarActivity {
    private static final String TAG = VehicleActivity.class.getSimpleName();
    private CustomActionBar customActionBar;
    private ActionBar actionBar;
    private SweetAlertDialog pDialog;
    private int SELECT_FILE = 1, REQUEST_CAMERA = 2;
    private int whichKind = 0;
    private ImageView license_plate_img, vehicle_image, moto_insurance_img;
    private Activity activity;
    private String key;
    private SessionManager session;
    private TextView tvLicensePlate, tvTypeVehicle, tvRegCertificate;
    private String saveType, saveLicensePlate, saveCertificate, saveLicense_plate_img, saveVehicle_img, saveMotor_insurance_img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_vehicle);
        customActionBar = new CustomActionBar(this, actionBar, pDialog, 2);
        actionBar = customActionBar.getActionBar();
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setTitleText(getResources().getString(R.string.process_data));
        license_plate_img = (ImageView) findViewById(R.id.license_plate_image);
        vehicle_image = (ImageView) findViewById(R.id.vehicle_image);
        moto_insurance_img = (ImageView) findViewById(R.id.moto_insurance_img);
        tvTypeVehicle = (TextView) findViewById(R.id.tvTypeVehicle);
        tvLicensePlate = (TextView) findViewById(R.id.tvLicensePlate);
        tvRegCertificate = (TextView) findViewById(R.id.tvRegCertificate);
        session = new SessionManager(this);


    }

    public void editVehicle(View v) {
        editVehicleDialog();
    }


    public void registerVehicle(View v) {
        if (tvTypeVehicle.getText().toString().length() == 0 || tvLicensePlate.getText().toString().length() == 0
                || tvRegCertificate.getText().toString().length() == 0 || (license_plate_img.getDrawable() ==  null)
                || vehicle_image.getDrawable() == null || moto_insurance_img.getDrawable() == null) {
            Toast.makeText(getApplicationContext(), R.string.no_input, Toast.LENGTH_SHORT).show();
        } else {
            registerVehicle(saveType, saveLicensePlate, saveCertificate, saveLicense_plate_img, saveVehicle_img, saveMotor_insurance_img);
        }
    }

    public void registerVehicle(final String type, final String license_plate,final String reg_certificate,final String license_plate_img, final String vehicle_img, final String morto_insurance_img) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";


        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_GET_VEHICLE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Vehicle Response: " + response.toString());
                hidePDialog();

                try {

                    JSONObject jObj = new JSONObject(
                            response.substring(response.indexOf("{"),
                                    response.lastIndexOf("}") + 1));
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        String message = jObj.getString("message");

                        Toast.makeText(getApplicationContext(),
                                message,
                                Toast.LENGTH_SHORT).show();
                        Intent returnIntent=new Intent();
                        setResult(RESULT_OK,returnIntent);
                        finish();

                    } else {
                        // Error occurred in registration. Get the error
                        // message
                        String message = jObj.getString("message");
                        Toast.makeText(getApplicationContext(),
                                message, Toast.LENGTH_LONG).show();


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                key = session.getAPIKey();
                params.put("Authorization", key);

                return params;
            }

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("type", type);
                params.put("license_plate", license_plate);
                params.put("reg_certificate", reg_certificate);
                params.put("license_plate_img", license_plate_img);
                params.put("vehicle_img", vehicle_img);
                params.put("motor_insurance_img", morto_insurance_img);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public void btnClickEditLicensePlateImage(View view) {
        selectImage();
        whichKind = 1;
    }

    public void btnClickEditVehicleImage(View view) {
        selectImage();
        whichKind = 2;
    }

    public void btnClickMotoInsurance(View view) {
        selectImage();
        whichKind = 3;
    }

    public void zoomImageLicensePlate(View v) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        MyDialogFragment frag = new MyDialogFragment();
        whichKind = 1;
        Bundle b = new Bundle();
        b.putString("license_plate", saveLicense_plate_img);
        b.putInt("whichKind", whichKind);
        frag.setArguments(b);
        frag.show(ft, "txn_tag");

    }

    public void zoomImageVehicle(View v) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        MyDialogFragment frag = new MyDialogFragment();
        whichKind = 2;
        Bundle b = new Bundle();
        b.putString("vehicle_img", saveVehicle_img);
        b.putInt("whichKind", whichKind);
        frag.setArguments(b);
        frag.show(ft, "txn_tag");

    }

    public void zoomImageInsurance(View v) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        MyDialogFragment frag = new MyDialogFragment();
        whichKind = 3;
        Bundle b = new Bundle();
        b.putString("insurance", saveMotor_insurance_img);
        b.putInt("whichKind", whichKind);
        frag.setArguments(b);
        frag.show(ft, "txn_tag");

    }

    public static class MyDialogFragment extends DialogFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(DialogFragment.STYLE_NORMAL, R.style.MY_DIALOG);
        }

        @Override
        public void onStart() {
            super.onStart();
            Dialog d = getDialog();
            if (d != null) {
                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                d.getWindow().setLayout(width, height);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.zooming_layout, container,
                    false);
            ImageView new_image = (ImageView) root
                    .findViewById(R.id.imageView1);

            Bundle extras = this.getArguments();

            String license_plate = extras.getString("license_plate");
            String vehicle_img = extras.getString("vehicle_img");
            String motor_insurance_img = extras.getString("insurance");
            int whichKind = extras.getInt("whichKind");
            if (whichKind == 1) {
                byte[] decodeString = Base64.decode(
                        license_plate, Base64.DEFAULT);
                Bitmap decodeByte = BitmapFactory
                        .decodeByteArray(decodeString, 0,
                                decodeString.length);
                new_image.setImageBitmap(decodeByte);
            } else if (whichKind == 2) {
                byte[] decodeString = Base64.decode(
                        vehicle_img, Base64.DEFAULT);
                Bitmap decodeByte = BitmapFactory
                        .decodeByteArray(decodeString, 0,
                                decodeString.length);
                new_image.setImageBitmap(decodeByte);
            } else if (whichKind == 3) {
                byte[] decodeString = Base64.decode(
                        motor_insurance_img, Base64.DEFAULT);
                Bitmap decodeByte = BitmapFactory
                        .decodeByteArray(decodeString, 0,
                                decodeString.length);
                new_image.setImageBitmap(decodeByte);

            }
            return root;
        }

    }

    private void selectImage() {
        final CharSequence[] items = { "Camera",getResources().getString(R.string.gallery), getResources().getString(R.string.cancel) };

        AlertDialog.Builder builder = new AlertDialog.Builder(
                VehicleActivity.this);
        builder.setTitle(getResources().getString(R.string.add_image));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Camera")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment
                            .getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals(getResources().getString(R.string.gallery))) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, getResources().getString(R.string.select_image)),
                            SELECT_FILE);
                } else if (items[item].equals(getResources().getString(R.string.cancel))) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                File f = new File(Environment.getExternalStorageDirectory()
                        .toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    Bitmap bm = getResizedBitmap(f.getAbsolutePath());
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                            bm.getHeight(), matrix, true);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    bm.compress(Bitmap.CompressFormat.JPEG, 90, stream);

                    byte[] image = stream.toByteArray();
                    String img_str_new = Base64.encodeToString(image, 0);

                    if (whichKind == 1) {
                        license_plate_img.setBackground(null);
                        license_plate_img.setImageBitmap(bm);
                        saveLicense_plate_img = img_str_new;
                    } else if (whichKind == 2) {
                        vehicle_image.setBackground(null);
                        vehicle_image.setImageBitmap(bm);
                        saveVehicle_img = img_str_new;
                    } else if (whichKind == 3) {
                        moto_insurance_img.setBackground(null);
                        moto_insurance_img.setImageBitmap(bm);
                        saveMotor_insurance_img = img_str_new;
                    }


                    String path = Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "Phoenix" + File.separator + "default";
                    f.delete();
                    OutputStream fOut = null;
                    File file = new File(path, String.valueOf(System
                            .currentTimeMillis()) + ".jpg");
                    try {
                        fOut = new FileOutputStream(file);
                        bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                        fOut.flush();
                        fOut.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();

                String tempPath = getPath(selectedImageUri,
                        VehicleActivity.this);
                Bitmap bm = getResizedBitmap(tempPath);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                // bm=getResizedBitmap(bm,70,120);
                byte[] image = stream.toByteArray();
                String img_str_new = Base64.encodeToString(image, 0);
                if (whichKind == 1) {
                    license_plate_img.setBackground(null);
                    license_plate_img.setImageBitmap(bm);
                    saveLicense_plate_img = img_str_new;
                } else if (whichKind == 2) {
                    vehicle_image.setBackground(null);
                    vehicle_image.setImageBitmap(bm);
                    saveVehicle_img = img_str_new;
                } else if (whichKind == 3) {
                    moto_insurance_img.setBackground(null);
                    moto_insurance_img.setImageBitmap(bm);
                    saveMotor_insurance_img = img_str_new;
                }

            }
        }
    }

    public Bitmap getResizedBitmap(String tempPath) {
        Bitmap bm;
        // Decode image size
        BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
        btmapOptions.inJustDecodeBounds = true;

        bm = BitmapFactory.decodeFile(tempPath, btmapOptions);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 120;

        // Find the correct scale value. It should be the power of 2.
        int scale = 1;
        while (btmapOptions.outWidth / scale / 2 >= REQUIRED_SIZE
                && btmapOptions.outHeight / scale / 2 >= REQUIRED_SIZE)
            scale *= 2;

        // Decode with inSampleSize
        BitmapFactory.Options btmapOptions2 = new BitmapFactory.Options();
        btmapOptions2.inSampleSize = scale;
        return BitmapFactory.decodeFile(tempPath, btmapOptions2);

    }

    public String getPath(Uri uri, Activity activity) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        @SuppressWarnings("deprecation")
        Cursor cursor = activity
                .managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    public void editVehicleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.change_info);
        View view = View.inflate(this, R.layout.dialog_register_vehicle, null);
        builder.setView(view);
        final EditText editTypeVehicle = (EditText) view
                .findViewById(R.id.editTypeVehicle);
        final EditText editLicensePlate = (EditText) view
                .findViewById(R.id.editLicensePlate);
        final EditText editRegCertificate = (EditText) view
                .findViewById(R.id.editRegCertificate);
        Button confirm = (Button) view
                .findViewById(R.id.btnConfirmChangeVehicle);
        Button cancel = (Button) view
                .findViewById(R.id.btnCancelChangeVehicle);
        editTypeVehicle.setText(tvTypeVehicle.getText().toString());
        editLicensePlate.setText(tvLicensePlate.getText().toString());
        editRegCertificate.setText(tvRegCertificate.getText().toString());
        final AlertDialog dialog = builder.create();
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveType = editTypeVehicle.getText().toString();
                saveLicensePlate = editLicensePlate.getText().toString();
                saveCertificate = editRegCertificate.getText().toString();
                if (tvTypeVehicle.getText().toString().equals(saveType)
                        && tvLicensePlate.getText().toString().equals(saveLicensePlate)
                        && tvRegCertificate.getText().toString().equals(saveCertificate)) {
                    Toast.makeText(getApplicationContext(), R.string.cancel_change, Toast.LENGTH_SHORT).show();
                } else if (saveType.length() == 0 || saveLicensePlate.length() == 0 || saveCertificate.length() == 0) {
                    Toast.makeText(getApplicationContext(),
                            R.string.no_input,
                            Toast.LENGTH_SHORT).show();
                } else {
                    tvTypeVehicle.setText(editTypeVehicle.getText().toString());
                    tvLicensePlate.setText(editLicensePlate.getText().toString());
                    tvRegCertificate.setText(editRegCertificate.getText().toString());
                    dialog.dismiss();
                }

            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }


}
