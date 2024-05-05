package com.encouragingroseprr.mangaincolor.presentation.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.encouragingroseprr.mangaincolor.MangaInColorApp
import com.encouragingroseprr.mangaincolor.databinding.FragmentMenuBinding
import com.encouragingroseprr.mangaincolor.presentation.activities.ArActivity
import com.encouragingroseprr.mangaincolor.presentation.viewmodels.MainViewModel
import com.encouragingroseprr.mangaincolor.presentation.viewmodels.ViewModelFactory
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class MenuFragment : Fragment() {

    private val binding by lazy {
        FragmentMenuBinding.inflate(layoutInflater)
    }

    private val component by lazy {
        (activity?.application as MangaInColorApp).component
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val vm: MainViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            val pickImageLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                        val contentResolver = requireContext().contentResolver
                        val selectedImageUri: Uri = result.data!!.data!!
                        val inputStream = contentResolver.openInputStream(selectedImageUri)
                        inputStream?.close()

                        val mimeType = contentResolver.getType(selectedImageUri)

                        uriToFile(requireContext(), selectedImageUri).also {
                            if (it != null) {
                                vm.getResultImage(
                                    it, mimeType ?: EMPTY
                                )
                            } else {
                                Toast.makeText(requireContext(), ERROR_MESSAGE, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }

            buttonChoose.setOnClickListener {
                pickImageLauncher.launch(
                    Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                )
            }

            buttonMenu.setOnClickListener {
                loadedGroup.visibility = View.GONE
                startGroup.visibility = View.VISIBLE
            }

            vm.getImageStateLiveData.observe(viewLifecycleOwner) {
                when (it) {
                    is MainViewModel.GetImageState.Loading -> {
                        startGroup.visibility = View.GONE
                        progressBar.visibility = View.VISIBLE
                    }
                    is MainViewModel.GetImageState.Error -> {
                        Toast.makeText(requireContext(), ERROR_MESSAGE, Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE
                        startGroup.visibility = View.VISIBLE
                    }
                    is MainViewModel.GetImageState.Loaded -> {
                        progressBar.visibility = View.GONE
                        loadedGroup.visibility = View.VISIBLE

                        if (it.imageBitmap == null) {
                            Toast.makeText(requireContext(), NULL_MESSAGE, Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            resultImage.setImageBitmap(it.imageBitmap)
                        }
                    }
                }
            }

            buttonCam.setOnClickListener {
                startActivity(ArActivity.newIntent(requireContext()))
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        val file = File(context.cacheDir, FILE_NAME)

        try {
            val inputStream = context.contentResolver.openInputStream(uri)

            inputStream?.use { input ->
                val outputStream = FileOutputStream(file)

                outputStream.use { output ->
                    val buffer = ByteArray(BUFFER_SIZE) // Размер буфера
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
                outputStream.close()
            }
            inputStream?.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() = MenuFragment()

        private const val FILE_NAME = "temp_image_file"
        private const val EMPTY = ""
        private const val ERROR_MESSAGE = "Error while processing the image! Try again!"
        private const val NULL_MESSAGE = "Null result! Try again!"
        private const val BUFFER_SIZE = 4096
    }
}