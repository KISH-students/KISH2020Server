![주석 2020-08-29 142602](https://user-images.githubusercontent.com/29895665/91631442-c0488100-ea03-11ea-8712-18e2aaa3cf74.png)

# 이 리포지토리는 [여기](https://github.com/KISH-students/KISH_server)로 이동되었습니다.
[참고](https://github.com/KISH-students/KISH2020Server/issues/4)

# KISH2020 - Server
KISH2020SERVER는 **KISH 2020**의 서버 처리를 담당하는 프로그램입니다.

또한 이 리포지트리는 KISH 학생이라면 누구나 자유롭게 기여할 수 있습니다.

자세한건 [여기](https://github.com/KISH-students/I-WANNA-JOIN) 를 참고해주세요.

# 주요 API

API | 요청 방식 | 설명
--- | -------- | ----
/api/getWeather | GET | 위도 경도를 통해 날씨정보를 얻습니다. ( Meteorogisk institutt 제공 )
/api/getCount | GET | 어플 실행 횟수를 받습니다.
/api/getLunch | GET | 식단 메뉴를 받습니다.
/api/getExamDates | GET | 학교 시험일정을 받습니다.
/api/getCalendar | GET | 학사 일정 정보를 받습니다.
/api/subcribeNotification | GET | 알림을 구독합니다.
/api/unsubcribeNotification | GET | 알림 구독을 해제합니다.
/api/checkSubscriptiohn | GET | 알림 구독 여부를 확인합니다.
/api/toggleLunchLikes | POST | 식단 좋아요를 toggle합니다.
/api/library/getInfo | GET | 회원 정보를 받습니다.
/api/library/getLoanedBooks | GET | 대출한 도서를 확인합니다.
/api/library/isMember | POST | 회원가입된 회원인지 확인합니다.
/api/library/findID | POST | 회원 id를 찾습니다.
/api/library/findPWD | POST | 회원 비밀번호를 찾습니다.
/api/library/changePWD | POST | 비밀번호를 변경합니다.
/api/library/searchBooks | POST | 도서를 찾습니다.
/api/library/checkID | POST | 도서관에 이미 가입된 회원 ID인지 확인합니다.
/api/library/register | POST | 도서관에 가입합니다.
/api/library/login | POST | 도서관에 로그인합니다.
/api/post/getMenuIds | GET | 서버에 가입된 모든 메뉴 id를 전달받습니다.
/api/post/getPostsFromMenu | GET | 특정 메뉴의 글들을 불러옵니다.
/api/post/getPost | GET | 게시물을 불러옵니다.
/api/post/searchPost | GET | 게시물을 검색합니다.

# 사용하기
KISH 2020 테스트를 위해 구동하거나 본인만의 학교 어플을 만드는 등의 자유로운 용도를 위해 사용할 수 있습니다.

기본 포트 : 40917

톰캣 연동을 위한 ajp 포트 : 8009

## 사용하기 - 단순 테스트용도
프로젝트 세팅 후, 아래 명령어로 프로젝트를 jar로 패키징 합니다.
```
mvn clean package
```

이 후, 생성된 jar 파일이 있는 폴더에서 터미널(CMD)에 아래 명령어를 입력하여 실행할 수 있습니다.
```
java -jar kish2020server.jar
```
이제 http://localhost:40917 으로 접속할 수 있습니다.

**https 접속이 필요한 경우 springBoot, 톰캣8, 아파치2를 연동하는 방법을 찾아보세요 (KISH2020Server의 ajp포트는 8009입니다)**

# 사용된 라이브러리
- Spring Boot
- json-simple
- Gson
- commons-io
- jsoup 
- firebase-admin
- auto-value
- commons-lang3
- mysql-connector-java
- [KoreanTextMatcher](https://github.com/bangjunyoung/KoreanTextMatcher)

# Java-doc
[java doc](https://ccc1.kro.kr/java-docs/kish2020Server/)

# License
KISH2020Server는 Apache-2.0 License를 따릅니다.
