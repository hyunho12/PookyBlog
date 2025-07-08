# PookyBlog
----------------------------------
## 1. 프로젝트 소개
Spring Boot 기반의 블로그/게시판 시스템으로, JWT 기반 인증, 좋아요/조회수 기능, 댓글 관리, 대용량 트래픽 처리까지 구현한 포트폴리오용 웹 애플리케이션입니다.

## 2. 프로젝트 목적
+ JWT + Spring Security를 활용한 인증/인가
+ Redis + MySQL 기반의 조회수 캐싱 및 동시성 제어
+ 멀티모듈 아키텍처 설계
+ Snowflake ID, Docker 등 실무 기술 도입

## 3. 주요 기능
+ 회원가입/로그인: Spring Security + JWT 기반 인증
+ 게시글 CRUD: 제목, 내용, 작성자 관리
+ 댓글 기능: 게시글에 대한 댓글 등록, 수정, 삭제
+ 좋아요 기능: 낙관적/비관적 락, 증감쿼리 등 동시성 처리 실습
+ 조회수 기능: Redis를 활용한 캐시 조회수 처리
+ 관리자 기능: 권한(Role) 기반의 사용자 구분 및 관리

## 4. 기술 스택
+ Backend: Spring Boot, Spring Security, JPA (Hibernate)
+ Infra: Mysql, Docker, Redis
+ Frontend: Mustache, Html/Css, JavaScript, Bootstrap 4.3.1

## 5. 아키텍처 및 설계 포인트
+ 멀티모듈 구조: web, service, common 등으로 모듈 분리
+ Snowflake ID 적용: 분산 시스템에서 유일성 보장
+ 동시성 문제 해결 실습: 좋아요 기능에 다양한 락 전략 적용
+ 조회수 성능 최적화: Redis 캐시와 주기적 MySQL 반영
