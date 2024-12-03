# sos-app

## 실행방법

0. 카카오톡이 설치된 안드로이드 스마트폰을 준비한다.
1. 아래의 구글 드라이브 설치 링크([https://drive.google.com/file/d/1avWbh2DUx0TJsRvgZlmVnBf0eKWcvy4K/view?usp=sharing](https://drive.google.com/file/d/1avWbh2DUx0TJsRvgZlmVnBf0eKWcvy4K/view?usp=sharing))를 통하여 apk 파일을 설치한다
2. 일반 사용자 기능(응급실 지도 조회, 접수 조회)은 카카오 로그인 후 사용이 가능하다.
   - 접수 조회 시 입력할 접수 번호는 웹([www.yu-sos.co.kr](www.yu-sos.co.kr))의 병원으로 로그인하여 특정 접수에 대한 6자리 문자열을 확인하여 입력한다.
   - 📌 웹 실행 방법 참고 : [Web github](https://github.com/YU-SOS/sos-web)
4. 구급대 기능(구급대원 정보 조회, 구급대원 관리, 접수 요청 등)은 구급대 로그인 후 사용이 가능하다.
   - 테스트용 아이디 1 - ID: testamb1 / PW: testamb1
   - 테스트용 아이디 2 - ID: testamb14 / PW: testamb14 (회원가입 요청 대기 중인 구급대, 로그인 실패 확인 용)
   - 테스트용 아이디 3 - ID: testamb15 / PW: testamb15 (회원가입 요청을 거절당한 구급대, 로그인 실패 확인 용)
---

## 📌 Convention

### branch naming
```
main - develop - feature/*
```

### commit message 
|type|description|
|:-:|---|
|feat|새로운 기능 개발, 요구사항을 반영한 기능 수정|
|fix|기능에 관한 버그 수정|
|style|코드에 변화가 없는 코드 스타일, 포맷팅에 관한 수정|
|refact|기능 변화가 아닌 코드 리팩토링|
|test|테스트 코드 추가/수정|
|chore|그 외 수정사항 ex) 패키지매니저, 파일/디렉토리 이름・위치 변경|
|docs|문서・주석 수정|
