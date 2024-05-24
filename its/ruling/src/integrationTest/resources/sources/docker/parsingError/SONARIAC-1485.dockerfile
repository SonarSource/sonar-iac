FROM debian:stretch-slim

ENV BDBVER db-4.8.30.NC
RUN wget --quiet https://download.oracle.com/berkeley-db/$BDBVER.tar.gz; \
        echo "Done"
