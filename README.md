# 날씨 정보 앱
playstore : https://play.google.com/store/apps/details?id=org.techtown.weatherpublicapiapp

기상청 open api를 활용한 날씨 앱 입니다.

최대 6시간까지 날씨 예보를 확인할 수 있습니다.

# 사용 open api & 자료

기상청_단기예보 ((구)_동네예보) 조회서비스 : https://www.data.go.kr/data/15084084/openapi.do

위도,경도 X,Y 격자 좌표로 변환 : https://gist.github.com/fronteer-kr/14d7f779d52a21ac2f16

# 추가하고 싶은 기능
일정 시간에 현재 날씨 ex) 비 맑음 흐림 등등 을 알려주는 notification 기능

# 사용된 기술
android studio, kotlin, retrofit2, recyclerView...

# 동작 과정
사용자 위치정보 수집 -> 위도,경도 좌표를 X,Y 격자 좌표로 변환  -> X,Y 격자 좌표로 초단기예보조회 정보 얻어오기

![image](https://user-images.githubusercontent.com/86578252/176240460-32df7e5e-88a9-4993-b6df-43382edaa67e.png)

# Result

![KakaoTalk_20220708_012022862_01](https://user-images.githubusercontent.com/86578252/177823666-ece9e6bd-66ac-4a0f-b242-953448f8d277.jpg)
