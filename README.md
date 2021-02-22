
# 전동 킥보드 블랙박스 및 미성년자 2차 인증 어플리케이션 개발     
</br>

## 목적 또는 기획 배경
전동 킥보드 확산에 따른 미성년자 킥보드 사용 문제 제기    
이에 대한 해결책으로 보호자 동의하에 사용할 수 있는 블랙박스 어플리케이션 기획
</br>

## 서비스 시나리오 순서도  

![image](https://user-images.githubusercontent.com/66519046/108667207-08141500-751c-11eb-8f1e-35f93f7fc0ad.png)  
</br>

## 사용된 기술 스택 

### 1. 앱

![image](https://user-images.githubusercontent.com/66519046/108670997-2c272480-7523-11eb-907e-cb1037b2db44.png)
</br>

- Android Studio: 회원가입, 로그인, 보호자 2차 인증, 블랙박스 영상
촬영 및 전송, 사고 다발 지역과 출발 및 도착 좌표 지도 표시 등 
java와 여러 api를 사용해 어플리케이션의 주요 기능들 구현
- Kakao api: 카카오톡에 로그인한 사용자들의 개인정보에 접근 및 
수집하는 api
- Gogle Map Api: 안드로이드 기기상에 지도와 좌표 마커, 사고 다
발 지역 등을 표시해주는 api
- Media recorder: 어플리케이션의 동영상 녹화 기능을 제공
- gps tracker: 좌표를 주소값으로 변경해주는 기능 제공. 주소값을 
MMS 형태로 전달하는 기능 제공
-Firebase: gogle에서 제공하는 Sas로 스토리지, DB 등 다양한 서
비스 제공, 문자 메시지 전달 기능 제공
- 가속도 센서: 스마트폰에 내장된 센서로 순간 변화량에 의해 트리
거를 감지. threshold를 조절해서 감지 기능 조절 가능
</br>

</br>

 ### 2. 웹-Pasta

![image](https://user-images.githubusercontent.com/66519046/108670970-1e719f00-7523-11eb-8f4b-16c7eba9cdeb.png)
</br>

- Spring bot : 여러 가지 스프링 프레임워크를 이용할 수 있는 어
플리케이션 생성 기능 제공, Gradle과 연동하여 필요한 의존성 주입. 
- JPA : Java Persistence API로서 JPA내부에서 JDBC API를 사용하여 
SQL을 호출해 DB와 통신 가능
- Thymeleaf : Template Engine으로 정적 컨텐츠인 HTML에 동적으
로 값을 받아오기 위해서 사용. 
- 네이버 지도 API : Rest API를 통해서 지도 불러오기 및 마커 생성
- MVC패턴 사용
</br>

</br>

## 3. 클라우드

![image](https://user-images.githubusercontent.com/66519046/108670930-0e59bf80-7523-11eb-9021-d8ed73f2957a.png)
</br>
- GCE: gogle compute engine. 사용자들이 원하는 양의 자원만큼
가상화하여 가상 서버 VM을 할당해주는 Ias 플랫폼. 2) Docker: 소프트웨어 응용프로그램을 컨테이너화하는 소프트웨어
- Kubernetes: 여러 VM 및 서버들로 cluster를 구축해 cluster 위에서
컨테이너들을 관리하는 컨테이너 오케스트레이터
- Apache & PHP: 앱에서 발생한 데이터를 Mysql DB로 전송하는 웹
서버 소프트웨어 및 programming language
- Mysql: 특정 데이터를 저장하는 DB 구축
- Gogle Cloud Storage: gogle에서 제공하는 storage 저장소. GCE에 
mount해서 사용 가능. Ap의 firebase와 연동 가능
- Prometheus & Grafana: 모니터링 도구로 cluster, DB 등의 metric
을 수집해 대쉬보드를 구축해 모니터링 할 수 있도록 해주는 오픈소
스 소프트웨어
</br>

</br>

## 최종 솔루션 및 서비스 구성도

![image](https://user-images.githubusercontent.com/66519046/108670883-fbdf8600-7522-11eb-8ce1-ab51c9671ee0.png)
</br>

- 회원 가입, 본인 인증 후 미성년자이면 보호자 동의 요구, 회원 정보는 클라우드에 저장
- 로그인하여 킥보드 사용 시 출발,도착 좌표 입력. 출발 시 보호자에게 알림
- 경로 상에 사고 다발 지역을 app상 지도에 표시, 이동 경로 보호자에게 제공
- 킥보드가 출발하면서 블랙 박스 녹화 시작
- 목적지까지 무사히 도착 시 app 종료 후 보호자에게 도착 알림
- 사고 발생 시, 보호자에게 알림 후 녹화 영상을 즉시 클라우드에 저장
- 관리자 모니터링 및 영상은 추후 법적 자료로 사용 가능
</br>

