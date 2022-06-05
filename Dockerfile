FROM openjdk:11

EXPOSE 8080

RUN mkdir ./app

COPY ./GifGiverAlfa.jar ./app

CMD java -jar ./app/GifGiverAlfa.jar