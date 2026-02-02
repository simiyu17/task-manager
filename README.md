# task-manager
Task Manager

curl --location 'http://localhost:8080/realms/myrealm/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'client_id=spring-boot-client' \
--data-urlencode 'username=dev_user' \
--data-urlencode 'password=your_password' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'scope=openid'
