package fastcampus.part2.chapter6.chatdetail

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import fastcampus.part2.chapter6.Key
import fastcampus.part2.chapter6.R
import fastcampus.part2.chapter6.databinding.ActivityChatdetailBinding
import fastcampus.part2.chapter6.userlist.UserItem
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import android.util.Log
import android.widget.TextView
import com.google.gson.JsonObject

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatdetailBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var resultTextView: TextView

    private var chatRoomId: String = ""
    private var otherUserId: String = ""
    private var otherUserFcmToken: String = ""
    private var myUserId: String = ""
    private var myUserName: String = ""
    private var isInit = false

    private val chatItemList = mutableListOf<ChatItem>()

    private fun analyzeMessage(message: String) {
        val requestBody = JsonObject().apply {
            addProperty("message", message)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .post(requestBody.toString().toRequestBody(mediaType))
            .url("http://9355-34-142-200-166.ngrok.io/analyze") // 서버 주소 입력
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(applicationContext, "분석에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseBody = it.body?.string()
                        val jsonObject = JSONObject(responseBody)
                        val result = jsonObject.optString("result")
                        runOnUiThread {
                            Toast.makeText(applicationContext, result, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "분석에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatdetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        resultTextView = binding.resultTextView

        chatRoomId = intent.getStringExtra(EXTRA_CHAT_ROOM_ID) ?: return
        otherUserId = intent.getStringExtra(EXTRA_OTHER_USER_ID) ?: return
        myUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        chatAdapter = ChatAdapter()
        linearLayoutManager = LinearLayoutManager(applicationContext)

        Firebase.database.reference.child(Key.DB_USERS).child(myUserId).get().addOnSuccessListener {
            val myUserItem = it.getValue(UserItem::class.java)
            myUserName = myUserItem?.username ?: ""
            getOtherUserData()
        }

        binding.chatRecyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = chatAdapter
        }

        chatAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                linearLayoutManager.smoothScrollToPosition(
                    binding.chatRecyclerView,
                    null,
                    chatAdapter.itemCount
                )
            }
        })

        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString()

            if (!isInit) {
                return@setOnClickListener
            }

            if (message.isEmpty()) {
                Toast.makeText(applicationContext, "빈 메시지를 전송할 수는 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newChatItem = ChatItem(
                message = message,
                userId = myUserId
            )

            // Firebase 데이터 업로드 로직
            Firebase.database.reference.child(Key.DB_CHATS).child(chatRoomId).push().apply {
                newChatItem.chatId = key
                setValue(newChatItem).addOnFailureListener {
                    Log.e("ChatActivity", "Error saving chat: ", it)
                }
            }
            Log.d("ChatActivity", "Firebase 데이터 업로드 완료")

            val updates: MutableMap<String, Any> = hashMapOf(
                "${Key.DB_CHAT_ROOMS}/$myUserId/$otherUserId/lastMessage" to message,
                "${Key.DB_CHAT_ROOMS}/$myUserId/$otherUserId/chatRoomId" to chatRoomId,
                "${Key.DB_CHAT_ROOMS}/$myUserId/$otherUserId/otherUserId" to otherUserId,
                "${Key.DB_CHAT_ROOMS}/$myUserId/$otherUserId/otherUserName" to myUserName,
                "${Key.DB_CHAT_ROOMS}/$otherUserId/$myUserId/lastMessage" to message,
                "${Key.DB_CHAT_ROOMS}/$otherUserId/$myUserId/chatRoomId" to chatRoomId,
                "${Key.DB_CHAT_ROOMS}/$otherUserId/$myUserId/otherUserId" to myUserId,
                "${Key.DB_CHAT_ROOMS}/$otherUserId/$myUserId/otherUserName" to myUserName,
            )
            Firebase.database.reference.updateChildren(updates)

            val client = OkHttpClient()

            val root = JSONObject()
            val notification = JSONObject()
            notification.put("title", getString(R.string.app_name))
            notification.put("body", message)

            root.put("to", otherUserFcmToken)
            root.put("priority", "high")
            root.put("notification", notification)

            val requestBody =
                root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request =
                Request.Builder().post(requestBody).url("http://127.0.0.1:4040").build()  // 수정 필요
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        // 응답이 성공적이지 않은 경우에 대한 처리
                        println("서버 요청 실패: ${response.code} - ${response.message}")
                    } else {
                        // 응답이 성공적인 경우에 대한 처리
                        println("서버 응답: ${response.body?.string()}")
                    }
                }
            })

            binding.messageEditText.text.clear()
        }

    }


    private fun getOtherUserData() {
        Firebase.database.reference.child(Key.DB_USERS).child(otherUserId).get()
            .addOnSuccessListener {
                val otherUserItem = it.getValue(UserItem::class.java)
                otherUserFcmToken = otherUserItem?.fcmToken.orEmpty()
                chatAdapter.otherUserItem = otherUserItem // ChatAdapter에 otherUserItem 설정
                isInit = true
                Log.d("ChatActivity", "isInit is now true")
                getChatData()
            }
    }

    private fun getChatData() {
        Firebase.database.reference.child(Key.DB_CHATS).child(chatRoomId)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatItem = snapshot.getValue(ChatItem::class.java)
                    chatItem ?: return
                    chatItemList.add(chatItem)
                    chatAdapter.submitList(chatItemList.toMutableList())
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {}

            })
    }

    companion object {
        const val EXTRA_CHAT_ROOM_ID = "CHAT_ROOM_ID"
        const val EXTRA_OTHER_USER_ID = "OTHER_USER_ID"
    }

}
