FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY . /app
RUN apt-get update && apt-get install -y ant vim python3 python3-pip && rm -rf /var/lib/apt/lists/*
CMD ["/bin/bash"]
