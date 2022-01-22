FROM ubuntu
RUN apt update
RUN apt install openjdk-8-jre -y
RUN apt install firefox -y
RUN mkdir /app
COPY ./build/install/bigbluebot/ /app/
WORKDIR /app/bin
CMD ["./bigbluebot"]