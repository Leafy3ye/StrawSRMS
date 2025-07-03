# 开发者备注：
打包命令：
$env:Path = [System.Environment]::GetEnvironmentVariable("Path", [System.EnvironmentVariableTarget]::Machine) --加载mvn
mvn clean package -DskipTests
java -jar target/coffee_management_system_backend-0.0.1-SNAPSHOT.jar
