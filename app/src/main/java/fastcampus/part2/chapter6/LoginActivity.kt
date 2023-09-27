package fastcampus.part2.chapter6

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import fastcampus.part2.chapter6.Key.Companion.DB_URL
import fastcampus.part2.chapter6.Key.Companion.DB_USERS
import fastcampus.part2.chapter6.databinding.ActivityLoginBinding
import fastcampus.part2.chapter6.userlist.UserItem

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signUpButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일 또는 패스워드가 입력되지 않았습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // 회원가입 성공
                        Toast.makeText(this, "회원가입에 성공했습니다. 로그인해주세요.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        // 회원가입 실패
                        Toast.makeText(this, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.signInButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일 또는 패스워드가 입력되지 않았습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (task.isSuccessful && currentUser != null) {
                        val userId = currentUser.uid

                        Firebase.messaging.token.addOnCompleteListener { tokenResult ->
                            val token = tokenResult.result

                            // 사용자 정보 가져오기
                            Firebase.database(DB_URL).reference.child(DB_USERS).child(userId)
                                .get().addOnSuccessListener { dataSnapshot ->
                                    val user = dataSnapshot.getValue(UserItem::class.java)
                                    val updatedUser = mutableMapOf<String, Any>()
                                    updatedUser["userId"] = userId
                                    updatedUser["username"] = user?.username ?: email  // 기존 이름을 사용하거나 없을 경우 이메일 사용
                                    updatedUser["fcmToken"] = token

                                    Firebase.database(DB_URL).reference.child(DB_USERS).child(userId)
                                        .updateChildren(updatedUser)

                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                        }
                    } else {
                        Toast.makeText(this, "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

        }
    }
}
