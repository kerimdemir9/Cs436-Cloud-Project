FROM openjdk:17
COPY target/customerModel.transactionModel-0.0.1-SNAPSHOT.jar customerModel.transactionModel-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "/customerModel.transactionModel-0.0.1-SNAPSHOT.jar"]