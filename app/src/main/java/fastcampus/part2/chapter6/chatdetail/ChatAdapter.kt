package fastcampus.part2.chapter6.chatdetail

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fastcampus.part2.chapter6.databinding.ItemChatBinding
import fastcampus.part2.chapter6.userlist.UserItem
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import android.os.Handler
import android.os.Looper
import android.widget.Toast



class ChatAdapter : ListAdapter<ChatItem, ChatAdapter.ViewHolder>(differ) {

    interface ModelService {
        @POST("analyze")
        fun analyzeMessage(@Body request: AnalyzeRequest): Call<AnalyzeResponse>
    }

    data class AnalyzeRequest(val message: String)
    data class AnalyzeResponse(val result: String)

    // Retrofit 및 서비스 초기화
    private val retrofit: Retrofit
    private val service: ModelService

    init {
        retrofit = Retrofit.Builder()
            .baseUrl("http://9355-34-142-200-166.ngrok.io")  // ngrok 주소로 변경
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(ModelService::class.java)
    }

    var otherUserItem: UserItem? = null

    inner class ViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatItem) {
            if (item.userId == otherUserItem?.userId) {
                // 상대방의 메시지 처리
                binding.usernameTextView.isVisible = true
                binding.usernameTextView.text = otherUserItem?.username
                binding.messageTextView.text = item.message
                binding.messageTextView.gravity = Gravity.START

                binding.modelingButton.isVisible = true // 분석 버튼을 보여줌

                binding.modelingButton.setOnClickListener {
                    val message = item.message
                    analyzeMessage(message.toString())
                }

            } else {
                // 사용자 자신의 메시지 처리
                binding.usernameTextView.isVisible = false
                binding.messageTextView.text = item.message
                binding.messageTextView.gravity = Gravity.END

                binding.modelingButton.isVisible = false // 분석 버튼을 숨김
            }
        }

        private fun analyzeMessage(message: String) {
            val call = service.analyzeMessage(AnalyzeRequest(message))
            call.enqueue(object : Callback<AnalyzeResponse> {
                override fun onResponse(call: Call<AnalyzeResponse>, response: Response<AnalyzeResponse>) {
                    if (response.isSuccessful) {
                        val result = response.body()?.result
                        // 결과에 따라 원하는 동작 수행 (예: 분석 결과 텍스트뷰에 설정)
                        val analysisResultTextView = binding.analysisResultTextView
                        val analysisResult = if (result == "가스라이팅 문장입니다.") {
                            "가스라이팅 문장입니다."
                        } else {
                            "일상 대화 문장입니다."
                        }
                        analysisResultTextView.text = "분석 결과: $analysisResult"
                    } else {
                        // API 호출 실패 처리
                        showResultToast("분석에 실패하였습니다.")
                    }
                }

                override fun onFailure(call: Call<AnalyzeResponse>, t: Throwable) {
                    // 네트워크 오류 처리
                    showResultToast("네트워크 오류가 발생하였습니다.")
                }
            })
        }

        private fun showResultToast(message: String) {
            val context = binding.root.context
            val uiHandler = Handler(Looper.getMainLooper())
            uiHandler.post {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChatBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val differ = object : DiffUtil.ItemCallback<ChatItem>() {
            override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem.chatId == newItem.chatId
            }

            override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
