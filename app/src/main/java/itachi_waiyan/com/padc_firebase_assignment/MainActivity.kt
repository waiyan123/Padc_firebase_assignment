package itachi_waiyan.com.padc_firebase_assignment

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object{

        private const val USER_NAME = "User name"
        private const val USER_EMAIL = "User email"
        private const val USER_PHONE = "User phone"
        private const val USER_PROFILE_URL = "User profile url"

        fun newIntent(context: Context, userName : String,userEmail : String,userPhone : String,userProfileUrl : String?): Intent{
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(USER_NAME,userName)
            intent.putExtra(USER_EMAIL,userEmail)
            intent.putExtra(USER_PHONE,userPhone)
            intent.putExtra(USER_PROFILE_URL,userProfileUrl)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userName = intent.getStringExtra(USER_NAME)
        val userEmail = intent.getStringExtra(USER_EMAIL)
        val userPhone = intent.getStringExtra(USER_PHONE)
        val userProfileUrl = intent.getStringExtra(USER_PROFILE_URL)

        if(userName!=null){
            tv_username.text = "User name - $userName"
        }
        if(userEmail!=null){
            tv_email.text = "Email - $userEmail"
        }
        if(userPhone!=null){
            tv_phone.text = "Phone - $userPhone"
        }
        if(userProfileUrl!=null){
            Glide.with(this)
                .load(userProfileUrl)
                .into(img_user_profile)
        }

        btn_ok.setOnClickListener {
            finish()
        }
    }
}
