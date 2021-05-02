package com.example.loginproject

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.kakao.sdk.auth.LoginClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import java.security.MessageDigest
import com.kakao.sdk.common.util.Utility
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton


private val RC_SIGN_IN = 9001
private var googleSignInClient: GoogleSignInClient? = null
private var firebaseAuth: FirebaseAuth? = null

class MainActivity : AppCompatActivity() {
    lateinit var mOAuthLoginInstance : OAuthLogin
    lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //hash 키 구하는 법
        val keyHash = Utility.getKeyHash(this)
        Log.d("Hash", keyHash)

        //네이버 아이도로 로그인
        var naver_client_id = "M_n9WAyX3OLR43MLzF0I"
        var naver_client_secret = "NOpZdFBvZG"
        var naver_client_name ="LoginProject"

        mContext = this

        mOAuthLoginInstance = OAuthLogin.getInstance()
        mOAuthLoginInstance.init(mContext, naver_client_id, naver_client_secret,
        naver_client_name)

        val mOAuthLoginHandler: OAuthLoginHandler = object : OAuthLoginHandler(){
            override fun run(success: Boolean) {
                if (success){

                    Log.d("sss",success.toString())
                }else{
                    val errorCode: String = mOAuthLoginInstance.getLastErrorCode(mContext).code
                    val errorDesc = mOAuthLoginInstance.getLastErrorDesc(mContext)

                    Toast.makeText(
                            baseContext, "errorCode:" + errorCode
                            + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show()
                }
            }
        }
        val buttonOAuthLoginImg = findViewById<OAuthLoginButton>(R.id.naver_login)
        buttonOAuthLoginImg.setOAuthLoginHandler(mOAuthLoginHandler)




        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()


        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //firebase auth 객체
        firebaseAuth = FirebaseAuth.getInstance();


        //구글 로그인 버튼 구현
        val googleLoginBtn = findViewById<SignInButton>(R.id.googleLoginBtn)

        googleLoginBtn.setOnClickListener {
            val signInIntent = googleSignInClient?.getSignInIntent()
            startActivityForResult(signInIntent, RC_SIGN_IN)


        }


        val kakao_login_button = findViewById<ImageButton>(R.id.kakao_login_button)

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            Log.d("token", token.toString())
            Log.d("error_code", error.toString())
            if (error != null) {
                when {
                    error.toString() == AuthErrorCause.AccessDenied.toString() -> {
                        Toast.makeText(this, "접근이 거부 됨(동의 취소)", Toast.LENGTH_SHORT).show()
                    }

                    else -> { // Unknown
                        Toast.makeText(this, "기타 에러", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (token != null) {
                Toast.makeText(this, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                Log.d("accessToken", token.accessToken)
                //ptNCCAGFBaI1OAjPBHeUItJpB_584M58e9Y96Ao9dBEAAAF44LqSgA
            }
        }

        kakao_login_button.setOnClickListener {
            if (LoginClient.instance.isKakaoTalkLoginAvailable(this)) {
                LoginClient.instance.loginWithKakaoTalk(this, callback = callback)
            } else {
                LoginClient.instance.loginWithKakaoAccount(this, callback = callback)
            }

        }

    }







    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //google 로그인 인텐트 응답
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {


            }
        }
    }//END onActivityResult

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this){

                    //성공여부
                    if(it.isSuccessful){

                        val user = firebaseAuth?.currentUser
                        Toast.makeText(this, "ㅅ성공", Toast.LENGTH_SHORT).show()

                    }else{
                        Toast.makeText(this, "t실패", Toast.LENGTH_SHORT).show()
                    }
                }
    }

}