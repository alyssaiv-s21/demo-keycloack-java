# demo-keycloack-java
A Java application that retrieves user and group information from Keycloak


1. ```docker compose up -d```   
   Результат: будет поднят контейнер, внутри запущен keycloack в режиме разработки, и с тестовыми данными (из папки /imports)


2. запустить приложение java и вызвать ```http://localhost:8081/users```
   Результат: сведения о пользователях и их группах будут отображены в консоли
