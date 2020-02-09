package me.duckfollow.ozone.activity

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import me.duckfollow.ozone.R
import me.duckfollow.ozone.simple.BaseActivity
import me.duckfollow.ozone.user.UserProfile
import me.duckfollow.ozone.utils.ConvertImagetoBase64
import me.duckfollow.ozone.view.crop.UCrop
import me.duckfollow.ozone.view.crop.UCropFragment
import java.io.File
import java.io.IOException
import java.util.HashMap

class ProfileActivity : AppCompatActivity() {

    lateinit var btn_back: ImageButton
    lateinit var view_profile:RelativeLayout
    lateinit var img_profile:ImageView
    lateinit var btn_qr_scan:ImageButton
    private val requestMode = 23
    private var mAlertDialog: AlertDialog? = null
    private var fragment: UCropFragment? = null

    private var mShowLoader: Boolean = false

    private var mToolbarTitle: String? = null
    @DrawableRes
    private var mToolbarCancelDrawable: Int = 0
    @DrawableRes
    private var mToolbarCropDrawable: Int = 0
    // Enables dynamic coloring
    private var mToolbarColor: Int = 0
    private var mStatusBarColor: Int = 0
    private var mToolbarWidgetColor: Int = 0

    lateinit var myRefUser: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        initView()
        btn_back.setOnClickListener {
            supportFinishAfterTransition()
        }

        view_profile.setOnClickListener {
            pickFromGallery()
        }

        val imgprofile = UserProfile(this).getImageBase64()
        if(imgprofile != "") {
            val b = ConvertImagetoBase64().base64ToBitmap(imgprofile)
            img_profile.setImageBitmap(b)
        }

        val database = FirebaseDatabase.getInstance().reference
        myRefUser = database.child("user/")

        btn_qr_scan.setOnClickListener {
            val i_scan = Intent(this,ScanActivity::class.java)
            startActivity(i_scan)
        }

    }

    private fun initView(){
        btn_back = findViewById(R.id.btn_back)
        view_profile = findViewById(R.id.view_profile)
        img_profile = findViewById(R.id.img_profile)
        btn_qr_scan = findViewById(R.id.btn_qr_scan)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == requestMode) {
                val selectedUri = data!!.data
                if (selectedUri != null) {
                    Log.d("pathUri",selectedUri.toString())
                    startCrop(selectedUri)
                } else {
                    Toast.makeText(
                        this,
                        R.string.toast_cannot_retrieve_selected_image,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }else if (requestCode == UCrop.REQUEST_CROP) {
                handleCropResult(data!!)
            }

            if (requestCode == UCrop.REQUEST_CROP) {
                val android_id = Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
                val uri = UCrop.getOutput(data!!)
                val x = getBitmapFromUri(uri!!)
                val b = ConvertImagetoBase64().getResizedBitmap(x,250,250)
                val imgBase64 = ConvertImagetoBase64().bitmapToBase64(b)
                img_profile.setImageBitmap(b)
                UserProfile(this).setImageBase64(imgBase64)
                val map = HashMap<String, Any>()
                map.put("img_url",imgBase64)
                myRefUser.child(android_id+"/profile").updateChildren(map)
            }
        }
        if (resultCode == UCrop.RESULT_ERROR) {
            handleCropError(data!!)
        }
    }

    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    private fun pickFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                getString(R.string.permission_read_storage_rationale),
                BaseActivity.REQUEST_STORAGE_READ_ACCESS_PERMISSION
            )
        } else {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
                .setType("image/*")
                .addCategory(Intent.CATEGORY_OPENABLE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val mimeTypes = arrayOf("image/jpeg", "image/png")
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }

            startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_picture)), requestMode)
        }
    }

    fun handleCropResult(result: Intent) {
        val resultUri = UCrop.getOutput(result)
        if (resultUri != null) {
            Log.d("testUri",resultUri.toString())
        } else {
            Toast.makeText(
                this,
                R.string.toast_cannot_retrieve_cropped_image,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun handleCropError(result: Intent) {
        val cropError = UCrop.getError(result)
        if (cropError != null) {
            Log.e("test", "handleCropError: ", cropError)
            Toast.makeText(this, cropError!!.message, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, R.string.toast_unexpected_error, Toast.LENGTH_SHORT)
                .show()
        }
    }

    protected fun requestPermission(permission: String, rationale: String, requestCode: Int) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            showAlertDialog(getString(R.string.permission_title_rationale), rationale,
                DialogInterface.OnClickListener { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(permission), requestCode
                    )
                }, getString(R.string.label_ok), null, getString(R.string.label_cancel)
            )
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }

    protected fun showAlertDialog(
        title: String?, message: String?,
        onPositiveButtonClickListener: DialogInterface.OnClickListener?,
        positiveText: String,
        onNegativeButtonClickListener: DialogInterface.OnClickListener?,
        negativeText: String
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(positiveText, onPositiveButtonClickListener)
        builder.setNegativeButton(negativeText, onNegativeButtonClickListener)
        mAlertDialog = builder.show()
    }

    private fun startCrop(uri: Uri) {
        var destinationFileName = SAMPLE_CROPPED_IMAGE_NAME+ ".png"

        var uCrop = UCrop.of(uri, Uri.fromFile(File(this.cacheDir, destinationFileName)))

        uCrop = basisConfig(uCrop)
        uCrop = advancedConfig(uCrop)

        if (requestMode == REQUEST_SELECT_PICTURE_FOR_FRAGMENT) {       //if build variant = fragment
            setupFragment(uCrop)
        } else {                                                        // else start uCrop Activity
            Log.d("test","activity")
            uCrop.start(this)
        }

    }

    fun setupFragment(uCrop: UCrop) {
        fragment = uCrop.getFragment(uCrop.getIntent(this).getExtras())
        this.supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, fragment!!, UCropFragment.TAG)
            .commitAllowingStateLoss()

        setupViews(uCrop.getIntent(this).getExtras())
    }

    fun setupViews(args: Bundle) {
        //settingsView!!.visibility = View.GONE
        mStatusBarColor = args.getInt(
            UCrop.Options.EXTRA_STATUS_BAR_COLOR,
            ContextCompat.getColor(this, R.color.ucrop_color_statusbar)
        )
        mToolbarColor = args.getInt(
            UCrop.Options.EXTRA_TOOL_BAR_COLOR,
            ContextCompat.getColor(this, R.color.ucrop_color_toolbar)
        )
        mToolbarCancelDrawable =
            args.getInt(UCrop.Options.EXTRA_UCROP_WIDGET_CANCEL_DRAWABLE, R.drawable.ucrop_ic_cross)
        mToolbarCropDrawable =
            args.getInt(UCrop.Options.EXTRA_UCROP_WIDGET_CROP_DRAWABLE, R.drawable.ucrop_ic_done)
        mToolbarWidgetColor = args.getInt(
            UCrop.Options.EXTRA_UCROP_WIDGET_COLOR_TOOLBAR,
            ContextCompat.getColor(this, R.color.ucrop_color_toolbar_widget)
        )
        mToolbarTitle = args.getString(UCrop.Options.EXTRA_UCROP_TITLE_TEXT_TOOLBAR)
        mToolbarTitle =
            if (mToolbarTitle != null) mToolbarTitle else resources.getString(R.string.ucrop_label_edit_photo)

        //setupAppBar()
    }

    private fun basisConfig(uCrop: UCrop): UCrop {
        var uCrop = uCrop
        uCrop = uCrop.withAspectRatio(1F, 1F)
        return uCrop
    }

    private fun advancedConfig(uCrop: UCrop): UCrop {
        val options = UCrop.Options()
        options.setCompressionQuality(100)
        options.setCompressionFormat(Bitmap.CompressFormat.PNG)

        //options.setHideBottomControls(mCheckBoxHideBottomControls!!.isChecked)
        //options.setFreeStyleCropEnabled(mCheckBoxFreeStyleCrop!!.isChecked)

        /*
        If you want to configure how gestures work for all UCropActivity tabs

        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
        * */

        /*
        This sets max size for bitmap that will be decoded from source Uri.
        More size - more memory allocation, default implementation uses screen diagonal.

        options.setMaxBitmapSize(640);
        * */


        /*

        Tune everything (ﾉ◕ヮ◕)ﾉ*:･ﾟ✧

        options.setMaxScaleMultiplier(5);
        options.setImageToCropBoundsAnimDuration(666);
        options.setDimmedLayerColor(Color.CYAN);
        options.setCircleDimmedLayer(true);
        options.setShowCropFrame(false);
        options.setCropGridStrokeWidth(20);
        options.setCropGridColor(Color.GREEN);
        options.setCropGridColumnCount(2);
        options.setCropGridRowCount(1);
        options.setToolbarCropDrawable(R.drawable.your_crop_icon);
        options.setToolbarCancelDrawable(R.drawable.your_cancel_icon);

        // Color palette
        options.setToolbarColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setActiveWidgetColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setRootViewBackgroundColor(ContextCompat.getColor(this, R.color.your_color_res));

        // Aspect ratio options
        options.setAspectRatioOptions(1,
            new AspectRatio("WOW", 1, 2),
            new AspectRatio("MUCH", 3, 4),
            new AspectRatio("RATIO", CropImageView.DEFAULT_ASPECT_RATIO, CropImageView.DEFAULT_ASPECT_RATIO),
            new AspectRatio("SO", 16, 9),
            new AspectRatio("ASPECT", 1, 1));

       */

        return uCrop.withOptions(options)
    }

    companion object {

        const val REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101
        const val REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102
        private val REQUEST_SELECT_PICTURE = 0x01
        private val REQUEST_SELECT_PICTURE_FOR_FRAGMENT = 0x02
        private val SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage"
    }
}
