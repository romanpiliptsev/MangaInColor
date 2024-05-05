package com.encouragingroseprr.mangaincolor.presentation.activities

import android.content.Context
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.encouragingroseprr.mangaincolor.MangaInColorApp
import com.encouragingroseprr.mangaincolor.R
import com.encouragingroseprr.mangaincolor.databinding.ActivityArBinding
import com.encouragingroseprr.mangaincolor.presentation.utils.ImageUtil
import com.encouragingroseprr.mangaincolor.presentation.viewmodels.MainViewModel
import com.encouragingroseprr.mangaincolor.presentation.viewmodels.ViewModelFactory
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import javax.inject.Inject

class ArActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityArBinding.inflate(layoutInflater)
    }

    private val component by lazy {
        (application as MangaInColorApp).component
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val vm: MainViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var arFragment: ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        showToast(SETUP_AR)

        arFragment =
            (supportFragmentManager.findFragmentById(R.id.fragment_container) as ArFragment).apply {
                setOnSessionConfigurationListener { session, config ->
                    config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                    config.focusMode = Config.FocusMode.AUTO

                    session.configure(config)
                    setupDatabase(config, session)

                    session.apply {
                        resume()
                        pause()
                        resume()
                    }
                }

                setOnViewCreatedListener { arSceneView ->
                    arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL)
                }
                setOnTapArPlaneListener(::onTapPlane)
            }

        vm.getImageStateLiveData.observe(this) {
            when (it) {
                is MainViewModel.GetImageState.Loading -> {}
                is MainViewModel.GetImageState.Error -> {
                    showToast(ERROR_MESSAGE)
                }
                is MainViewModel.GetImageState.Loaded -> {
                    if (it.imageBitmap == null) {
                        showToast(ERROR_MESSAGE)
                    } else {
                        if (viewRenderable == null) {
                            showToast(ERROR_MESSAGE)
                            Log.i("MIC", "viewRenderable==null")
                        } else {
                            viewRenderable?.view?.findViewById<ImageView>(R.id.main_image)
                                ?.setImageBitmap(it.imageBitmap)

                            val anchor = augImage?.createAnchor(augImage?.centerPose)
                            val anchorNode = AnchorNode(anchor)
                            anchorNode.worldScale = Vector3(SCALE_CF, SCALE_CF, SCALE_CF)

                            val node = TransformableNode(arFragment.transformationSystem)
                            node.renderable = viewRenderable
                            node.parent = anchorNode
                            node.localRotation =
                                Quaternion.axisAngle(Vector3(1f, 0f, 0f), ROTATE_DEGREES)
                            arFragment.arSceneView.scene.addChild(anchorNode)
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        startActivity(MainActivity.newIntent(this))
    }

    private fun setupDatabase(config: Config, session: Session) {
        val augmentedImageDatabase = this.assets.open(DB_NAME).use {
            AugmentedImageDatabase.deserialize(session, it)
        }

        config.augmentedImageDatabase = augmentedImageDatabase
    }

    private var viewRenderable: ViewRenderable? = null
    private var augImage: AugmentedImage? = null
    private var workingFlag = false

    private fun onTapPlane(hitResult: HitResult?, plane: Plane?, motionEvent: MotionEvent?) {
        if (!workingFlag) {
            workingFlag = true

            var frameImage: Image? = null
            val sceneView = arFragment.arSceneView
            val images: Collection<AugmentedImage>? = sceneView.updatedAugmentedImages

            Log.i("MIC", "onTapPlane ${images?.size}")

            images?.forEach { image ->
                if (image.trackingState == TrackingState.TRACKING &&
                    image.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING
                ) {
                    showToast(IMAGE_WAS_FOUNDED)

                    frameImage = sceneView.arFrame?.acquireCameraImage()
                    val byteStream = ByteArrayInputStream(ImageUtil.imageToByteArray(frameImage))

                    // Создание плоскости с текстурой изображения
                    val viewRenderable = ViewRenderable.builder()
                        .setView(this, R.layout.image)
                        .build()

                    // Добавление слушателя, который будет вызван, когда ViewRenderable будет готово
                    viewRenderable.thenAccept { renderable ->
                        renderable.isShadowCaster = false
                        renderable.isShadowReceiver = false
                        this.viewRenderable = renderable
                        augImage = image

                        vm.getResultImage(
                            getFileFromByteArrayInputStream(
                                image.name
                                    .replace(JPG, EMPTY).replace(JPEG, EMPTY).replace(PNG, EMPTY),
                                byteStream
                            ) ?: throw RuntimeException(FILE_DOES_NOT_EXIST),
                            JPEG_FORMAT // !!!
                        )
                    }
                    frameImage?.close()
                    workingFlag = false
                    return
                }
            }
            frameImage?.close()
            workingFlag = false
        } else {
            showToast(WAIT)
        }
    }

    // ex getFileFromDrawable
    private fun getFileFromByteArrayInputStream(
        fileName: String,
        inputStream: ByteArrayInputStream
    ): File? {
        try {
//            val resourceId = resources.getIdentifier(fileName, DRAWABLE, packageName)
//            if (resourceId == 0) {
//                return null
//            }
//            val inputStream: InputStream = resources.openRawResource(resourceId)

            // Создание временного файла для сохранения изображения
            val tempFile = File.createTempFile(fileName, null, cacheDir)

            // Запись данных из InputStream в файл
            val outputStream: OutputStream = FileOutputStream(tempFile)
            val buffer = ByteArray(BUFFER_SIZE)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            inputStream.close()
            outputStream.close()

            return tempFile
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {

        fun newIntent(context: Context): Intent = Intent(context, ArActivity::class.java)

        //        private const val DRAWABLE = "drawable"
        private const val EMPTY = ""
        private const val JPG = ".jpg"
        private const val JPEG = ".jpeg"
        private const val PNG = ".png"
        private const val JPEG_FORMAT = "image/jpeg"
        private const val FILE_DOES_NOT_EXIST = "The file does not exist!"
        private const val WAIT = "Wait for the operation to complete!"
        private const val IMAGE_WAS_FOUNDED = "Image was founded! Wait!"
        private const val DB_NAME = "images.imgdb"
        private const val ERROR_MESSAGE = "Error while processing the image! Try again!"
        private const val SETUP_AR = "Start to setup AR!"
        private const val SCALE_CF = 0.2f
        private const val ROTATE_DEGREES = -90f
        private const val BUFFER_SIZE = 1024
    }
}