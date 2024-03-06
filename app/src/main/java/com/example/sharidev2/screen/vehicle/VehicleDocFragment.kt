import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.example.sharidev2.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.*

class VehicleDocFragment : Fragment() {

    private lateinit var licenseBackImageView: ImageView
    private lateinit var uploadBackButton: Button
    private lateinit var saveRecordButton: Button
    private lateinit var storageRef: StorageReference
    private lateinit var databaseRef: FirebaseDatabase

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_driving_license, container, false)

        licenseBackImageView = view.findViewById(R.id.license_back)
        uploadBackButton = view.findViewById(R.id.btn_upload_license_back)
        saveRecordButton = view.findViewById(R.id.save_vehicle_record)

        // Initialize Firebase Storage and Realtime Database
        storageRef = FirebaseStorage.getInstance().reference.child("license_images")
        databaseRef = FirebaseDatabase.getInstance()

        uploadBackButton.setOnClickListener {
            openFileChooser(PICK_IMAGE_REQUEST)
        }

        saveRecordButton.setOnClickListener {
            // Add code to save other record details to Firebase (if needed)

            // Upload images to Firebase Storage
            uploadImageToFirebaseStorage(licenseBackImageView, "back")
        }

        return view
    }

    private fun openFileChooser(request_code: Int) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, request_code)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val selectedImageUri = data.data
            val selectedImageBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedImageUri)

            licenseBackImageView.setImageBitmap(selectedImageBitmap)
        }
    }

    private fun uploadImageToFirebaseStorage(imageView: ImageView, imageType: String) {
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable).toBitmap()

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val imageRef = storageRef.child("${UUID.randomUUID()}_$imageType.jpg")
        val uploadTask = imageRef.putBytes(data)

        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // Handle successful uploads
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                // Save the image URL to Firebase Realtime Database
                val imageUrl = uri.toString()
                saveImageUrlToDatabase(imageUrl)
            }
        }
    }

    private fun saveImageUrlToDatabase(imageUrl: String) {
        // Get the current user's ID (You need to handle user authentication)
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            // Save the image URL to the "user_images" node in Realtime Database
            val imagesRef = databaseRef.getReference("user_images").child(userId)
            imagesRef.push().setValue(imageUrl)
        }
    }
}
