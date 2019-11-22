package itachi_waiyan.com.padc_firebase_assignment.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import itachi_waiyan.com.padc_firebase_assignment.R
import itachi_waiyan.com.padc_firebase_assignment.utils.RC_SIGN_IN
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import java.util.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.GoogleAuthProvider
import itachi_waiyan.com.padc_firebase_assignment.MainActivity

class LoginActivity : AppCompatActivity(), OnCompleteListener<AuthResult> {

    lateinit var callbackManager : CallbackManager
    lateinit var auth : FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        FacebookSdk.sdkInitialize(applicationContext);

        val userName = Observable.create<String> { emitter ->
            et_username.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(p0: Editable?) {

                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    emitter.onNext(p0.toString())
                }

            })
        }

        val password = Observable.create<String> {emitter ->
            et_password.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(p0: Editable?) {

                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    emitter.onNext(p0.toString())
                }

            })
        }


        Observable.combineLatest<String,String,Boolean>(
            userName,
            password,
            BiFunction { name, password ->
                name.length > 6 && password.length > 6 && name.contains("@")
            }
        ).subscribe{
            if(it==true) {
                btn_login.isEnabled = true
                btn_login.setTextColor(resources.getColor(R.color.colorMainLayout))
            }
            else {
                btn_login.isEnabled = false
                btn_login.setTextColor(resources.getColor(R.color.textColorSecondaryLight))
            }
        }

        btn_login.setOnClickListener{
            showLoading()
            signInWithEmailPassword(et_username.text.toString(),et_password.text.toString())
        }

        btn_facebook.setOnClickListener {
            showLoading()
            signInWithFacebook()
        }

        btn_google.setOnClickListener {
            showLoading()
            signInWithGoogle()
        }

    }

    fun showLoading() {
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        if (!progressDialog.isShowing() && !isFinishing) {
            progressDialog.show()
        }
    }

    fun hideLoading() {
        if (progressDialog!=null && progressDialog.isShowing()) {
            progressDialog.dismiss()
        }
    }

    //Facebook sign in
    private fun signInWithFacebook(){
        LoginManager.getInstance().logInWithReadPermissions(this,Arrays.asList( "public_profile","name","email"))
        LoginManager.getInstance().registerCallback(callbackManager,object : FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult?) {
                if(result!=null){
                    GraphRequest.newMeRequest(result.accessToken,object : GraphRequest.GraphJSONObjectCallback{
                        override fun onCompleted(`object`: JSONObject?, response: GraphResponse?) {

                            hideLoading()
                            startActivity(MainActivity.newIntent(applicationContext,`object`!!.getString("name"),
                                `object`!!.getString("email"),
                                "NULL",
                                `object`!!.getString("public_profile")
                            ))
                        }

                    })
                }
                else Log.d("test---","result null")

            }

            override fun onCancel() {
                hideLoading()
                Log.d("test---","cancel")
            }

            override fun onError(error: FacebookException?) {
                Log.d("test---","error")
            }

        })
    }

    //Google sign in
    private fun signInWithGoogle(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Email pass sign in
    private fun signInWithEmailPassword(email : String, password : String){
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this)
    }

    // For email login
    override fun onComplete(p0: Task<AuthResult>) {
        hideLoading()
        startActivity(MainActivity.newIntent(this,"NULL",et_username.text.toString(),"NULL",null))
    }


    //For google authentication
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data) as Task
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            hideLoading()
                            startActivity(MainActivity.newIntent(this,user?.displayName.toString(),
                                user?.email.toString(),
                                user?.phoneNumber.toString(),
                                user?.photoUrl.toString()
                                ))
                        } else {
                            Log.w("test---", "signInWithCredential:failure", task.exception)
                        }

                        // ...
                    }
            } catch (e: ApiException) {
                Log.w("test---", "Google sign in failed",e)
            }
        }

    }
}