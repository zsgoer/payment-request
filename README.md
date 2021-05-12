# payment-request
결제시스템

- API 설계문서는 좌측링크를 참고 부탁드립니다. (https://zsgoer.github.io/index.html) 


A. 사용 개발 프레임워크 : Springboot
- Spring Web
- Spring Data JPA
- Spring HATEOS
- 개발도구 : intelliJ
- JDK : 11

B. 테이블 설계
- 개략적인 개요는 아래와 같습니다.
1. PAYMENT_REQUEST : 결제 API 요청 이력을 저장하기 위한 테이블. MANAGE_NO 테이블의 id를 기반으로 MANAGE_NO를 SEQUENCIAL하게 생성하며 1:1 구조를 지니고 있습니다.
2. PAYMENT_CENCEL  : 결제 취소 API 요청 이력을 저장하기 위한 테이블. 부분취소가 있기 때문에 MANAGE_NO 테이블의 MANAGE_NO 컬럼을 키로 1:多 구조를 지니고 있습니다.
3. MANAGE_NO : 관리번호를 기준으로 현재 결제상태를 저장하는 테이블.

C. 문제해결 전략
- 상기와 같이 테이블 설계 후 간단한 테스트 코드를 작성하여 서비스를 호출하여 디버깅하는 방식으로 개발하였습니다.
- 암복호화 방식은 KISA의 SEED 방식의 CBC를 참조하여 구현하였습니다.
- 기본적으로 @Valid 어노테이션과 PaymentRequestValidator, PaymentCancelValidator 클래스를 구현하여 명세서에 있는 제약조건을 맞추었습니다.
- 선택문제의 TESTCASE는 TestCaseModule Class를 구현하여 단위테스트를 진행했습니다.


D. 빌드 및 실행 방법
해당 프로젝트는 maven 기반으로 생성이 되었습니다. 메이븐 기반 IDE에서 IMPORT 하여 실행하거나, 아래와 같은 절차로 빌드를 진행하시면 됩니다.
1. 소스코드 다운로드.
2. 해당 압축을 풀고, 커멘드 창에서 해당 위치로 이동 (pom.xml 파일 위치.)
3. mvn compile 입력 (프로젝트 생성한 버전은 java11입니다.)
![캡처](https://user-images.githubusercontent.com/31767068/118047517-d6da1100-b3b5-11eb-9527-45ff54972d35.PNG)

4. mvn build 입력 (해당 폴더 하위에 jar 파일 생성)
![캡처2](https://user-images.githubusercontent.com/31767068/118048050-a9419780-b3b6-11eb-9b8a-3b51347becf6.PNG)

5. java -jar target\paymentrequest-0.0.1-SNAPSHOT.jar 명령어로 실행 가능하며, localhost:8080 도메인에서 해당 프로그램을 실행시킬 수 있습니다.



  
 
    
