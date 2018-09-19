#AlpineLinux with a glibc-2.23, Oracle Java 8, sbt and git
FROM anapsix/alpine-java:8_jdk

LABEL maintainer="vrijeu@gmail.com"

# sbt

ENV SBT_URL=https://dl.bintray.com/sbt/native-packages/sbt
ENV SBT_VERSION 0.13.15
ENV INSTALL_DIR /usr/local
ENV SBT_HOME /usr/local/sbt
ENV PATH ${PATH}:${SBT_HOME}/bin

# Install sbt
RUN apk add --no-cache --update bash wget && mkdir -p "$SBT_HOME" && \
    wget -qO - --no-check-certificate "https://dl.bintray.com/sbt/native-packages/sbt/$SBT_VERSION/sbt-$SBT_VERSION.tgz" |  tar xz -C $INSTALL_DIR && \
    echo -ne "- with sbt $SBT_VERSION\n" >> /root/.built

# Install git
RUN  apk add --no-cache git openssh

# Copy play project and compile it
# This will download all the ivy2 and sbt dependencies and install them
# in the container /root directory

ENV PROJECT_HOME /usr/src
ENV PROJECT_NAME play-java-swan-cloud

COPY . ${PROJECT_HOME}/${PROJECT_NAME}
RUN cd $PROJECT_HOME/$PROJECT_NAME && \
    sbt compile

# Command

CMD ["sbt"]

# Expose code volume and play port 9000

EXPOSE 9000
VOLUME "/code"
WORKDIR /code

# Call below commands to run the server
# docker build -t cowbird-cloud .
# docker run --rm  -it -v "${PWD}:/code" -p 9000:9000 cowbird-cloud
