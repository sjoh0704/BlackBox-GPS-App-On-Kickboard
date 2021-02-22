# BlackBox-GPS-App on kickboard

## 전동 킥보드 블랙박스 및 미성년자 2차 인증 어플리케이션 개발


- ### 목적(또는 추진배경)

전동 킥보드 확산에 따라 많은 사람들이 전동 킥보드를 사용하고 있다. 그러나 전동 킥보드가 대중화되면서 우려의 목소리 역시 많이 나오고 있다. 속도가 빨라 사고 발생 확률도 높고 전동 킥보드 규제가 완화되면서 미성년자 학생들도 사용이 가능하게 되었기 때문이다. 따라서 미성년자가 전동 킥보드를 사용할 때, 보호자의 동의 하에 사용할 수 있고 킥보드 사고 발생 시, 자동차처럼 블랙박스를 통해서 사고 상황을 대처할 수 있어야 한다고 생각하여 본 서비스 및 어플리케이션을 기획하였다.



- ### 서비스 시나리오 순서도

![image](https://user-images.githubusercontent.com/66519046/108667207-08141500-751c-11eb-8f1e-35f93f7fc0ad.png)



- ### 파트별 사용 기술 설명
#### 1. 앱:
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

#### 2. 웹-Pasta:
- Spring bot : 여러 가지 스프링 프레임워크를 이용할 수 있는 어
플리케이션 생성 기능 제공, Gradle과 연동하여 필요한 의존성 주입. 
- JPA : Java Persistence API로서 JPA내부에서 JDBC API를 사용하여 
SQL을 호출해 DB와 통신 가능
- Thymeleaf : Template Engine으로 정적 컨텐츠인 HTML에 동적으
로 값을 받아오기 위해서 사용. 
- 네이버 지도 API : Rest API를 통해서 지도 불러오기 및 마커 생성
- MVC패턴 사용

#### 3. 클라우드:
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


- ### 사용기술을 통한 서비스 구현

1. 앱 - 회원가입 및 로그인

![image](https://user-images.githubusercontent.com/66519046/108667247-211cc600-751c-11eb-8cc6-e8cac6cbe4ff.png)

![image](https://user-images.githubusercontent.com/66519046/108667269-2c6ff180-751c-11eb-97b9-231a3bb3b092.png)

![image](https://user-images.githubusercontent.com/66519046/108667306-427db200-751c-11eb-8fdb-7195e5b66579.png)

![image](https://user-images.githubusercontent.com/66519046/108667326-4f020a80-751c-11eb-9245-5bdc0331734f.png)

![image](https://user-images.githubusercontent.com/66519046/108669998-74454780-7521-11eb-8273-946982da19b9.png)

![image](https://user-images.githubusercontent.com/66519046/108667407-7f49a900-751c-11eb-9638-f5c2560d1cfd.png)

![image](https://user-images.githubusercontent.com/66519046/108667433-8c669800-751c-11eb-87ce-e5685d11c7bb.png)

![image](https://user-images.githubusercontent.com/66519046/108667480-a6a07600-751c-11eb-93f7-42b904424b17.png)

![image](https://user-images.githubusercontent.com/66519046/108668022-b3719980-751d-11eb-88e2-ec4dc9b1a616.png)



![image](https://user-images.githubusercontent.com/66519046/108667629-fc751e00-751c-11eb-8b64-c3685b9f92b6.png)

![image](https://user-images.githubusercontent.com/66519046/108667910-79a09300-751d-11eb-8a8c-f0c477dead48.png)




