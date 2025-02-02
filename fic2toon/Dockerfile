FROM amazoncorretto:17

# 빌드된 JAR 파일을 컨테이너로 복사
COPY build/libs/fic2toon-0.0.1-SNAPSHOT.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]