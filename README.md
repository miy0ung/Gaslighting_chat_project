# 실시간 대화 속 가스라이팅 문장 위험 감지 앱서비스
![image](https://github.com/eoh9/Gaslighting_chat/assets/62730155/84d4e78c-c42a-46d3-b122-dfa93299ad4e)
![image](https://github.com/eoh9/Gaslighting_chat/assets/62730155/5af50c0e-a9e5-426a-a2d8-ca52e9fc6e89)
![image](https://github.com/eoh9/Gaslighting_chat/assets/62730155/6141a18e-f3c3-4303-9011-9a20a48e51e7)

현재 시중에 나와있는 여러 챗봇이나 클린봇들은 모두 욕설이나, 혐오표현 등 직관적인 부정문을 잡아주고 있습니다.
하지만, 그 어느 서비스에서도 가스라이팅 문장을 잡아낼 수는 없었습니다. 심지어, 우리가 많이 쓰는 chatgpt또한 가스라이팅 문장을 잡아내지 못하고 오히려 가스라이팅을 당하는 jailbreak현상을 겪고 있습니다.
가스라이팅은 직장내, 학교, 가정, 연인사이 등 수직관계가 형성될 수 있는 모든 관계에서 이루어질 수 있습니다. 따라서 저희 돈!가스는 대화 속에서 가스라이팅 문장을 감지하여 해당 문장의 위험도를 측정해줍니다.
사용자가 상대와 대화를 하면서 가스라이팅 의심 대화부분에 화면을 맞추고 오른쪽 상단에 보이는 버튼은 활성화시키면 해당 문장들 중 가스라이팅 문장이 있는 곳을 하이라이트 표시해줌으로써 인지시켜 줍니다. 아직 국내에서는 가스라이팅에 관련한 법적 처벌이 정의 되어 있지 않은 상태이지만, 꾸준히 사회에 이슈가 되고 있는 주제임으로 이에 대해 현재 우리가 할 수 있는 일은 그저 가스라이팅에 대한 경각심을 더욱 심어주고 미리 예방할 수 있도록 도움을 드리고 싶습니다.

## 데이터 수집
1. 공공데이터 & AI Hub
2. 비정형 데이터: 디시인사이트, 네이버카페, 다음카페, 네이트판, 인벤, 펨코, 인스타그램, 페이스북 등.
3. 구글 대화 이미지 크로링 및 텍스트 추출
4. 데이터셋 자체 제작(전문가의 자문을 받아 검증을 완료, LGG그래프 활용)

## 데이터 증강
1. KoGPT fine-tuning

## 가스라이팅 문장 분류
1. KoBERT

## 앱 서비스 제작
1. 안드로이드 스튜디오(코틀린)

## Firebase & 서버 연동 기술
1. Firebase Realtime Database
2. Firebase Authentication
3. Firebase Cloude Message (일명 FCM)
4. ngrok 가상 서버 연결 호출 & 출력


<img width="313" alt="1" src="https://github.com/eoh9/Gaslighting_chat/assets/62730155/8a549239-ff42-481c-9240-c066ad7ae642">
<img width="313" alt="2" src="https://github.com/eoh9/Gaslighting_chat/assets/62730155/e47600f4-486e-43ab-bbd5-ad1f9c890482">
<img width="311" alt="3" src="https://github.com/eoh9/Gaslighting_chat/assets/62730155/d05a8cf3-9846-42f5-ad58-229e463c442b">
<img width="310" alt="4" src="https://github.com/eoh9/Gaslighting_chat/assets/62730155/0738566d-dc56-4c4e-8351-41882199d7bc">
<img width="308" alt="5" src="https://github.com/eoh9/Gaslighting_chat/assets/62730155/2efaf659-54a4-4d47-844e-659bbb320922">




- 팀원 : 김다빈, 김대현, 김미영, 오에린, 이승용, 한승윤
