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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DetailVehicleActivity extends ActionBarActivity {
    private SessionManager session;
    private SweetAlertDialog pDialog;
    private String vehicle_id;
    private CustomActionBar custom_actionbar;
    private android.support.v7.app.ActionBar actionBar;
    private TextView tvTypeVehicle, tvLicensePlate, tvRegCertificate;
    private ImageView imgLicensePlate, imgVehicle, imgInsurance;
    private static final int SELECT_FILE = 1, REQUEST_CAMERA = 2;
    private String saveType, saveLicensePlate, saveCertificate;
    private int whichKind = 0;
    //    private ImageView license_plate_img, vehicle_image, moto_insurance_img;
    private String type, license_plate, reg_certificate, license_plate_img, vehicle_img, motor_insurance_img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_vehicle);
        tvTypeVehicle = (TextView) findViewById(R.id.tvTypeVehicle);
        tvLicensePlate = (TextView) findViewById(R.id.tvLicensePlate);
        tvRegCertificate = (TextView) findViewById(R.id.tvRegCertificate);
        imgLicensePlate = (ImageView) findViewById(R.id.license_plate_image);
        imgVehicle = (ImageView) findViewById(R.id.vehicle_image);
        imgInsurance = (ImageView) findViewById(R.id.moto_insurance_img);
        custom_actionbar=new CustomActionBar(this,actionBar,pDialog,2);
        actionBar=custom_actionbar.getActionBar();
        session = new SessionManager(this);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            vehicle_id = bundle.getString("bundle");
            getVehicle(vehicle_id);
        }

    }

    public void editVehicle(View v) {
        editVehicleDialog();
    }

    public void btnEditVehicle(View v) {
        editVehicle(tvTypeVehicle.getText().toString().trim(), tvLicensePlate.getText().toString().trim(), tvRegCertificate.getText().toString().trim(), license_plate_img, vehicle_img, motor_insurance_img);
    }

    public void btnDeleteVehicle(View v) {
        confirmDelete();
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
        b.putString("license_plate", license_plate_img);
        b.putInt("whichKind", whichKind);
        frag.setArguments(b);
        frag.show(ft, "txn_tag");

    }

    public void zoomImageVehicle(View v) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        MyDialogFragment frag = new MyDialogFragment();
        whichKind = 2;
        Bundle b = new Bundle();
        b.putString("vehicle_img", vehicle_img);
        b.putInt("whichKind", whichKind);
        frag.setArguments(b);
        frag.show(ft, "txn_tag");

    }

    public void zoomImageInsurance(View v) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        MyDialogFragment frag = new MyDialogFragment();
        whichKind = 3;
        Bundle b = new Bundle();
        b.putString("insurance", motor_insurance_img);
        b.putInt("whichKind", whichKind);
        frag.setArguments(b);
        frag.show(ft, "txn_tag");

    }

    private void selectImage() {
        final CharSequence[] items = { "Camera",getResources().getString(R.string.gallery), getResources().getString(R.string.cancel) };

        AlertDialog.Builder builder = new AlertDialog.Builder(
                DetailVehicleActivity.this);
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


    public void getVehicle(final String vehicle_id) {
        String tag_string_req = "get_vehicle";
        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_GET_VEHICLE + "/" + vehicle_id, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("", "Vehicle Response: " + response.toString());
                hidePDialog();

                try {
                    JSONObject jObj = new JSONObject(
                            response.substring(response.indexOf("{"),
                                    response.lastIndexOf("}") + 1));
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        type = jObj.getString("type");
                        license_plate = jObj.getString("license_plate");
                        reg_certificate = jObj.getString("reg_certificate");
                        license_plate_img = jObj.getString("license_plate_img");
                        vehicle_img = jObj.getString("vehicle_img");
                        motor_insurance_img = jObj.getString("motor_insurance_img");
                        tvTypeVehicle.setText(type);
                        tvLicensePlate.setText(license_plate);
                        tvRegCertificate.setText(reg_certificate);
                        //IMAGE of License Plate
                        byte[] decodeString_LicensePlate = Base64.decode(
                                license_plate_img, Base64.DEFAULT);
                        Bitmap decodeByte_LicensePlate = BitmapFactory
                                .decodeByteArray(decodeString_LicensePlate, 0,
                                        decodeString_LicensePlate.length);
                        imgLicensePlate.setImageBitmap(decodeByte_LicensePlate);
                        //IMAGE of Vehicle's Type
                        byte[] decodeString_VehicleType = Base64.decode(
                                vehicle_img, Base64.DEFAULT);
                        Bitmap decodeByte_VehicleType = BitmapFactory
                                .decodeByteArray(decodeString_VehicleType, 0,
                                        decodeString_VehicleType.length);
                        imgVehicle.setImageBitmap(decodeByte_VehicleType);
                        //IMAGE of Morto Insurance
                        byte[] decodeString_Insurance = Base64.decode(
                                motor_insurance_img, Base64.DEFAULT);
                        Bitmap decodeByte_Insurance = BitmapFactory
                                .decodeByteArray(decodeString_Insurance, 0,
                                        decodeString_Insurance.length);
                        imgInsurance.setImageBitmap(decodeByte_Insurance);
                    } else {
                        // Error in login. Get the error message
                        String message = jObj.getString("message");
                        Toast.makeText(getApplicationContext(),
                                message, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("", "Avatar Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hidePDialog();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", session.getAPIKey());

                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    public void editVehicle(final String saveType, final String saveLicensePlate, final String saveCertificate,
                            final String  saveLicense_plate_img, final String saveVehicle_img, final String saveMotor_insurance_img) {
        String tag_string_req = "edit_vehicle";
        StringRequest strReq = new StringRequest(Request.Method.PUT,
                AppConfig.URL_GET_VEHICLE + "/" + vehicle_id + "?lang=" + Locale.getDefault().getLanguage(), new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("", "Vehicle Response: " + response.toString());
                hidePDialog();

                try {
                    JSONObject jObj = new JSONObject(
                            response.substring(response.indexOf("{"),
                                    response.lastIndexOf("}") + 1));
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        String message = jObj.getString("message");
                        Toast.makeText(getApplicationContext(),
                                message, Toast.LENGTH_SHORT).show();
                    } else {
                        // Error in login. Get the error message
                        String message = jObj.getString("message");
                        Toast.makeText(getApplicationContext(),
                                message, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("", "Avatar Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hidePDialog();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", session.getAPIKey());

                return params;
            }

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("type", saveType);
                params.put("license_plate", saveLicensePlate);
                params.put("reg_certificate", saveCertificate);
                params.put("license_plate_img", saveLicense_plate_img);
                params.put("vehicle_img", saveVehicle_img);
                params.put("motor_insurance_img", saveMotor_insurance_img);

                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public void deleteVehicle() {
        String tag_string_req = "edit_vehicle";
        StringRequest strReq = new StringRequest(Request.Method.DELETE,
                AppConfig.URL_GET_VEHICLE + "/" + vehicle_id + "?lang=" + Locale.getDefault().getLanguage(), new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("", "Vehicle Response: " + response.toString());
                hidePDialog();

                try {
                    JSONObject jObj = new JSONObject(
                            response.substring(response.indexOf("{"),
                                    response.lastIndexOf("}") + 1));
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        Intent returnIntent = new Intent(getApplicationContext(), ManageVehicle.class);
                        setResult(RESULT_OK, returnIntent);

                        String message = jObj.getString("message");
                        Toast.makeText(getApplicationContext(),
                                message, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        // Error in login. Get the error message
                        String message = jObj.getString("message");
                        Toast.makeText(getApplicationContext(),
                                message, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("", "Avatar Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hidePDialog();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", session.getAPIKey());

                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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
                        imgLicensePlate.setBackground(null);
                        imgLicensePlate.setImageBitmap(bm);
                        license_plate_img = img_str_new;
                    } else if (whichKind == 2) {
                        imgVehicle.setBackground(null);
                        imgVehicle.setImageBitmap(bm);
                        vehicle_img = img_str_new;
                    } else if (whichKind == 3) {
                        imgInsurance.setBackground(null);
                        imgInsurance.setImageBitmap(bm);
                        motor_insurance_img = img_str_new;
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
                        DetailVehicleActivity.this);
                Bitmap bm = getResizedBitmap(tempPath);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                // bm=getResizedBitmap(bm,70,120);
                byte[] image = stream.toByteArray();
                String img_str_new = Base64.encodeToString(image, 0);
                if (whichKind == 1) {
                    imgLicensePlate.setBackground(null);
                    imgLicensePlate.setImageBitmap(bm);
                    license_plate_img = img_str_new;
                } else if (whichKind == 2) {
                    imgVehicle.setBackground(null);
                    imgVehicle.setImageBitmap(bm);
                    vehicle_img = img_str_new;
                } else if (whichKind == 3) {
                    imgInsurance.setBackground(null);
                    imgInsurance.setImageBitmap(bm);
                    motor_insurance_img = img_str_new;
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

    public void confirmDelete() {

        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);

        dialog.setTitleText(getResources().getString(R.string.delete_vehicle))
                .setContentText(getResources().getString(R.string.you_sure))
                .setCancelText(getResources().getString(R.string.cancel))
                .setConfirmText(getResources().getString(R.string.ok))
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        deleteVehicle();

                    }
                }).show();

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
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
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
